package im.bigs.pg.domain.calculation

import org.junit.jupiter.api.DisplayName
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class FeeCalculatorTest {
    @Test
    @DisplayName("퍼센트 수수료만 적용 시 반올림 및 정산금이 정확해야 한다")
    fun `should calculate fee with percentage only`() {
        val amount = BigDecimal("10000")
        val rate = BigDecimal("0.0235")
        val (fee, net) = FeeCalculator.calculateFee(amount, rate, null)
        assertEquals(BigDecimal("235"), fee)
        assertEquals(BigDecimal("9765"), net)
    }

    @Test
    @DisplayName("퍼센트+정액 수수료가 함께 적용되어야 한다")
    fun `should calculate fee with percentage and fixed fee`() {
        val amount = BigDecimal("10000")
        val rate = BigDecimal("0.0300")
        val fixed = BigDecimal("100")
        val (fee, net) = FeeCalculator.calculateFee(amount, rate, fixed)
        assertEquals(BigDecimal("400"), fee)
        assertEquals(BigDecimal("9600"), net)
    }
}
