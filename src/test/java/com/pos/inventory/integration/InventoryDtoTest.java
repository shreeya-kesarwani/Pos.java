package com.pos.inventory.integration;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.dto.InventoryDto;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventoryForm;
import com.pos.model.form.InventorySearchForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import com.pos.utils.InventoryTsvParser;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryDtoTest {

    @Mock private InventoryFlow inventoryFlow;
    @Mock private InventoryApi inventoryApi;
    @Mock private ProductApi productApi;
    @Mock private Validator validator;

    @InjectMocks private InventoryDto inventoryDto;

    @Test
    void uploadReturnsWhenParserReturnsEmpty() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "i.tsv",
                "text/tab-separated-values",
                "".getBytes(StandardCharsets.UTF_8)
        );

        try (MockedStatic<InventoryTsvParser> mocked = mockStatic(InventoryTsvParser.class)) {
            mocked.when(() -> InventoryTsvParser.parse(any(MultipartFile.class))).thenReturn(List.of());

            inventoryDto.upload(file);

            verifyNoInteractions(inventoryApi, productApi);
        }
    }

    @Test
    void uploadParsesConvertsAndCallsApiAdd() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "i.tsv",
                "text/tab-separated-values",
                "x".getBytes(StandardCharsets.UTF_8)
        );

        InventoryForm f = new InventoryForm();
        f.setBarcode("  b1  ");
        f.setQuantity(5);

        Product p = new Product();
        p.setId(10);
        p.setBarcode("b1");

        Inventory inv = new Inventory();
        inv.setProductId(10);
        inv.setQuantity(5);

        try (MockedStatic<InventoryTsvParser> parser = mockStatic(InventoryTsvParser.class);
             MockedStatic<InventoryConversion> conv = mockStatic(InventoryConversion.class)) {

            parser.when(() -> InventoryTsvParser.parse(any(MultipartFile.class))).thenReturn(List.of(f));

            // keep this ONLY if InventoryDto.upload calls productApi.getCheckByBarcodes(...)
            when(productApi.getCheckByBarcodes(eq(List.of("b1")))).thenReturn(List.of(p));

            conv.when(() -> InventoryConversion.convertFormsToPojos(anyList(), anyMap()))
                    .thenReturn(List.of(inv));

            inventoryDto.upload(file);

            verify(inventoryApi).add(eq(List.of(inv)));
        }
    }

    @Test
    void getAllNormalizesAndReturnsPaginatedResponse() throws Exception {
        Inventory i = new Inventory();
        i.setProductId(1);
        i.setQuantity(3);

        Product p = new Product();
        p.setId(1);
        p.setName("P");
        p.setBarcode("b");

        InventorySearchForm form = new InventorySearchForm();
        form.setBarcode("  b ");
        form.setProductName("  name ");
        form.setPageNumber(2);
        form.setPageSize(20);

        when(inventoryFlow.searchInventories(eq("b"), eq("name"), eq(2), eq(20))).thenReturn(List.of(i));
        when(inventoryFlow.getSearchCount(eq("b"), eq("name"))).thenReturn(1L);
        when(productApi.getByIds(eq(List.of(1)))).thenReturn(List.of(p));

        PaginatedResponse<InventoryData> resp = inventoryDto.getAll(form);

        assertEquals(1L, resp.getTotalCount());
        assertEquals(2, resp.getPageNo());
        assertEquals(1, resp.getData().size());
        assertEquals("b", resp.getData().get(0).getBarcode());
        assertEquals("P", resp.getData().get(0).getProductName());
    }
}