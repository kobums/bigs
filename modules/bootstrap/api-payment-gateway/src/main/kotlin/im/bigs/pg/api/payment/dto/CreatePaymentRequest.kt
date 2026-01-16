package im.bigs.pg.api.payment.dto

import jakarta.validation.constraints.Min
import java.math.BigDecimal

data class CreatePaymentRequest(
    val partnerId: Long,
    @field:Min(1)
    val amount: BigDecimal,
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val cardPassword: String,
    val productName: String? = null,
)
