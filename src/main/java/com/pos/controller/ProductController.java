package com.pos.controller;

import com.pos.dto.ProductDto;
import com.pos.exception.ApiException;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.model.form.ProductSearchForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @RequestMapping(method = RequestMethod.POST)
    public void add(@Valid @RequestBody ProductForm form) throws ApiException {
        productDto.add(form);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("clientId") Integer clientId, @RequestParam("file") MultipartFile file)
            throws ApiException, IOException {
        productDto.addBulk(clientId, file);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<ProductData> search(@ModelAttribute ProductSearchForm form) throws ApiException {
        if (form.getPageNumber() == null) form.setPageNumber(0);
        if (form.getPageSize() == null) form.setPageSize(10);

        return productDto.getProducts(form);
    }

    @RequestMapping(value = "/{productId}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer productId, @Valid @RequestBody ProductForm form) throws ApiException {
        productDto.update(productId, form);
    }
}