package com.pos.product.integration.dto;

import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.model.form.ProductForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoCreateIT extends AbstractIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;
    @Autowired TestFactory factory;

    @Test
    void shouldCreateProduct_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        flushAndClear();

        ProductForm form = new ProductForm();
        form.setName("  iPhone  ");
        form.setBarcode("  b1  ");
        form.setMrp(100.0);
        form.setClientId(client.getId());

        productDto.add(form);
        flushAndClear();

        var savedList = productDao.selectByBarcodes(java.util.List.of("b1"));
        assertEquals(1, savedList.size());
        var saved = savedList.get(0);

        assertEquals("iPhone", saved.getName());
        assertEquals("b1", saved.getBarcode());
        assertEquals(100.0, saved.getMrp());
        assertEquals(client.getId(), saved.getClientId());
    }

    @Test
    void shouldThrowWhenProductFormInvalid() {
        ProductForm form = new ProductForm();
        form.setName(null); // assuming @NotBlank

        assertThrows(ApiException.class, () -> productDto.add(form));
    }
}