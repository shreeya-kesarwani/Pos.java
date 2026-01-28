package com.pos.dto;

import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.exception.BulkUploadException;
import com.pos.exception.UploadValidationException;
import com.pos.flow.InventoryFlow;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventoryForm;
import com.pos.pojo.Inventory;
import com.pos.pojo.Product;
import com.pos.utils.InventoryConversion;
import com.pos.utils.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class InventoryDto extends AbstractDto {

    @Autowired private InventoryFlow inventoryFlow;
    @Autowired private ProductApi productApi;

    public void upload(MultipartFile file) throws ApiException, IOException {
        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "barcode", "quantity");

        List<String> errors = new ArrayList<>();
        List<Inventory> pojos = new ArrayList<>();
        List<String> barcodes = new ArrayList<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                InventoryForm form = new InventoryForm();
                form.setBarcode(TsvParser.s(r, 0));

                String qStr = TsvParser.s(r, 1);
                if (qStr.isEmpty()) throw new ApiException("quantity is required");
                try {
                    form.setQuantity(Integer.parseInt(qStr));
                } catch (NumberFormatException e) {
                    throw new ApiException("Invalid quantity");
                }

                validateForm(form);
                normalize(form);

                productApi.getCheckByBarcode(form.getBarcode());

                Inventory inv = InventoryConversion.convertFormToPojo(form);
                pojos.add(inv);
                barcodes.add(form.getBarcode());

            } catch (ApiException ex) {
                err = "Line " + (i + 1) + ": " + ex.getMessage();
            } catch (Exception ex) {
                err = "Line " + (i + 1) + ": Invalid row";
            }
            errors.add(err);
        }
        boolean hasAnyError = errors.stream().anyMatch(Objects::nonNull);
        if (hasAnyError) {
            byte[] errorTsv = TsvParser.buildErrorTsv(rows, errors);
            String fname = "inventory_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";
            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    "inventory_upload_errors.tsv",
                    "text/tab-separated-values"
            );
        }
        inventoryFlow.upsertBulk(pojos, barcodes);
    }

    public PaginatedResponse<InventoryData> getAll(
            String barcode, String productName, String clientName,
            Integer page, Integer size) throws ApiException {

        int pageNumber = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        String b = normalize(barcode);
        String pn = normalize(productName);
        String cn = normalize(clientName);

        List<Inventory> inventories = inventoryFlow.search(b, pn, cn, pageNumber, pageSize);
        java.util.Set<Integer> productIds = new java.util.HashSet<>();
        for (Inventory inv : inventories) {
            if (inv.getProductId() != null) productIds.add(inv.getProductId());
        }

        List<Product> products = productApi.getByIds(new java.util.ArrayList<>(productIds));
        java.util.Map<Integer, Product> productMap = new java.util.HashMap<>();
        for (Product p : products) {
            productMap.put(p.getId(), p);
        }

        List<InventoryData> dataList = new ArrayList<>();
        for (Inventory inventory : inventories) {
            Product p = productMap.get(inventory.getProductId());
            if (p == null) {
                System.out.println("Skipping inventory id=" + inventory.getId()
                        + " due to missing productId=" + inventory.getProductId());
                continue;
            }
            dataList.add(InventoryConversion.convertPojoToData(inventory, p.getBarcode(), p.getName()));
        }

        Long totalCount = inventoryFlow.getCount(b, pn, cn);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }
}
