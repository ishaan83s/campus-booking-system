package backend.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.authentication.BadCredentialsException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ErrorResponse> notFound(ResourceNotFoundException ex, HttpServletRequest request) { return error(HttpStatus.NOT_FOUND, ex, request); }
    @ExceptionHandler({ConflictException.class, backend.professor.exception.SlotOverlapException.class})
    ResponseEntity<ErrorResponse> conflict(RuntimeException ex, HttpServletRequest request) { return error(HttpStatus.CONFLICT, ex, request); }
    @ExceptionHandler({ForbiddenOperationException.class, backend.professor.exception.SlotNotOwnedException.class})
    ResponseEntity<ErrorResponse> forbidden(RuntimeException ex, HttpServletRequest request) { return error(HttpStatus.FORBIDDEN, ex, request); }
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> responseStatus(ResponseStatusException ex, HttpServletRequest request) { return error(HttpStatus.valueOf(ex.getStatusCode().value()), ex, request); }
    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ErrorResponse> unauthorized(BadCredentialsException ex, HttpServletRequest request) { return error(HttpStatus.UNAUTHORIZED, ex, request); }
    @ExceptionHandler({IllegalArgumentException.class, org.springframework.web.bind.MethodArgumentNotValidException.class})
    ResponseEntity<ErrorResponse> badRequest(Exception ex, HttpServletRequest request) { return error(HttpStatus.BAD_REQUEST, ex, request); }
    private ResponseEntity<ErrorResponse> error(HttpStatus status, Exception ex, HttpServletRequest request) { return ResponseEntity.status(status).body(new ErrorResponse(LocalDateTime.now(), status.value(), status.name(), ex.getMessage(), request.getRequestURI())); }
}
