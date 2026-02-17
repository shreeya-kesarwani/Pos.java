package com.pos.integration.dto;

import com.pos.api.ProductApi;
import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.ProductTsvParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductDtoTest {

    @Mock private ProductFlow productFlow;
    @Mock private ProductApi productApi;
    @Mock private Validator validator;

    @InjectMocks private ProductDto productDto;

    @Test
    void add_shouldNormalizeValidateAndCallFlow() throws Exception {
        ProductForm form = new ProductForm();
        form.setName("  iPhone  ");
        form.setBarcode("  b1  ");
        form.setMrp(100.0);
        form.setClientId(1);

        when(validator.validate(any(ProductForm.class))).thenReturn(Set.of());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        productDto.add(form);

        verify(productFlow).add(captor.capture());
        Product sent = captor.getValue();
        assertEquals("iPhone", sent.getName());
        assertEquals("b1", sent.getBarcode());
        assertEquals(100.0, sent.getMrp());
        assertEquals(1, sent.getClientId());
    }

    @Test
    void add_shouldThrow_whenValidationFails() {
        ProductForm form = new ProductForm();
        form.setName("x");

        @SuppressWarnings("unchecked")
        ConstraintViolation<ProductForm> v = mock(ConstraintViolation.class);
        when(v.getMessage()).thenReturn("name invalid");
        when(validator.validate(any(ProductForm.class))).thenReturn(Set.of(v));

        ApiException ex = assertThrows(ApiException.class, () -> productDto.add(form));
        assertEquals("name invalid", ex.getMessage());
        verifyNoInteractions(productFlow);
    }

    @Test
    void update_shouldNormalizeValidateAndCallApi() throws Exception {
        ProductForm form = new ProductForm();
        form.setName("  n  ");
        form.setBarcode("  b  ");
        form.setMrp(10.0);
        form.setClientId(2);

        when(validator.validate(any(ProductForm.class))).thenReturn(Set.of());

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        productDto.update(7, form);

        verify(productApi).update(eq(7), captor.capture());
        Product sent = captor.getValue();
        assertEquals("n", sent.getName());
        assertEquals("b", sent.getBarcode());
        assertEquals(10.0, sent.getMrp());
        assertEquals(2, sent.getClientId());
    }

    @Test
    void addBulk_shouldReturn_whenParserReturnsEmpty() throws Exception {
        MultipartFile file = new MockMultipartFile(
                "file",
                "p.tsv",
                "text/tab-separated-values",
                "".getBytes(StandardCharsets.UTF_8)
        );

        try (MockedStatic<ProductTsvParser> mocked = mockStatic(ProductTsvParser.class)) {
            mocked.when(() -> ProductTsvParser.parse(any(MultipartFile.class), anyInt()))
                    .thenReturn(new ProductTsvParser.ProductTsvParseResult(List.of()));

            productDto.addBulk(1, file);

            verifyNoInteractions(productFlow);
        }
    }


    @Test
    void addBulk_shouldValidateEachRowAndCallFlow() throws Exception {
        MultipartFile file = new MockMultipartFile("file", "p.tsv", "text/tab-separated-values",
                "x".getBytes(StandardCharsets.UTF_8));

        ProductForm f1 = new ProductForm();
        f1.setName("  A ");
        f1.setBarcode("  b1 ");
        f1.setMrp(1.0);
        f1.setClientId(99);

        ProductForm f2 = new ProductForm();
        f2.setName("B");
        f2.setBarcode("b2");
        f2.setMrp(2.0);
        f2.setClientId(99);

        when(validator.validate(any(ProductForm.class))).thenReturn(Set.of());

        try (MockedStatic<ProductTsvParser> mocked = mockStatic(ProductTsvParser.class)) {
            mocked.when(() -> ProductTsvParser.parse(any(MultipartFile.class), eq(99)))
                    .thenReturn(new ProductTsvParser.ProductTsvParseResult(List.of(f1, f2)));

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<Product>> captor = ArgumentCaptor.forClass(List.class);

            productDto.addBulk(99, file);

            verify(productFlow).addBulk(captor.capture(), eq(99));
            List<Product> products = captor.getValue();
            assertEquals(2, products.size());
            assertEquals("A", products.get(0).getName());
        }

    }

    @Test
    void getProducts_shouldNormalizeInputsAndReturnPaginatedResponse() throws ApiException {
        Product p = new Product();
        p.setId(1);
        p.setName("n");
        p.setBarcode("barcode");
        p.setMrp(5.0);
        p.setClientId(10);

        when(productApi.search(eq("name"), eq("barcode"), eq(10), eq(2), eq(20)))
                .thenReturn(List.of(p));
        when(productApi.getCount(eq("name"), eq("barcode"), eq(10)))
                .thenReturn(1L);

        PaginatedResponse<ProductData> resp =
                productDto.getProducts("  name ", "  barcode ", 10, 2, 20);

        assertEquals(1L, resp.getTotalCount());
        assertEquals(2, resp.getPageNo());
        assertEquals(1, resp.getData().size());
        assertEquals("barcode", resp.getData().get(0).getBarcode());
    }

}
