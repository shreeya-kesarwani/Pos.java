package com.pos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UploadValidationException.class)
    public ResponseEntity<byte[]> handleUploadValidation(UploadValidationException exception) {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(exception.getContentType()));
        headers.set(
                HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + exception.getFilename() + "\""
        );

        return new ResponseEntity<>(
                exception.getFileBytes(),
                headers,
                HttpStatus.BAD_REQUEST
        );
    }

    // 2) Business Errors
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(ApiException exception, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }

    // 3) Bean Validation Errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String msg = "Validation failed";
        if (exception.getBindingResult().getFieldError() != null) {
            msg = exception.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    // 4) Invoice service down
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleNetworkError(ResourceAccessException exception, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message",
                        "The Invoice Service is unreachable. Ensure it is running on port 8081."
                ));
    }

    // 5) IO errors (file upload)
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIo(IOException ex, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid file upload"));
    }

    // 6) Security: Not logged in
    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<?> handleNotLoggedIn(AuthenticationCredentialsNotFoundException ex, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "You are not logged in"));
    }

    // 7) Security: Forbidden
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleForbidden(AccessDeniedException ex, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of("message", "Only supervisors can upload or edit master data."));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<?> handleNoResource(NoResourceFoundException ex, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "No handler found for this path."));
    }

    // 8) Catch-all safety net
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(Exception exception, HttpServletRequest request) {

        exception.printStackTrace();

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected system error occurred."));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Upload failed: duplicate or invalid data."));
    }

    private boolean expectsPdf(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/pdf");
    }
}
