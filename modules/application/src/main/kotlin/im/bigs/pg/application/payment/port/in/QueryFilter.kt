package im.bigs.pg.application.payment.port.`in`

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

/**
 * 결제 조회 조건.
 * - cursor 는 다음 페이지를 가리키는 토큰(Base64 URL-safe)
 * - 기간은 UTC 기준 권장
 */
data class QueryFilter(
    val partnerId: Long? = null,
    val status: String? = null,
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val from: LocalDateTime? = null,
    @get:JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val to: LocalDateTime? = null,
    val cursor: String? = null,
    val limit: Int = 20,
)
