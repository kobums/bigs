package im.bigs.pg.external.pg

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.security.MessageDigest
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * TestPG 클라이언트: 외부 PG API와 실제 연동합니다.
 * - AES-256-GCM 암호화 사용
 * - partnerId=2 (TESTPAY1) 제휴사 전용
 */
@Component
class TestPgClient(
    @Value("\${pg.test.api-key:11111111-1111-4111-8111-111111111111}")
    private val apiKey: String,
    @Value("\${pg.test.iv:AAAAAAAAAAAAAAAA}")
    private val ivBase64: String,
    @Value("\${pg.test.base-url:https://api-test-pg.bigs.im}")
    private val baseUrl: String,
) : PgClientOutPort {

    private val httpClient = HttpClient.newHttpClient()
    private val objectMapper = ObjectMapper().apply {
        registerModule(KotlinModule.Builder().build())
        registerModule(JavaTimeModule())
    }

    override fun supports(partnerId: Long): Boolean = partnerId == 2L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        val plaintext = buildPlaintext(request)
        val encrypted = encrypt(plaintext)

        val requestBody = mapOf("enc" to encrypted)
        val httpRequest = HttpRequest.newBuilder()
            .uri(URI.create("$baseUrl/api/v1/pay/credit-card"))
            .header("Content-Type", "application/json")
            .header("API-KEY", apiKey)
            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
            .build()

        val response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString())

        when (response.statusCode()) {
            200 -> { /* success - handled below */ }
            401 -> {
                val authError = parseAuthError(response.body())
                throw PgAuthenticationException(authError.first, authError.second)
            }
            422 -> {
                val errorBody = try {
                    objectMapper.readValue<PgErrorResponse>(response.body())
                } catch (e: Exception) {
                    PgErrorResponse(422, "UNKNOWN", response.body(), null)
                }
                // errorCode가 숫자 코드인 경우 PgErrorCode enum으로 매핑
                val numericCode = errorBody.errorCode.toIntOrNull()
                val mappedError = numericCode?.let { PgErrorCode.fromCode(it) }
                throw PgApprovalException(
                    code = errorBody.code,
                    errorCode = mappedError?.name ?: errorBody.errorCode,
                    message = mappedError?.description ?: errorBody.message,
                    referenceId = errorBody.referenceId,
                )
            }
            else -> throw PgApprovalException(
                code = response.statusCode(),
                errorCode = "UNKNOWN",
                message = "Unexpected PG response: ${response.body()}",
                referenceId = null,
            )
        }

        val successBody = objectMapper.readValue<TestPgSuccessResponse>(response.body())
        return PgApproveResult(
            approvalCode = successBody.approvalCode,
            approvedAt = LocalDateTime.parse(successBody.approvedAt, DateTimeFormatter.ISO_DATE_TIME),
            status = PaymentStatus.APPROVED,
        )
    }

    private fun buildPlaintext(request: PgApproveRequest): String {
        val data = mapOf(
            "cardNumber" to request.cardNumber,
            "birthDate" to request.birthDate,
            "expiry" to request.expiry,
            "password" to request.cardPassword,
            "amount" to request.amount.toInt(),
        )
        return objectMapper.writeValueAsString(data)
    }

    /** 401 응답 바디에서 에러 코드/메시지 파싱 */
    private fun parseAuthError(body: String): Pair<PgAuthErrorCode, String> {
        return try {
            val json = objectMapper.readValue<Map<String, Any>>(body)
            val message = json["message"]?.toString() ?: "Authentication failed"
            val errorCode = when {
                message.contains("missing", ignoreCase = true) ||
                    message.contains("헤더", ignoreCase = true) ->
                    PgAuthErrorCode.MISSING_API_KEY
                message.contains("format", ignoreCase = true) ||
                    message.contains("UUID", ignoreCase = true) ||
                    message.contains("포맷", ignoreCase = true) ->
                    PgAuthErrorCode.INVALID_API_KEY_FORMAT
                message.contains("unregistered", ignoreCase = true) ||
                    message.contains("미등록", ignoreCase = true) ||
                    message.contains("not found", ignoreCase = true) ->
                    PgAuthErrorCode.UNREGISTERED_API_KEY
                else -> PgAuthErrorCode.UNREGISTERED_API_KEY
            }
            errorCode to message
        } catch (e: Exception) {
            PgAuthErrorCode.UNREGISTERED_API_KEY to "PG authentication failed: $body"
        }
    }

    private fun encrypt(plaintext: String): String {
        val keyBytes = MessageDigest.getInstance("SHA-256").digest(apiKey.toByteArray())
        val secretKey = SecretKeySpec(keyBytes, "AES")
        val iv = Base64.getUrlDecoder().decode(ivBase64)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))

        val ciphertext = cipher.doFinal(plaintext.toByteArray())
        return Base64.getUrlEncoder().withoutPadding().encodeToString(ciphertext)
    }

    /** 성공 응답 (200) */
    private data class TestPgSuccessResponse(
        val approvalCode: String,
        val approvedAt: String,
        val maskedCardLast4: String,
        val amount: Int,
        val status: String,
    )

    /** 실패 응답 (422) */
    private data class PgErrorResponse(
        val code: Int,
        val errorCode: String,
        val message: String,
        val referenceId: String?,
    )
}

/** PG 인증 실패 예외 (401). */
class PgAuthenticationException(
    val errorCode: PgAuthErrorCode,
    override val message: String,
) : RuntimeException(message)

/** 401 인증 실패 세부 코드 */
enum class PgAuthErrorCode(val description: String) {
    MISSING_API_KEY("API-KEY 헤더 없음"),
    INVALID_API_KEY_FORMAT("API-KEY 포맷 오류 (UUID 형식이 아님)"),
    UNREGISTERED_API_KEY("미등록 API-KEY"),
}

/** PG 승인 실패 예외 (422 등). */
class PgApprovalException(
    val code: Int,
    val errorCode: String,
    override val message: String,
    val referenceId: String? = null,
) : RuntimeException(message)

/**
 * 422 에러 코드 매핑
 * - 1001: STOLEN_OR_LOST - 도난 또는 분실된 카드
 * - 1002: INSUFFICIENT_LIMIT - 한도 초과
 * - 1003: EXPIRED_OR_BLOCKED - 정지/만료된 카드
 * - 1004: TAMPERED_CARD - 위조/변조된 카드
 * - 1005: TAMPERED_CARD - 위조/변조된 카드 (허용되지 않은 카드)
 */
enum class PgErrorCode(val code: Int, val description: String) {
    STOLEN_OR_LOST(1001, "도난 또는 분실된 카드입니다."),
    INSUFFICIENT_LIMIT(1002, "한도가 초과되었습니다."),
    EXPIRED_OR_BLOCKED(1003, "정지되었거나 만료된 카드입니다."),
    TAMPERED_CARD(1004, "위조 또는 변조된 카드입니다."),
    TAMPERED_CARD_NOT_ALLOWED(1005, "위조 또는 변조된 카드입니다. (허용되지 않은 카드)"),
    ;

    companion object {
        fun fromCode(code: Int): PgErrorCode? = entries.find { it.code == code }
    }
}
