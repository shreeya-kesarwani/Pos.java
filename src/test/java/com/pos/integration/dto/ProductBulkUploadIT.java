package com.pos.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.ProductDto;
import com.pos.exception.UploadValidationException;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product TSV bulk upload.
 *
 * ProductDto.addBulkFromTsv() behavior (as per your code):
 * - reads TSV, validates header
 * - validates each row; collects row-wise errors
 * - if ANY error -> throws UploadValidationException with error TSV bytes
 * - if NO errors -> productFlow.addBulk(...) persists
 */
@SpringBootTest
@ActiveProfiles("test")
class ProductBulkUploadIT extends AbstractMySqlIntegrationTest {

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDao clientDao;

    private static MockMultipartFile tsvFile(String filename, String tsv) {
        return new MockMultipartFile(
                "file",
                filename,
                "text/tab-separated-values",
                tsv.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String asString(byte[] bytes) {
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private void insertClient(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        clientDao.insert(c);
    }

    @Test
    void addBulkFromTsv_shouldInsertAll_whenValid() throws Exception {
        // Arrange
        insertClient("ABC", "abc@test.com");

        String tsv =
                "barcode\tclientname\tname\tmrp\timageurl\n" +
                        "BULK1\tABC\tPen\t10\t\n" +
                        "BULK2\tABC\tPencil\t5\thttp://img\n";

        MockMultipartFile file = tsvFile("products.tsv", tsv);

        // Act
        productDto.addBulkFromTsv(file);

        // Assert via paginated fetch
        PaginatedResponse<ProductData> resp = productDto.getProducts(
                null,     // name filter
                null,     // barcode filter
                "ABC",    // client filter
                0,
                50
        );

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertEquals(2L, resp.getTotalCount());
        assertEquals(2, resp.getData().size());

        assertTrue(resp.getData().stream().anyMatch(p -> "BULK1".equals(p.getBarcode())));
        assertTrue(resp.getData().stream().anyMatch(p -> "BULK2".equals(p.getBarcode())));
    }

    @Test
    void addBulkFromTsv_shouldThrowUploadValidationException_withErrorTsv_whenRowInvalid() {
        // Arrange: client exists, but one row has missing mrp (your code throws "mrp is required")
        insertClient("ABC", "abc@test.com");

        String tsv =
                "barcode\tclientname\tname\tmrp\timageurl\n" +
                        "BULK3\tABC\tPen\t\t\n"; // mrp missing

        MockMultipartFile file = tsvFile("bad_products.tsv", tsv);

        // Act + Assert
        UploadValidationException ex = assertThrows(
                UploadValidationException.class,
                () -> productDto.addBulkFromTsv(file)
        );

        assertNotNull(ex.getFileBytes());
        String errorTsv = asString(ex.getFileBytes());

        // Error TSV should contain the line-level error message
        assertTrue(errorTsv.contains("mrp is required") || errorTsv.toLowerCase().contains("mrp"));
        assertNotNull(ex.getFilename());
        assertTrue(ex.getFilename().endsWith(".tsv"));
        assertEquals("text/tab-separated-values", ex.getContentType());
    }

    @Test
    void addBulkFromTsv_shouldThrowUploadValidationException_whenDuplicateBarcodeInFile() {
        // Arrange
        insertClient("ABC", "abc@test.com");

        String tsv =
                "barcode\tclientname\tname\tmrp\timageurl\n" +
                        "DUP1\tABC\tPen\t10\t\n" +
                        "DUP1\tABC\tPen2\t12\t\n"; // duplicate barcode in same file

        MockMultipartFile file = tsvFile("dup_products.tsv", tsv);

        // Act + Assert
        UploadValidationException ex = assertThrows(
                UploadValidationException.class,
                () -> productDto.addBulkFromTsv(file)
        );

        String errorTsv = asString(ex.getFileBytes());
        assertTrue(errorTsv.contains("Duplicate barcode in file: DUP1"));
    }
}
