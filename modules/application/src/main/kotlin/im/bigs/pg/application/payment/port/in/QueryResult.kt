package im.bigs.pg.application.payment.port.`in`

import im.bigs.pg.domain.payment.Payment
import im.bigs.pg.domain.payment.PaymentSummary

/**
 * 결제 조회 결과.
 * - summary 는 items 와 동일한 필터 조건으로 집계됩니다.
 * - nextCursor 가 null이면 다음 페이지가 없음을 의미합니다.
 */
data class QueryResult(
    val items: List<Payment>,
    val summary: PaymentSummary,
    val nextCursor: String?,
    val hasNext: Boolean,
)
