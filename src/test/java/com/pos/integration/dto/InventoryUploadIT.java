package com.pos.integration.dto;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.dao.ClientDao;
import com.pos.dto.InventoryDto;
import com.pos.dto.ProductDto;
import com.pos.exception.UploadValidationException;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class InventoryUploadIT extends AbstractMySqlIntegrationTest {

    @Autowired private InventoryDto inventoryDto;
    @Autowired private ProductDto productDto;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductApi productApi;
    @Autowired private InventoryApi inventoryApi;

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

    private void addProductViaDto(String clientName, String barcode, String name, double mrp) throws Exception {
        ProductForm f = new ProductForm();
        f.setClientName(clientName);
        f.setBarcode(barcode);
        f.setName(name);
        f.setMrp(mrp);
        f.setImageUrl(null);
        productDto.add(f);
    }

    private int getInventoryQtyByBarcode(String barcode) throws Exception {
        Product p = productApi.getCheckByBarcode(barcode);
        Inventory inv = inventoryApi.getByProductId(p.getId());
        return (inv == null) ? 0 : inv.getQuantity();
    }

    @Test
    void upload_shouldInsert_whenNoExistingInventory() throws Exception {
        // Arrange
        insertClient("ABC", "abc@test.com");
        addProductViaDto("ABC", "I1", "Pen", 10.0);

        String tsv =
                "barcode\tquantity\n" +
                        "I1\t5\n";

        // Act
        inventoryDto.upload(tsvFile("inv.tsv", tsv));

        // Assert
        assertEquals(5, getInventoryQtyByBarcode("I1"));
    }

    @Test
    void upload_shouldIncrement_whenInventoryAlreadyExists() throws Exception {
        // Arrange
        insertClient("ABC", "abc@test.com");
        addProductViaDto("ABC", "I2", "Pencil", 5.0);

        // First upload => 5
        inventoryDto.upload(tsvFile("inv1.tsv",
                "barcode\tquantity\n" +
                        "I2\t5\n"
        ));
        assertEquals(5, getInventoryQtyByBarcode("I2"));

        // Act: second upload => +3
        inventoryDto.upload(tsvFile("inv2.tsv",
                "barcode\tquantity\n" +
                        "I2\t3\n"
        ));

        // Assert
        assertEquals(8, getInventoryQtyByBarcode("I2"));
    }

    @Test
    void upload_shouldThrowUploadValidationException_whenBarcodeUnknown() {
        String tsv =
                "barcode\tquantity\n" +
                        "UNKNOWN\t5\n";

        UploadValidationException ex = assertThrows(
                UploadValidationException.class,
                () -> inventoryDto.upload(tsvFile("bad.tsv", tsv))
        );

        assertNotNull(ex.getFileBytes());
        String errorTsv = asString(ex.getFileBytes());

        // InventoryDto builds: "Line X: <message>"
        assertTrue(
                errorTsv.toLowerCase().contains("not found") ||
                        errorTsv.toLowerCase().contains("barcode"),
                "Expected error TSV to mention not found/barcode, got:\n" + errorTsv
        );

        assertEquals("inventory_upload_errors.tsv", ex.getFilename());
        assertEquals("text/tab-separated-values", ex.getContentType());
    }

    @Test
    void upload_shouldThrowUploadValidationException_whenQuantityInvalid() {
        // Non-numeric quantity triggers: "Invalid quantity"
        String tsv =
                "barcode\tquantity\n" +
                        "I3\tabc\n";

        UploadValidationException ex = assertThrows(
                UploadValidationException.class,
                () -> inventoryDto.upload(tsvFile("bad_qty.tsv", tsv))
        );

        String errorTsv = asString(ex.getFileBytes());
        assertTrue(
                errorTsv.toLowerCase().contains("invalid quantity") ||
                        errorTsv.toLowerCase().contains("quantity"),
                "Expected error TSV to mention quantity, got:\n" + errorTsv
        );
    }
}
