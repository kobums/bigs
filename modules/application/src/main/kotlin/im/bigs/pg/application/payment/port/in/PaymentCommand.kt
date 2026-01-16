package im.bigs.pg.application.payment.port.`in`

import java.math.BigDecimal

/**
 * 결제 생성에 필요한 최소 입력.
 *
 * @property partnerId 제휴사 식별자
 * @property amount 결제 금액(정수 금액 권장)
 * @property cardNumber 전체 카드번호(PG 연동용, 16자리)
 * @property birthDate 생년월일(YYYYMMDD)
 * @property expiry 유효기간(MMYY)
 * @property cardPassword 카드 비밀번호 앞 2자리
 * @property productName 상품명(없을 수 있음)
 */
data class PaymentCommand(
    val partnerId: Long,
    val amount: BigDecimal,
    val cardNumber: String,
    val birthDate: String,
    val expiry: String,
    val cardPassword: String,
    val productName: String? = null,
) {
    /** 카드 BIN (앞 6자리) */
    val cardBin: String get() = cardNumber.replace("-", "").take(6)

    /** 카드 마지막 4자리 */
    val cardLast4: String get() = cardNumber.replace("-", "").takeLast(4)
}
