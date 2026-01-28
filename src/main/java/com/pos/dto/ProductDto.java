package com.pos.dto;

import com.pos.api.ClientApi;
import com.pos.api.ProductApi;
import com.pos.exception.ApiException;
import com.pos.exception.BulkUploadException;
import com.pos.exception.UploadValidationException;
import com.pos.flow.ProductFlow;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.pojo.Client;
import com.pos.pojo.Product;
import com.pos.utils.ProductConversion;
import com.pos.utils.TsvParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Component
public class ProductDto extends AbstractDto {

    @Autowired private ProductFlow productFlow;
    @Autowired private ClientApi clientApi;
    @Autowired private ProductApi productApi;

    public void add(ProductForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        Product productPojo = ProductConversion.convertFormToPojo(form);
        productFlow.add(productPojo, form.getClientName());
    }

    public void addBulkFromTsv(MultipartFile file) throws ApiException, IOException {
        List<String[]> rows = TsvParser.read(file.getInputStream());
        TsvParser.validateHeader(rows.get(0), "barcode", "clientname", "name", "mrp", "imageurl");

        List<String> errors = new ArrayList<>();
        List<Product> products = new ArrayList<>();
        List<String> clientNames = new ArrayList<>();

        // in-file duplicate barcode check
        Set<String> seenBarcodes = new HashSet<>();

        for (int i = 1; i < rows.size(); i++) {
            String[] r = rows.get(i);
            String err = null;

            try {
                ProductForm form = new ProductForm();
                form.setBarcode(TsvParser.s(r, 0));
                form.setClientName(TsvParser.s(r, 1));
                form.setName(TsvParser.s(r, 2));

                String mrpStr = TsvParser.s(r, 3);
                if (mrpStr.isEmpty()) throw new ApiException("mrp is required");
                try {
                    form.setMrp(Double.parseDouble(mrpStr));
                } catch (NumberFormatException e) {
                    throw new ApiException("Invalid mrp");
                }

                String img = TsvParser.s(r, 4);
                form.setImageUrl(img.isEmpty() ? null : img);

                // bean validations + trim
                validateForm(form);
                normalize(form);

                // business validations (no DB writes)
                String barcode = form.getBarcode();
                if (!seenBarcodes.add(barcode)) {
                    throw new ApiException("Duplicate barcode in file: " + barcode);
                }

                Client client = clientApi.getByName(form.getClientName());
                if (client == null) {
                    throw new ApiException("Client not found: " + form.getClientName());
                }

                if (productApi.getByBarcode(barcode) != null) {
                    throw new ApiException("Product with barcode [" + barcode + "] already exists");
                }

                Product product = ProductConversion.convertFormToPojo(form);
                products.add(product);
                clientNames.add(form.getClientName());

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
            String fname = "product_upload_errors_" + LocalDateTime.now().toString().replace(":", "-") + ".tsv";
            throw new UploadValidationException(
                    "TSV has errors",
                    errorTsv,
                    fname + ".tsv",
                    "text/tab-separated-values"
            );

        }

        // only when NO errors, do DB writes (all-or-nothing)
        productFlow.addBulk(products, clientNames);
    }

    public void update(String barcode, ProductForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        Product product = ProductConversion.convertFormToPojo(form);
        productFlow.update(barcode, product, form.getClientName());
    }

    public PaginatedResponse<ProductData> getProducts(String name, String barcode, String clientName, Integer page, Integer size)
            throws ApiException {

        int pageNumber = (page == null) ? 0 : page;
        int pageSize = (size == null) ? 10 : size;

        String normalizedName = normalize(name);
        String normalizedBarcode = normalize(barcode);
        String normalizedClientName = normalize(clientName);

        List<Product> pojos = productFlow.search(normalizedName, normalizedBarcode, normalizedClientName, pageNumber, pageSize);

        List<ProductData> dataList = pojos.stream()
                .map(pojo -> {
                    try {
                        String client = productFlow.getClientName(pojo.getClientId());
                        return ProductConversion.convertPojoToData(pojo.getId(), pojo, client);
                    } catch (Exception e) {
                        return ProductConversion.convertPojoToData(pojo.getId(), pojo, "Unknown Client");
                    }
                })
                .toList();

        Long totalCount = productFlow.getCount(normalizedName, normalizedBarcode, normalizedClientName);
        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }

    public Long getCount() {
        return productFlow.getCount(null, null, null);
    }
}
