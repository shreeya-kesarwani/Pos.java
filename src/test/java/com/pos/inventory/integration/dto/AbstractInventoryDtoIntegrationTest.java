package com.pos.inventory.integration.dto;

import com.pos.model.form.InventorySearchForm;
import com.pos.setup.AbstractIntegrationTest;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.charset.StandardCharsets;

public abstract class AbstractInventoryDtoIntegrationTest extends AbstractIntegrationTest {

    protected MockMultipartFile inventoryTsv(String content) {
        return new MockMultipartFile(
                "file",
                "inventory.tsv",
                "text/tab-separated-values",
                content.getBytes(StandardCharsets.UTF_8)
        );
    }

    protected InventorySearchForm searchForm(String barcode, String name) {
        InventorySearchForm form = new InventorySearchForm();
        form.setBarcode(barcode);
        form.setProductName(name);
        form.setPageNumber(0);
        form.setPageSize(10);
        return form;
    }
}