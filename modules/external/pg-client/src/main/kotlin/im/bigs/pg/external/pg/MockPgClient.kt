package im.bigs.pg.external.pg

import im.bigs.pg.application.pg.port.out.PgApproveRequest
import im.bigs.pg.application.pg.port.out.PgApproveResult
import im.bigs.pg.application.pg.port.out.PgClientOutPort
import im.bigs.pg.domain.payment.PaymentStatus
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.random.Random

/**
 * 목업 PG: 모든 승인을 성공으로 처리합니다.
 * - 실제 네트워크 호출은 없으며, 시나리오 이해를 위한 더미 구성입니다.
 * - partnerId=1 (MOCK1) 제휴사 전용
 */
@Component
class MockPgClient : PgClientOutPort {
    override fun supports(partnerId: Long): Boolean = partnerId == 1L

    override fun approve(request: PgApproveRequest): PgApproveResult {
        val dateOfMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("MMdd"))
        val randomDigits = Random.nextInt(9999).toString().padStart(4, '0')
        return PgApproveResult(
            approvalCode = "$dateOfMonth$randomDigits",
            approvedAt = LocalDateTime.now(ZoneOffset.UTC),
            status = PaymentStatus.APPROVED,
        )
    }
}
