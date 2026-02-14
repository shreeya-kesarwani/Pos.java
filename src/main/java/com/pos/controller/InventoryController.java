package com.pos.controller;

import com.pos.dto.InventoryDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("file") MultipartFile file) throws ApiException, IOException {
        inventoryDto.upload(file);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<InventoryData> get(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) throws ApiException {
        return inventoryDto.getAll(barcode, productName, page, size);
    }
}