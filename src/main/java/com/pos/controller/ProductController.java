package com.pos.controller;

import com.pos.dto.ProductDto;
import com.pos.model.data.ProductData;
import com.pos.model.data.PaginatedResponse; // New wrapper
import com.pos.model.form.ProductForm;
import com.pos.exception.ApiException;
import com.pos.utils.TsvParser;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/products") // Conventionally plural and prefixed
public class ProductController {

    @Autowired
    private ProductDto productDto;

    // CREATE
    @RequestMapping(method = RequestMethod.POST)
    public void add(@Valid @RequestBody ProductForm form) throws ApiException {
        productDto.add(form);
    }

    @Operation(summary = "Uploads products via TSV file")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("file") MultipartFile file) throws ApiException, IOException {
        // Parsing logic moved to DTO layer as requested
        productDto.addBulkFromTsv(file);
    }

    // SEARCH & LIST (Unified)
    // Task 1: Return PaginatedResponse instead of List
    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<ProductData> search(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String clientName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws ApiException {

        // Fix 1: Use getProducts (matches your DTO)
        // Fix 2: Passing parameters to getProducts ensures it returns the PaginatedResponse correctly
        return productDto.getProducts(name, barcode, clientName, page, size);
    }

    // UPDATE
    @RequestMapping(value = "/{barcode}", method = RequestMethod.PUT)
    public void update(@PathVariable String barcode, @Valid @RequestBody ProductForm form) throws ApiException {
        productDto.update(barcode, form);
    }

    // COUNT
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long getCount() {
        return productDto.getCount();
    }
}