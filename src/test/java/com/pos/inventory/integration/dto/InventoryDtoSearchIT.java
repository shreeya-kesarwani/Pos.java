package com.pos.inventory.integration.dto;

import com.pos.dto.InventoryDto;
import com.pos.model.form.InventorySearchForm;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoSearchIT extends AbstractInventoryDtoIntegrationTest {

    @Autowired InventoryDto inventoryDto;
    @Autowired TestFactory factory;

    @Test
    void shouldGetAllInventory_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var p1 = factory.createProduct("b1", "Prod One", client.getId(), 10.0, null);
        factory.createInventory(p1.getId(), 3);
        flushAndClear();

        var resp = inventoryDto.getAll(searchForm("  b1  ", "  Prod  "));

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(d ->
                d.getBarcode().equals("b1") &&
                        d.getProductName().equals("Prod One") &&
                        d.getQuantity() == 3
        ));
    }

    @Test
    void shouldReturnEmpty_whenNoMatches() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        var p1 = factory.createProduct("b1", "P1", client.getId(), 10.0, null);
        factory.createInventory(p1.getId(), 3);
        flushAndClear();

        var resp = inventoryDto.getAll(searchForm("no-such-barcode", "no-such-name"));

        assertNotNull(resp);
        assertEquals(0, resp.getData().size());
        assertEquals(0, resp.getTotalCount());
    }
}