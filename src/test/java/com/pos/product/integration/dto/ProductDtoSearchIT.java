package com.pos.product.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.model.form.ProductSearchForm;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoSearchIT extends AbstractProductDtoIntegrationTest {

    @Autowired ProductDto productDto;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;

    @Test
    void shouldSearchProducts_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        productDao.insert(TestEntities.newProduct("barcode-1", "Phone", client.getId(), 10.0, null));
        productDao.insert(TestEntities.newProduct("barcode-2", "Laptop", client.getId(), 20.0, null));
        flushAndClear();

        ProductSearchForm form = new ProductSearchForm();
        form.setName("  ph  ");
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