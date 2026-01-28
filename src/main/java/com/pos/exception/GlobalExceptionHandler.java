package com.pos.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;

import java.io.IOException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1️⃣ TSV Upload Validation Errors (MOST SPECIFIC — MUST BE FIRST)
     */
    @ExceptionHandler(UploadValidationException.class)
    public ResponseEntity<byte[]> handleUploadValidation(
            UploadValidationException exception) {

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

    /**
     * 2️⃣ Business Errors
     */
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", exception.getMessage()));
    }

    /**
     * 3️⃣ Bean Validation Errors (@Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String msg = "Validation failed";
        if (exception.getBindingResult().getFieldError() != null) {
            msg = exception.getBindingResult()
                    .getFieldErrors()
                    .get(0)
                    .getDefaultMessage();
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", msg));
    }

    /**
     * 4️⃣ Invoice service down
     */
    @ExceptionHandler(ResourceAccessException.class)
    public ResponseEntity<?> handleNetworkError(
            ResourceAccessException exception,
            HttpServletRequest request) {

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).build();
        }

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "message",
                        "The Invoice Service is unreachable. Ensure it is running on port 8081."
                ));
    }

    /**
     * 5️⃣ IO errors (file upload)
     */
    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIo(IOException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid file upload"));
    }

    /**
     * 6️⃣ Catch-all safety net
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneral(
            Exception exception,
            HttpServletRequest request) {

        exception.printStackTrace();

        if (expectsPdf(request)) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "An unexpected system error occurred."));
    }

    // Helper
    private boolean expectsPdf(HttpServletRequest request) {
        String accept = request.getHeader("Accept");
        return accept != null && accept.contains("application/pdf");
    }
}
