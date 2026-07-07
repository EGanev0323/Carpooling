package bg.tu_sofia.carpooling.common.exception;

import bg.tu_sofia.carpooling.common.api.ProblemDetail;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ProblemDetail> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "Not Found", ex.getMessage(), request, null);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(ConflictException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Conflict", ex.getMessage(), request, null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Unauthorized", ex.getMessage(), request, null);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.FORBIDDEN, "Forbidden", "Access denied", request, null);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.UNPROCESSABLE_ENTITY;
        }
        return buildResponse(status, resolveTitle(status), ex.getMessage(), request, null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ProblemDetail.FieldError> fieldErrors = ex.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    if (error instanceof FieldError fe) {
                        return new ProblemDetail.FieldError(fe.getField(), fe.getDefaultMessage(), fe.getRejectedValue());
                    }
                    return new ProblemDetail.FieldError(error.getObjectName(), error.getDefaultMessage(), null);
                })
                .toList();

        String traceId = UUID.randomUUID().toString();
        ProblemDetail body = ProblemDetail.builder()
                .type("https://carpooling.tu-sofia.bg/errors/validation-failed")
                .title("Validation Failed")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("Request validation failed. See fieldErrors for details.")
                .traceId(traceId)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred. Please try again later.", request, null);
    }

    private ResponseEntity<ProblemDetail> buildResponse(
            HttpStatus status, String title, String detail,
            HttpServletRequest request, List<ProblemDetail.FieldError> fieldErrors) {

        String traceId = UUID.randomUUID().toString();
        ProblemDetail body = ProblemDetail.builder()
                .type("about:blank")
                .title(title)
                .status(status.value())
                .detail(detail)
                .traceId(traceId)
                .path(request.getRequestURI())
                .fieldErrors(fieldErrors)
                .build();

        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }

    private String resolveTitle(HttpStatus status) {
        return switch (status) {
            case TOO_MANY_REQUESTS -> "Too Many Requests";
            case UNPROCESSABLE_ENTITY -> "Unprocessable Entity";
            default -> status.getReasonPhrase();
        };
    }
}
