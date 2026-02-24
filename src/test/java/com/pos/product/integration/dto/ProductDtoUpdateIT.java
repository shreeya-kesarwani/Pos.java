package com.pos.product.integration.dto;

import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.model.form.ProductForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoUpdateIT extends AbstractIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;
    @Autowired TestFactory factory;

    @Test
    void shouldUpdateProduct_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var product = factory.createProduct("b1", "Old", client.getId(), 10.0, null);
        flushAndClear();

        ProductForm form = new ProductForm();
        form.setName("  New  ");
        form.setBarcode("  b1  ");  // same barcode
        form.setMrp(99.0);
        form.setClientId(client.getId());

        productDto.update(product.getId(), form);
        flushAndClear();

        var updated = productDao.selectById(product.getId());
        assertNotNull(updated);
        assertEquals("New", updated.getName());
        assertEquals("b1", updated.getBarcode());
        assertEquals(99.0, updated.getMrp());
        assertEquals(client.getId(), updated.getClientId());
    }
}