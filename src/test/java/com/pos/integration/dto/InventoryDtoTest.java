package com.pos.integration.dto;

import com.pos.api.InventoryApi;
import com.pos.api.ProductApi;
import com.pos.dto.InventoryDto;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import com.pos.utils.InventoryTsvParser;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

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
    void upload_shouldReturn_whenParserReturnsEmpty() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "i.tsv", "text/tab-separated-values",
                "".getBytes(StandardCharsets.UTF_8));

        try (MockedStatic<InventoryTsvParser> mocked = mockStatic(InventoryTsvParser.class)) {
            mocked.when(() -> InventoryTsvParser.parse(any(MultipartFile.class))).thenReturn(List.of());
            inventoryDto.upload(file);
            verifyNoInteractions(inventoryApi, productApi);
        }
    }

    @Test
    void upload_shouldParseConvertAndCallApiAdd() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "i.tsv", "text/tab-separated-values",
                "x".getBytes(StandardCharsets.UTF_8));

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
             MockedStatic<InventoryConversion> conv = mockStatic(InventoryConversion.class);
             MockedStatic<ProductApi> productApiStatic = mockStatic(ProductApi.class)) {

            parser.when(() -> InventoryTsvParser.parse(any(MultipartFile.class))).thenReturn(List.of(f));
            when(productApi.getCheckByBarcodes(eq(List.of("b1")))).thenReturn(List.of(p));
            productApiStatic.when(() -> ProductApi.toProductIdByBarcode(anyList())).thenReturn(Map.of("b1", 10));
            conv.when(() -> InventoryConversion.convertFormsToPojos(anyList(), anyMap())).thenReturn(List.of(inv));

            inventoryDto.upload(file);
            verify(inventoryApi).add(eq(List.of(inv)));
        }
    }

    @Test
    void getAll_shouldNormalizeAndReturnPaginatedResponse() throws Exception {
        Inventory i = new Inventory();
        i.setProductId(1);
        i.setQuantity(3);

        Product p = new Product();
        p.setId(1);
        p.setName("P");
        p.setBarcode("b");

        when(inventoryFlow.searchInventories(eq("b"), eq("name"), eq(2), eq(20))).thenReturn(List.of(i));
        when(inventoryFlow.getSearchCount(eq("b"), eq("name"))).thenReturn(1L);
        when(productApi.getByIds(eq(List.of(1)))).thenReturn(List.of(p));

        PaginatedResponse<InventoryData> resp = inventoryDto.getAll("  b ", "  name ", 2, 20);
        assertEquals(1L, resp.getTotalCount());
        assertEquals(2, resp.getPageNo());
        assertEquals(1, resp.getData().size());
        assertEquals("b", resp.getData().get(0).getBarcode());
        assertEquals("P", resp.getData().get(0).getProductName());
    }
}
