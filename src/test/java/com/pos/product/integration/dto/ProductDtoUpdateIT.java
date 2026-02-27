package com.pos.product.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.model.form.ProductForm;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoUpdateIT extends AbstractProductDtoIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;
    @Autowired ClientDao clientDao;

    private ProductForm productForm(String name, String barcode, double mrp, Integer clientId) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setBarcode(barcode);
        form.setMrp(mrp);
        form.setClientId(clientId);
        return form;
    }

    @Test
    void shouldUpdateProduct_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var product = TestEntities.newProduct("b1", "Old", client.getId(), 10.0, null);
        productDao.insert(product);
        flushAndClear();

        ProductForm form = productForm("  New  ", "  b1  ", 99.0, client.getId());

        productDto.update(product.getId(), form);
        flushAndClear();

        var updated = productDao.selectById(product.getId());
        assertNotNull(updated);
        assertEquals("New", updated.getName());
        assertEquals("b1", updated.getBarcode());
        assertEquals(99.0, updated.getMrp());
        assertEquals(client.getId(), updated.getClientId());
    }

    @Test
    void shouldThrowWhenUpdatingToExistingBarcode() throws Exception {
        var client = TestEntities.newClient("AcmeU", "u@acme.com");
        clientDao.insert(client);

        var p1 = TestEntities.newProduct("b1", "P1", client.getId(), 10.0, null);
        productDao.insert(p1);

        productDao.insert(TestEntities.newProduct("b2", "P2", client.getId(), 20.0, null));
        flushAndClear();

        ProductForm form = productForm("P1 New", "b2", 15.0, client.getId());

        assertThrows(ApiException.class, () -> productDto.update(p1.getId(), form));
    }
}