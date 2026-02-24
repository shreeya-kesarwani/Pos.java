package com.pos.product.integration.dto;

import com.pos.dto.ProductDto;
import com.pos.model.form.ProductSearchForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoSearchIT extends AbstractProductDtoIntegrationTest {

    @Autowired ProductDto productDto;

    @Test
    void shouldSearchProducts_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        factory.createProduct("barcode-1", "Phone", client.getId(), 10.0, null);
        factory.createProduct("barcode-2", "Laptop", client.getId(), 20.0, null);
        flushAndClear();

        ProductSearchForm form = new ProductSearchForm();
        form.setName("  ph  "); // should match "Phone"
        form.setBarcode(null);
        form.setClientId(client.getId());
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = productDto.getProducts(form);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(p -> p.getName().equalsIgnoreCase("Phone")));
    }
}