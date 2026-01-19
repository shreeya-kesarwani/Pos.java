package com.pos.controller;

import com.pos.dto.ProductDto;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping("/upload")
    public void upload(@RequestBody List<ProductForm> forms) throws ApiException {
        productDto.uploadTsv(forms);
    }

    @PostMapping
    public void add(@RequestBody ProductForm form) throws ApiException {
        productDto.add(form);
    }

    @GetMapping
    public List<ProductData> getAll() throws ApiException {
        return productDto.getAll();
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Integer id, @RequestBody ProductForm form) throws ApiException {
        productDto.update(id, form);
    }

    @GetMapping
    public List<ProductData> getAll(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode) throws ApiException {
        return productDto.getAllFiltered(name, barcode);
    }
}