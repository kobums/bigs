package im.bigs.pg.infra.persistence.payment.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

/**
 * DB용 결제 이력 엔티티.
 * - createdAt/Id 조합을 커서 정렬 키로 사용합니다.
 */
@Entity
@Table(name = "payment")
class PaymentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var partnerId: Long = 0L,
    @Column(nullable = false, precision = 15, scale = 0)
    var amount: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, precision = 10, scale = 6)
    var appliedFeeRate: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, precision = 15, scale = 0)
    var feeAmount: BigDecimal = BigDecimal.ZERO,
    @Column(nullable = false, precision = 15, scale = 0)
    var netAmount: BigDecimal = BigDecimal.ZERO,
    @Column(length = 8)
    var cardBin: String? = null,
    @Column(length = 4)
    var cardLast4: String? = null,
    @Column(nullable = false, length = 32)
    var approvalCode: String = "",
    @Column(nullable = false)
    var approvedAt: Instant = Instant.EPOCH,
    @Column(nullable = false, length = 20)
    var status: String = "",
    @Column(nullable = false)
    var createdAt: Instant = Instant.EPOCH,
    @Column(nullable = false)
    var updatedAt: Instant = Instant.EPOCH,
)
