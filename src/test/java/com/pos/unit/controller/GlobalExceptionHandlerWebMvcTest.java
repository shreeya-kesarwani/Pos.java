package com.pos.unit.controller;

import com.pos.exception.ApiException;
import com.pos.exception.GlobalExceptionHandler;
import com.pos.exception.UploadValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.ResourceAccessException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(GlobalExceptionHandlerWebMvcTest.ThrowingController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false) // disable Spring Security in this unit test
class GlobalExceptionHandlerWebMvcTest {

    @Autowired
    private MockMvc mvc;

    @RestController
    @RequestMapping("/test")
    static class ThrowingController {

        @GetMapping("/api-ex")
        public String apiEx() throws ApiException {
            throw new ApiException("boom");
        }

        @GetMapping("/network-ex")
        public String networkEx() {
            throw new ResourceAccessException("connection refused");
        }

        @GetMapping("/upload-ex")
        public String uploadEx() {
            byte[] bytes = "barcode\tquantity\terror\nX\t-1\tbad\n".getBytes();

            // ctor order MUST match your UploadValidationException constructor:
            // UploadValidationException(String message, byte[] fileBytes, String filename, String contentType)
            throw new UploadValidationException(
                    "Invalid upload",
                    bytes,
                    "inventory_upload_errors.tsv",
                    "text/tab-separated-values"
            );
        }

        @GetMapping("/not-logged-in")
        public String notLoggedIn() {
            throw new org.springframework.security.authentication.AuthenticationCredentialsNotFoundException("no auth");
        }

        @GetMapping("/forbidden")
        public String forbidden() {
            throw new org.springframework.security.access.AccessDeniedException("nope");
        }

        @PostMapping(
                value = "/validate",
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE
        )
        public String validate(@Valid @RequestBody DemoForm form) {
            return "ok";
        }
    }

    static class DemoForm {
        @NotBlank(message = "name cannot be empty")
        public String name;
    }

    // ---------- tests ----------

    @Test
    void apiException_shouldReturnJsonMessage_whenAcceptJson() throws Exception {
        mvc.perform(get("/test/api-ex").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("boom"));
    }

    @Test
    void apiException_shouldReturnEmptyBody_whenAcceptPdf() throws Exception {
        mvc.perform(get("/test/api-ex").accept(MediaType.APPLICATION_PDF))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void uploadValidationException_shouldReturnAttachment_withHeadersAndBytes() throws Exception {
        mvc.perform(get("/test/upload-ex"))
                .andExpect(status().isBadRequest())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"inventory_upload_errors.tsv\""))
                .andExpect(header().string("Content-Type", "text/tab-separated-values"))
                .andExpect(content().bytes("barcode\tquantity\terror\nX\t-1\tbad\n".getBytes()));
    }

    @Test
    void resourceAccessException_shouldReturn503_andFriendlyMessage() throws Exception {
        mvc.perform(get("/test/network-ex").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void methodArgumentNotValid_shouldReturnFirstFieldErrorMessage_asJson() throws Exception {
        mvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("name cannot be empty"));
    }

    @Test
    void methodArgumentNotValid_shouldReturnEmptyBody_whenAcceptPdf() throws Exception {
        mvc.perform(post("/test/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_PDF)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(""));
    }

    @Test
    void authenticationCredentialsNotFound_shouldReturn401_json() throws Exception {
        mvc.perform(get("/test/not-logged-in").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("You are not logged in"));
    }

    @Test
    void accessDenied_shouldReturn403_json() throws Exception {
        mvc.perform(get("/test/forbidden").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").exists());
    }
}
