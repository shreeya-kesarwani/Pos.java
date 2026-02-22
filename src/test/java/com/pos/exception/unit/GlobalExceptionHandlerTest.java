package com.pos.exception.unit;

import com.pos.exception.ApiException;
import com.pos.exception.GlobalExceptionHandler;
import com.pos.exception.UploadValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.mock.web.MockHttpServletRequest;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static MockHttpServletRequest reqWithAccept(String accept) {
        MockHttpServletRequest req = new MockHttpServletRequest();
        if (accept != null) req.addHeader("Accept", accept);
        return req;
    }

    @Test
    void handleUploadValidation_shouldReturnAttachmentBytesAndHeaders() {
        byte[] bytes = "bad".getBytes();
        UploadValidationException ex = new UploadValidationException(
                "TSV has errors", bytes, "errors.tsv", "text/tab-separated-values"
        );

        ResponseEntity<byte[]> res = handler.handleUploadValidation(ex);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertArrayEquals(bytes, res.getBody());

        assertNotNull(res.getHeaders().getContentType());
        assertEquals("text/tab-separated-values", res.getHeaders().getContentType().toString());

        String cd = res.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertNotNull(cd);
        assertTrue(cd.contains("attachment"));
        assertTrue(cd.contains("errors.tsv"));
    }

    @Test
    void handleApiException_shouldReturnJsonMessage_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        ApiException ex = new ApiException("nope");

        ResponseEntity<?> res = handler.handleApiException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertTrue(res.getBody() instanceof Map);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("nope", body.get("message"));
    }

    @Test
    void handleApiException_shouldReturnEmptyBody_whenPdfExpected() {
        MockHttpServletRequest req = reqWithAccept("application/pdf");
        ApiException ex = new ApiException("nope");

        ResponseEntity<?> res = handler.handleApiException(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        assertNull(res.getBody());
    }

    @Test
    void handleValidation_shouldReturnFirstFieldErrorMessage_whenNotPdf() throws Exception {
        MockHttpServletRequest req = reqWithAccept("application/json");

        // Build BindingResult with a FieldError
        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "target");
        br.addError(new FieldError("target", "field", "field is required"));

        // Need a MethodParameter for MethodArgumentNotValidException
        Method m = Dummy.class.getDeclaredMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter mp = new org.springframework.core.MethodParameter(m, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);

        ResponseEntity<?> res = handler.handleValidation(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("field is required", body.get("message"));
    }

    @Test
    void handleValidation_shouldReturnGenericMessage_whenNoFieldErrors() throws Exception {
        MockHttpServletRequest req = reqWithAccept("application/json");

        Object target = new Object();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(target, "target");
        // no field errors

        Method m = Dummy.class.getDeclaredMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter mp = new org.springframework.core.MethodParameter(m, 0);

        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(mp, br);

        ResponseEntity<?> res = handler.handleValidation(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("Validation failed", body.get("message"));
    }

    @Test
    void handleNetworkError_shouldReturn503Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        ResourceAccessException ex = new ResourceAccessException("down");

        ResponseEntity<?> res = handler.handleNetworkError(ex, req);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, res.getStatusCode());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertNotNull(body.get("message"));
    }

    @Test
    void handleNetworkError_shouldReturn503Empty_whenPdf() {
        MockHttpServletRequest req = reqWithAccept("application/pdf");
        ResourceAccessException ex = new ResourceAccessException("down");

        ResponseEntity<?> res = handler.handleNetworkError(ex, req);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, res.getStatusCode());
        assertNull(res.getBody());
    }

    @Test
    void handleIo_shouldReturn400Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        IOException ex = new IOException("bad");

        ResponseEntity<?> res = handler.handleIo(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("Invalid file upload", body.get("message"));
    }

    @Test
    void handleNotLoggedIn_shouldReturn401Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        AuthenticationCredentialsNotFoundException ex = new AuthenticationCredentialsNotFoundException("no auth");

        ResponseEntity<?> res = handler.handleNotLoggedIn(ex, req);

        assertEquals(HttpStatus.UNAUTHORIZED, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("You are not logged in", body.get("message"));
    }

    @Test
    void handleForbidden_shouldReturn403Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        AccessDeniedException ex = new AccessDeniedException("no");

        ResponseEntity<?> res = handler.handleForbidden(ex, req);

        assertEquals(HttpStatus.FORBIDDEN, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("Only supervisors can upload or edit master data.", body.get("message"));
    }

    @Test
    void handleNoResource_shouldReturn404Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");

        // We can just mock it because handler doesn't use the exception fields.
        NoResourceFoundException ex = mock(NoResourceFoundException.class);

        ResponseEntity<?> res = handler.handleNoResource(ex, req);

        assertEquals(HttpStatus.NOT_FOUND, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("No handler found for this path.", body.get("message"));
    }

    @Test
    void handleDataIntegrity_shouldReturn400Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        DataIntegrityViolationException ex = new DataIntegrityViolationException("dup");

        ResponseEntity<?> res = handler.handleDataIntegrity(ex, req);

        assertEquals(HttpStatus.BAD_REQUEST, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("Upload failed: duplicate or invalid data.", body.get("message"));
    }

    @Test
    void handleGeneral_shouldReturn500Json_whenNotPdf() {
        MockHttpServletRequest req = reqWithAccept("application/json");
        Exception ex = new Exception("boom");

        ResponseEntity<?> res = handler.handleGeneral(ex, req);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) res.getBody();
        assertEquals("An unexpected system error occurred.", body.get("message"));
    }

    @Test
    void handleGeneral_shouldReturn500Empty_whenPdf() {
        MockHttpServletRequest req = reqWithAccept("application/pdf");
        Exception ex = new Exception("boom");

        ResponseEntity<?> res = handler.handleGeneral(ex, req);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, res.getStatusCode());
        assertNull(res.getBody());
    }

    private static final class Dummy {
        @SuppressWarnings("unused")
        private void dummyMethod(String x) {
            // used only to create MethodParameter
        }
    }
}
