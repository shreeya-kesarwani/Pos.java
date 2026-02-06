package com.pos.dto;

import com.pos.api.OrderApi;
import com.pos.api.OrderItemApi;
import com.pos.api.ProductApi;
import com.pos.client.InvoiceClient;
import com.pos.exception.ApiException;
import com.pos.flow.OrderFlow;
import com.pos.model.constants.OrderStatus;
import com.pos.model.data.InvoiceData;
import com.pos.model.data.OrderData;
import com.pos.model.data.OrderItemData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InvoiceForm;
import com.pos.model.form.OrderForm;
import com.pos.pojo.Order;
import com.pos.pojo.OrderItem;
import com.pos.pojo.Product;
import com.pos.utils.InvoiceConversion;
import com.pos.utils.OrderConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.pos.model.constants.ErrorMessages.*;

@Component
public class OrderDto extends AbstractDto {

    private static final String INVOICE_DIR = "/tmp/invoices";

    @Autowired private OrderFlow orderFlow;
    @Autowired private OrderApi orderApi;
    @Autowired private OrderItemApi orderItemApi;
    @Autowired private ProductApi productApi;
    @Autowired private InvoiceClient invoiceClient;

    public Integer create(@Valid OrderForm form) throws ApiException {
        normalize(form);
        return orderFlow.createOrder(form);
    }

    public PaginatedResponse<OrderData> search(
            Integer id,
            ZonedDateTime start,
            ZonedDateTime end,
            String status,
            Integer pageNumber,
            Integer pageSize
    ) throws ApiException {

        OrderStatus orderStatus = validateStatus(status);

        List<Order> orders = orderApi.search(id, start, end, orderStatus, pageNumber, pageSize);
        Long totalCount = orderApi.getCount(id, start, end, orderStatus);

        List<OrderData> data = new ArrayList<>();
        for (Order order : orders) {
            Double totalAmount = calculateTotalAmount(order.getId());
            data.add(OrderConversion.toOrderData(order, totalAmount));
        }

        return PaginatedResponse.of(data, totalCount, pageNumber);
    }

    public List<OrderItemData> getItems(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);

        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        if (items.isEmpty()) return List.of();

        Map<Integer, Product> productById = getProductByIdMap(items);

        List<OrderItemData> data = new ArrayList<>();
        for (OrderItem item : items) {
            Product product = productById.get(item.getProductId());
            if (product == null) {
                throw new ApiException(PRODUCT_NOT_FOUND.value() + ": productId=" + item.getProductId());
            }
            data.add(OrderConversion.toOrderItemData(item, product.getBarcode(), product.getName()));
        }
        return data;
    }

    public InvoiceData invoice(Integer orderId) throws ApiException {

        Order order = orderApi.getCheck(orderId);
        if (order.getStatus() == OrderStatus.INVOICED) {
            throw new ApiException(ORDER_ALREADY_INVOICED.value() + ": orderId=" + orderId);
        }

        List<OrderItem> items = orderItemApi.getByOrderId(orderId);
        if (items.isEmpty()) {
            throw new ApiException(CANNOT_INVOICE_EMPTY_ORDER.value() + ": orderId=" + orderId);
        }

        Map<Integer, Product> productById = getProductByIdMap(items);

        InvoiceForm form = InvoiceConversion.toInvoiceForm(orderId, items, productById);
        validateForm(form);

        InvoiceData data = invoiceClient.generate(form);
        byte[] pdfBytes = InvoiceConversion.decodePdfBytes(data);

        storeInvoicePdfAndAttach(orderId, pdfBytes);

        return data;
    }

    public ResponseEntity<byte[]> downloadInvoiceResponse(Integer orderId) throws ApiException {
        byte[] pdf = downloadInvoice(orderId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=INV-" + orderId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    private Double calculateTotalAmount(Integer orderId) throws ApiException {
        orderApi.getCheck(orderId);
        List<OrderItem> items = orderItemApi.getByOrderId(orderId);

        double total = 0.0;
        for (OrderItem item : items) {
            total += item.getQuantity() * item.getSellingPrice();
        }
        return total;
    }

    private byte[] downloadInvoice(Integer orderId) throws ApiException {
        Order order = orderApi.getCheck(orderId);

        if (order.getStatus() != OrderStatus.INVOICED) {
            throw new ApiException(INVOICE_NOT_GENERATED_YET.value() + ": orderId=" + orderId);
        }

        String path = order.getInvoicePath();
        if (path == null || path.isBlank()) {
            throw new ApiException(INVOICE_PATH_MISSING.value() + ": orderId=" + orderId);
        }

        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new ApiException(FAILED_TO_READ_INVOICE_FILE.value() + ": path=" + path, e);
        }
    }

    private Map<Integer, Product> getProductByIdMap(List<OrderItem> items) {
        Set<Integer> productIds = items.stream()
                .map(OrderItem::getProductId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        if (productIds.isEmpty()) return Map.of();

        List<Product> products = productApi.getByIds(productIds.stream().toList());

        return products.stream()
                .filter(p -> p.getId() != null)
                .collect(Collectors.toMap(Product::getId, p -> p, (a, b) -> a));
    }

    private OrderStatus validateStatus(String status) throws ApiException {
        if (status == null || status.trim().isEmpty()) return null;
        try {
            return OrderStatus.valueOf(status.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ApiException(INVALID_STATUS.value() + ": " + status);
        }
    }

    private void storeInvoicePdfAndAttach(Integer orderId, byte[] pdfBytes) throws ApiException {
        try {
            Path dir = Paths.get(INVOICE_DIR);
            Files.createDirectories(dir);

            Path path = dir.resolve("INV-" + orderId + ".pdf");
            Files.write(path, pdfBytes);

            orderApi.attachInvoice(orderId, path.toString());
        } catch (Exception e) {
            throw new ApiException(FAILED_TO_STORE_INVOICE_PDF.value() + ": orderId=" + orderId, e);
        }
    }
}
