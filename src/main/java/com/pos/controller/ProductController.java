package com.pos.controller;

import com.pos.dto.ProductDto;
import com.pos.model.data.ProductData;
import com.pos.model.form.ProductForm;
import com.pos.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @RequestMapping(method = RequestMethod.POST)
    public void add(@RequestBody ProductForm form) throws ApiException {
        productDto.add(form);
    }

    @RequestMapping(method = RequestMethod.GET)
    public List<ProductData> get(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String clientName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) throws ApiException {

        // Note: Passing null for clientId as the UI uses clientName
        //change name of this
        return productDto.getProducts(name, barcode, null, clientName, page, size);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @RequestBody ProductForm form) throws ApiException {
        productDto.update(id, form);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long getCount() {
        return productDto.getCount();
    }
}