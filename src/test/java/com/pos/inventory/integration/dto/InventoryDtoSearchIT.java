package com.pos.inventory.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.dto.InventoryDto;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoSearchIT extends AbstractInventoryDtoIntegrationTest {

    @Autowired InventoryDto inventoryDto;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;
    @Autowired private InventoryDao inventoryDao;

    @Test
    void shouldGetAllInventory_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var p1 = TestEntities.newProduct("b1", "Prod One", client.getId(), 10.0, null);
        productDao.insert(p1);

        inventoryDao.insert(TestEntities.newInventory(p1.getId(), 3));
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
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var p1 = TestEntities.newProduct("b1", "P1", client.getId(), 10.0, null);
        productDao.insert(p1);

        inventoryDao.insert(TestEntities.newInventory(p1.getId(), 3));
        flushAndClear();

        var resp = inventoryDto.getAll(searchForm("no-such-barcode", "no-such-name"));

        assertNotNull(resp);
        assertEquals(0, resp.getData().size());
        assertEquals(0, resp.getTotalCount());
    }
}