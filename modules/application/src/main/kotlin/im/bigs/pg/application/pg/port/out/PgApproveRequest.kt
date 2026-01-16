package im.bigs.pg.application.pg.port.out

import java.math.BigDecimal

/** PG 승인 요청 최소 정보. */
data class PgApproveRequest(
    val partnerId: Long,
    val amount: BigDecimal,
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val cardPassword: String,
    val productName: String?,
)
