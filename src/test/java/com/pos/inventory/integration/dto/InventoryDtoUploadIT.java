package com.pos.inventory.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.dto.InventoryDto;
import com.pos.exception.ApiException;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoUploadIT extends AbstractInventoryDtoIntegrationTest {

    @Autowired InventoryDto inventoryDto;
    @Autowired InventoryDao inventoryDao;

    @Autowired private ClientDao clientDao;
    @Autowired private ProductDao productDao;

    @Test
    void shouldUploadInventory_happyFlow() throws Exception {
        var client = TestEntities.newClient("Acme", "a@acme.com");
        clientDao.insert(client);

        var p1 = TestEntities.newProduct("b1", "P1", client.getId(), 10.0, null);
        productDao.insert(p1);
        flushAndClear();

        String tsv =
                "barcode\tquantity\n" +
                        "b1\t5\n";

        inventoryDto.upload(inventoryTsv(tsv));
        flushAndClear();

        var inv = inventoryDao.selectByProductId(p1.getId());

        assertNotNull(inv);
        assertEquals(p1.getId(), inv.getProductId());
        assertEquals(5, inv.getQuantity());
    }

    @Test
    void shouldReturn_whenNoRows() throws Exception {
        String tsv = "barcode\tquantity\n";

        assertDoesNotThrow(() -> inventoryDto.upload(inventoryTsv(tsv)));
    }

    @Test
    void shouldThrow_whenBarcodeNotFound() {
        String tsv =
                "barcode\tquantity\n" +
                        "b-missing\t5\n";

        assertThrows(ApiException.class,
                () -> inventoryDto.upload(inventoryTsv(tsv)));
    }
}