package im.bigs.pg.infra.persistence.partner.adapter

import im.bigs.pg.application.partner.port.out.FeePolicyOutPort
import im.bigs.pg.domain.partner.FeePolicy
import im.bigs.pg.infra.persistence.partner.repository.FeePolicyJpaRepository
import org.springframework.stereotype.Component
import java.time.ZoneOffset

/** 수수료 정책 조회 어댑터. */
@Component
class FeePolicyPersistenceAdapter(
    private val repo: FeePolicyJpaRepository,
) : FeePolicyOutPort {
    override fun findEffectivePolicy(partnerId: Long, at: java.time.LocalDateTime): FeePolicy? =
        repo.findTop1ByPartnerIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(partnerId, at.toInstant(ZoneOffset.UTC))?.let {
            FeePolicy(
                id = it.id,
                partnerId = it.partnerId,
                effectiveFrom = java.time.LocalDateTime.ofInstant(it.effectiveFrom, ZoneOffset.UTC),
                percentage = it.percentage,
                fixedFee = it.fixedFee,
            )
        }
}
