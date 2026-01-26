package com.pos.controller;

import com.pos.dto.InventoryDto;
import com.pos.model.data.InventoryData;
import com.pos.exception.ApiException;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @Operation(summary = "Upload inventory via TSV")
    @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void upload(@RequestParam("file") MultipartFile file) throws ApiException, IOException {
        inventoryDto.upload(file);
    }

    @Operation(summary = "Get filtered inventory list")
    @RequestMapping(method = RequestMethod.GET)
    public List<InventoryData> get(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String clientName) throws ApiException {
        return inventoryDto.getAll(barcode, productName, clientName);
    }
}