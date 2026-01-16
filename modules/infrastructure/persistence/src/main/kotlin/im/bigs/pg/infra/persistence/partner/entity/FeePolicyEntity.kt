package im.bigs.pg.infra.persistence.partner.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.math.BigDecimal
import java.time.Instant

/**
 * DB용 수수료 정책 엔티티.
 * - 유효 시작 시점(effectiveFrom) 기준으로 최신 정책을 조회합니다.
 */
@Entity
@Table(name = "partner_fee_policy")
class FeePolicyEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var partnerId: Long = 0L,
    @Column(nullable = false)
    var effectiveFrom: Instant = Instant.EPOCH,
    @Column(nullable = false, precision = 10, scale = 6)
    var percentage: BigDecimal = BigDecimal.ZERO,
    @Column(precision = 15, scale = 0)
    var fixedFee: BigDecimal? = null,
)
