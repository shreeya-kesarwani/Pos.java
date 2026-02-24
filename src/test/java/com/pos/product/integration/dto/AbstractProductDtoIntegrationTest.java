package com.pos.product.integration.dto;

import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

public abstract class AbstractProductDtoIntegrationTest extends AbstractIntegrationTest {

    @Autowired protected TestFactory factory;

    protected MockMultipartFile tsvFile(String filename, String tsvContent) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/tab-separated-values",
                tsvContent.getBytes(StandardCharsets.UTF_8)
        );
    }
}