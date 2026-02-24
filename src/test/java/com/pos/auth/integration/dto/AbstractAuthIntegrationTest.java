package com.pos.auth.integration.dto;

import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class AbstractAuthIntegrationTest extends AbstractIntegrationTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }
}