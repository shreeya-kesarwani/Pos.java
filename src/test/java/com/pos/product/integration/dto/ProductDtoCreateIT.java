package com.pos.product.integration.dto;

import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.model.form.ProductForm;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoCreateIT extends AbstractProductDtoIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;

    private ProductForm productForm(String name, String barcode, double mrp, Integer clientId) {
        ProductForm form = new ProductForm();
        form.setName(name);
        form.setBarcode(barcode);
        form.setMrp(mrp);
        form.setClientId(clientId);
        return form;
    }

    @Test
    void shouldCreateProduct_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        flushAndClear();

        ProductForm form = productForm("  iPhone  ", "  b1  ", 100.0, client.getId());

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
        form.setName(null); // Bean validation should fail

        assertThrows(ApiException.class, () -> productDto.add(form));
    }

    @Test
    void shouldThrowWhenBarcodeAlreadyExists() throws Exception {
        var client = factory.createClient("AcmeDup", "dup@acme.com");
        // seed an existing product with barcode b1
        factory.createProduct("b1", "Existing", client.getId(), 10.0, null);
        flushAndClear();

        ProductForm form = productForm("New Product", "b1", 20.0, client.getId());

        assertThrows(ApiException.class, () -> productDto.add(form));
    }
}