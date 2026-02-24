package com.pos.product.integration.dto;

import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoBulkUploadIT extends AbstractProductDtoIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;

    @Test
    void shouldBulkUploadProducts_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        flushAndClear();

        String tsv =
                "barcode\tname\tmrp\timageurl\n" +
                        "b1\tA\t1.0\thttps://example.com/a.png\n" +
                        "b2\tB\t2.0\t\n";

        productDto.addBulk(client.getId(), tsvFile("products.tsv", tsv));
        flushAndClear();

        var list = productDao.selectByBarcodes(List.of("b1", "b2"));
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(p -> p.getBarcode().equals("b1") && p.getName().equals("A")));
        assertTrue(list.stream().anyMatch(p -> p.getBarcode().equals("b2") && p.getName().equals("B")));
    }

    @Test
    void shouldReturn_whenBulkUploadFileHasNoRows() throws Exception {
        var client = factory.createClient("Acme2", "a2@acme.com");
        flushAndClear();

        String tsv = "barcode\tname\tmrp\timageurl\n"; // header only

        assertDoesNotThrow(() -> productDto.addBulk(client.getId(), tsvFile("empty.tsv", tsv)));
    }
}