package com.pos.product.integration.dto;

import com.pos.dao.ProductDao;
import com.pos.dto.ProductDto;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDtoBulkUploadIT extends AbstractIntegrationTest {

    @Autowired ProductDto productDto;
    @Autowired ProductDao productDao;
    @Autowired TestFactory factory;

    @Test
    void shouldBulkUploadProducts_happyFlow() throws Exception {
        var client = factory.createClient("Acme", "a@acme.com");
        flushAndClear();

        String tsv =
                "barcode\tname\tmrp\timageurl\n" +
                        "b1\tA\t1.0\thttps://example.com/a.png\n" +
                        "b2\tB\t2.0\t\n";

        var file = new MockMultipartFile(
                "file",
                "products.tsv",
                "text/tab-separated-values",
                tsv.getBytes(StandardCharsets.UTF_8)
        );

        productDto.addBulk(client.getId(), file);
        flushAndClear();

        var list = productDao.selectByBarcodes(List.of("b1", "b2"));
        assertEquals(2, list.size());
        assertTrue(list.stream().anyMatch(p -> p.getBarcode().equals("b1") && p.getName().equals("A")));
        assertTrue(list.stream().anyMatch(p -> p.getBarcode().equals("b2") && p.getName().equals("B")));
    }
}