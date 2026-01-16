package im.bigs.pg.api.payment.dto

import java.math.BigDecimal

data class QueryResponse(
    val items: List<PaymentResponse>,
    val summary: Summary,
    val nextCursor: String?,
    val hasNext: Boolean,
)

data class Summary(
    val count: Long,
    val totalAmount: BigDecimal,
    val totalNetAmount: BigDecimal,
)
