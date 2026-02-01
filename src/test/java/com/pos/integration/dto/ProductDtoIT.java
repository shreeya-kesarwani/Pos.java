package com.pos.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.ProductDto;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductDtoIT extends AbstractMySqlIntegrationTest {

    @Autowired
    private ProductDto productDto;

    @Autowired
    private ClientDao clientDao;

    @Test
    void addAndGetProducts_shouldPersistAndReturnPaginated() throws Exception {
        // Arrange: create required client (email is NOT NULL in entity)
        Client client = new Client();
        client.setName("ABC");
        client.setEmail("abc@test.com");
        clientDao.insert(client);

        // Act: add product via DTO
        ProductForm form = new ProductForm();
        form.setClientName("ABC");
        form.setName("Pen");
        form.setBarcode("B1");
        form.setMrp(10.0);
        form.setImageUrl(null);

        productDto.add(form);

        // Assert: fetch via DTO pagination method
        PaginatedResponse<ProductData> resp = productDto.getProducts(
                "Pen",   // name
                "B1",    // barcode
                "ABC",   // clientName
                0,       // page
                10       // size
        );

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertEquals(0, resp.getPageNo());
        assertEquals(1L, resp.getTotalCount());
        assertEquals(1, resp.getData().size());

        ProductData data = resp.getData().get(0);
        assertEquals("Pen", data.getName());
        assertEquals("B1", data.getBarcode());
        assertEquals("ABC", data.getClientName());
        assertEquals(10.0, data.getMrp());
    }

    @Test
    void getProducts_shouldReturnEmpty_whenNoMatch() throws Exception {
        PaginatedResponse<ProductData> resp = productDto.getProducts(
                "DoesNotExist",
                null,
                null,
                0,
                10
        );

        assertNotNull(resp);
        assertNotNull(resp.getData());
        assertEquals(0L, resp.getTotalCount());
        assertTrue(resp.getData().isEmpty());
    }

    @Test
    void add_shouldFail_whenClientDoesNotExist() {
        ProductForm form = new ProductForm();
        form.setClientName("NO_SUCH_CLIENT");
        form.setName("Pen");
        form.setBarcode("B2");
        form.setMrp(10.0);
        form.setImageUrl(null);

        Exception ex = assertThrows(Exception.class, () -> productDto.add(form));
        // Typically should be ApiException with message "Client not found: ..."
        assertTrue(ex.getMessage() == null || ex.getMessage().toLowerCase().contains("client"));
    }
}
