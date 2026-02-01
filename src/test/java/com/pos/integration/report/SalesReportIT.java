//package com.pos.integration.report;
//
//import com.pos.dao.ClientDao;
//import com.pos.dto.OrderDto;
//import com.pos.dto.ProductDto;
//import com.pos.dto.SalesReportDto;
//import com.pos.exception.ApiException;
//import com.pos.integration.AbstractMySqlIntegrationTest;
//import com.pos.model.data.SalesReportData;
//import com.pos.model.form.OrderForm;
//import com.pos.model.form.OrderItemForm;
//import com.pos.model.form.ProductForm;
//import com.pos.model.form.SalesReportForm;
//import com.pos.pojo.Client;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.ActiveProfiles;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class SalesReportIT extends AbstractMySqlIntegrationTest {
//
//    @Autowired private SalesReportDto salesReportDto;
//    @Autowired private OrderDto orderDto;
//    @Autowired private ProductDto productDto;
//    @Autowired private ClientDao clientDao;
//
//    private Integer insertClient(String name, String email) {
//        Client c = new Client();
//        c.setName(name);
//        c.setEmail(email);
//        clientDao.insert(c);
//        return c.getId();
//    }
//
//    private void addProduct(String clientName, String barcode, String name, double mrp) throws Exception {
//        ProductForm f = new ProductForm();
//        f.setClientName(clientName);
//        f.setBarcode(barcode);
//        f.setName(name);
//        f.setMrp(mrp);
//        f.setImageUrl(null);
//        productDto.add(f);
//    }
//
//    private static OrderItemForm item(String barcode, int quantity, double sellingPrice) {
//        OrderItemForm i = new OrderItemForm();
//        i.setBarcode(barcode);
//        i.setQuantity(quantity);
//        i.setSellingPrice(sellingPrice);
//        return i;
//    }
//
//    private Integer createOrderWithItems(List<OrderItemForm> items) throws Exception {
//        OrderForm form = new OrderForm();
//        form.setItems(items);
//        return orderDto.create(form);
//    }
//
//    private static SalesReportForm reportForm(LocalDate start, LocalDate end, Integer clientId) {
//        SalesReportForm f = new SalesReportForm();
//        f.setStartDate(start);
//        f.setEndDate(end);
//        f.setClientId(clientId);
//        return f;
//    }
//
//    @Test
//    void getCheck_shouldValidateDates_andReturnAggregatedData() throws Exception {
//        // Arrange
//        insertClient("ABC", "abc@test.com");
//        addProduct("ABC", "SR1", "Pen", 10.0);
//        addProduct("ABC", "SR2", "Pencil", 5.0);
//
//        // Create orders (your report may only include INVOICED orders; if so,
//        // you'll need to add the "invoice/complete" step here once you share that method.)
//        createOrderWithItems(List.of(
//                item("SR1", 2, 9.0),
//                item("SR2", 3, 4.0)
//        ));
//
//        LocalDate today = LocalDate.now();
//        SalesReportForm form = reportForm(today.minusDays(1), today.plusDays(1), null);
//
//        // Act
//        salesReportDto.validate_form(form);
//        List<SalesReportData> data = salesReportDto.getCheck();
//
//        // Assert
//        assertNotNull(data);
//        assertFalse(data.isEmpty());
//
//        // We don't assume exact field names beyond the DTO returning list,
//        // but typically each row is sku/barcode aggregated.
//        assertTrue(
//                data.stream().anyMatch(r ->
//                        "SR1".equalsIgnoreCase(r.getBarcode()) || "SR1".equalsIgnoreCase(r.getBarcode()
//                        )
//                ),
//                "Expected report to include SR1"
//        );
//    }
//
//    @Test
//    void validate_form_shouldFail_whenStartAfterEnd() {
//        LocalDate today = LocalDate.now();
//        SalesReportForm form = reportForm(today, today.minusDays(1), null);
//
//        ApiException ex = assertThrows(ApiException.class, () -> salesReportDto.validate_form(form));
//        assertTrue(ex.getMessage().contains("startDate cannot be after endDate"));
//    }
//
//    @Test
//    void getCheck_shouldFilterByClient_whenClientIdProvided() throws Exception {
//        // Arrange: two clients, one product each
//        Integer client1Id = insertClient("C1", "c1@test.com");
//        insertClient("C2", "c2@test.com");
//
//        addProduct("C1", "C1B1", "Pen", 10.0);
//        addProduct("C2", "C2B1", "Pen", 10.0);
//
//        // Orders for both
//        createOrderWithItems(List.of(item("C1B1", 1, 9.0)));
//        createOrderWithItems(List.of(item("C2B1", 1, 9.0)));
//
//        LocalDate today = LocalDate.now();
//        SalesReportForm form = reportForm(today.minusDays(1), today.plusDays(1), client1Id);
//
//        // Act
//        salesReportDto.validate_form(form);
//        List<SalesReportData> data = salesReportDto.getCheck();
//
//        // Assert: should include C1 barcode, should NOT include C2 barcode
//        assertNotNull(data);
//
//        boolean hasC1 = data.stream().anyMatch(r ->
//                "C1B1".equalsIgnoreCase(r.getBarcode()) || "C1B1".equalsIgnoreCase(r.getBarcode()
//                )
//        );
//        boolean hasC2 = data.stream().anyMatch(r ->
//                "C2B1".equalsIgnoreCase(r.getBarcode()) || "C2B1".equalsIgnoreCase(r.getBarcode()
//                )
//        );
//
//        assertTrue(hasC1, "Expected filtered report to include client1 sales");
//        assertFalse(hasC2, "Expected filtered report to exclude client2 sales");
//    }
//}
