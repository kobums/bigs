package im.bigs.pg.api.payment

import im.bigs.pg.api.payment.dto.CreatePaymentRequest
import im.bigs.pg.api.payment.dto.PaymentResponse
import im.bigs.pg.api.payment.dto.QueryResponse
import im.bigs.pg.api.payment.dto.Summary
import im.bigs.pg.application.payment.port.`in`.PaymentCommand
import im.bigs.pg.application.payment.port.`in`.PaymentUseCase
import im.bigs.pg.application.payment.port.`in`.QueryFilter
import im.bigs.pg.application.payment.port.`in`.QueryPaymentsUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

/**
 * 결제 API 진입점.
 * - POST: 결제 생성
 * - GET: 결제 조회(커서 페이지네이션 + 통계)
 */
@RestController
@RequestMapping("/api/v1/payments")
@Validated
class PaymentController(
    private val paymentUseCase: PaymentUseCase,
    private val queryPaymentsUseCase: QueryPaymentsUseCase,
) {

    /** 결제 생성 요청 페이로드(간소화된 필드). */
    /** API 응답을 위한 변환용 DTO. 도메인 모델을 그대로 노출하지 않습니다. */

    /**
     * 결제 생성.
     *
     * @param req 결제 요청 본문
     * @return 생성된 결제 요약 응답
     */
    @PostMapping
    fun create(@RequestBody req: CreatePaymentRequest): ResponseEntity<PaymentResponse> {
        val saved = paymentUseCase.pay(
            PaymentCommand(
                partnerId = req.partnerId,
                amount = req.amount,
                cardNumber = req.cardNumber,
                birthDate = req.birthDate,
                expiry = req.expiry,
                cardPassword = req.cardPassword,
                productName = req.productName,
            ),
        )
        return ResponseEntity.ok(PaymentResponse.from(saved))
    }

    /** 목록 + 통계를 포함한 조회 응답. */

    /**
     * 결제 조회(커서 기반 페이지네이션 + 통계).
     *
     * @param partnerId 제휴사 필터
     * @param status 상태 필터
     * @param from 조회 시작 시각(ISO-8601)
     * @param to 조회 종료 시각(ISO-8601)
     * @param cursor 다음 페이지 커서
     * @param limit 페이지 크기(기본 20)
     * @return 목록/통계/커서 정보
     */
    @GetMapping
    fun query(
        @RequestParam(required = false) partnerId: Long?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") from: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") to: LocalDateTime?,
        @RequestParam(required = false) cursor: String?,
        @RequestParam(defaultValue = "20") limit: Int,
    ): ResponseEntity<QueryResponse> {
        val res = queryPaymentsUseCase.query(
            QueryFilter(partnerId, status, from, to, cursor, limit),
        )
        return ResponseEntity.ok(
            QueryResponse(
                items = res.items.map { PaymentResponse.from(it) },
                summary = Summary(res.summary.count, res.summary.totalAmount, res.summary.totalNetAmount),
                nextCursor = res.nextCursor,
                hasNext = res.hasNext,
            ),
        )
    }
}
