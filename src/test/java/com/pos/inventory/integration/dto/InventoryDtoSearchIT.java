package com.pos.inventory.integration.dto;

import com.pos.dto.InventoryDto;
import com.pos.model.form.InventorySearchForm;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoSearchIT extends AbstractIntegrationTest {

    @Autowired InventoryDto inventoryDto;
    @Autowired TestFactory factory;

    @Test
    void shouldGetAllInventory_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var p1 = factory.createProduct("b1", "Prod One", client.getId(), 10.0, null);
        factory.createInventory(p1.getId(), 3);
        flushAndClear();

        InventorySearchForm form = new InventorySearchForm();
        form.setBarcode("  b1  ");
        form.setProductName("  Prod  ");
        form.setPageNumber(0);
        form.setPageSize(10);

        var resp = inventoryDto.getAll(form);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(d ->
                "b1".equals(d.getBarcode()) &&
                        "Prod One".equals(d.getProductName()) &&
                        d.getQuantity() == 3
        ));
    }
}