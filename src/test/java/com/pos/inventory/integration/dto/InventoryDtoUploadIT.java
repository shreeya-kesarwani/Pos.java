package com.pos.inventory.integration.dto;

import com.pos.dao.InventoryDao;
import com.pos.dao.ProductDao;
import com.pos.dto.InventoryDto;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class InventoryDtoUploadIT extends AbstractIntegrationTest {

    @Autowired InventoryDto inventoryDto;
    @Autowired InventoryDao inventoryDao;
    @Autowired ProductDao productDao;
    @Autowired TestFactory factory;

    @Test
    void shouldUploadInventory_happyFlow() throws Exception {
        // inventory upload requires products to exist (barcode -> productId)
        var client = factory.createClient("Acme", "a@acme.com");
        var p1 = factory.createProduct("b1", "P1", client.getId(), 10.0, null);
        flushAndClear();

        String tsv =
                "barcode\tquantity\n" +
                        "b1\t5\n";

        var file = new MockMultipartFile(
                "file",
                "inventory.tsv",
                "text/tab-separated-values",
                tsv.getBytes(StandardCharsets.UTF_8)
        );

        inventoryDto.upload(file);
        flushAndClear();

        var inv = inventoryDao.selectByProductId(p1.getId());   // adjust if method differs
        assertNotNull(inv);
        assertEquals(p1.getId(), inv.getProductId());
        assertEquals(5, inv.getQuantity());
    }
}