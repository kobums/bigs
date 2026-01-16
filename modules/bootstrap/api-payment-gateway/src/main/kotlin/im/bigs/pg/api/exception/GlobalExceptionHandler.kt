package im.bigs.pg.api.exception

import im.bigs.pg.external.pg.PgApprovalException
import im.bigs.pg.external.pg.PgAuthenticationException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리기.
 * - PG 관련 예외를 적절한 HTTP 응답으로 변환합니다.
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * PG 인증 실패 (401)
     * - MISSING_API_KEY: API-KEY 헤더 없음
     * - INVALID_API_KEY_FORMAT: API-KEY 포맷 오류 (UUID 형식이 아님)
     * - UNREGISTERED_API_KEY: 미등록 API-KEY
     */
    @ExceptionHandler(PgAuthenticationException::class)
    fun handlePgAuthenticationException(ex: PgAuthenticationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse(
                code = 401,
                errorCode = ex.errorCode.name,
                message = ex.message,
            ),
        )
    }

    /** PG 승인 실패 (422) */
    @ExceptionHandler(PgApprovalException::class)
    fun handlePgApprovalException(ex: PgApprovalException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ErrorResponse(
                code = ex.code,
                errorCode = ex.errorCode,
                message = ex.message,
                referenceId = ex.referenceId,
            ),
        )
    }

    /** 잘못된 요청 (400) */
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                code = 400,
                errorCode = "BAD_REQUEST",
                message = ex.message ?: "Invalid request",
            ),
        )
    }

    /** 상태 오류 (500) */
    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(ex: IllegalStateException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse(
                code = 500,
                errorCode = "INTERNAL_ERROR",
                message = ex.message ?: "Internal server error",
            ),
        )
    }
}

/** API 에러 응답 DTO */
data class ErrorResponse(
    val code: Int,
    val errorCode: String,
    val message: String,
    val referenceId: String? = null,
)
