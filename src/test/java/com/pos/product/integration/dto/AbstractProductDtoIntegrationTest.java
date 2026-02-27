package com.pos.product.integration.dto;

import com.pos.setup.AbstractIntegrationTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

public abstract class AbstractProductDtoIntegrationTest extends AbstractIntegrationTest {

    protected MockMultipartFile tsvFile(String filename, String tsvContent) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/tab-separated-values",
                tsvContent.getBytes(StandardCharsets.UTF_8)
        );
    }
}