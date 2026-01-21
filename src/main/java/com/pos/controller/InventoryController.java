package com.pos.controller;

import com.pos.dto.InventoryDto;
import com.pos.model.data.InventoryData;
import com.pos.model.form.InventoryForm;
import com.pos.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/inventory") // Base path
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    // Use method = RequestMethod.POST instead of @PostMapping
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public void upload(@RequestBody List<InventoryForm> forms) throws ApiException {
        inventoryDto.upload(forms);
    }

    // Use method = RequestMethod.GET instead of @GetMapping
    @RequestMapping(method = RequestMethod.GET)
    public List<InventoryData> get(
            @RequestParam(required = false) String barcode,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String clientName) throws ApiException {
        return inventoryDto.getAllFiltered(barcode, productName, clientName);
    }
}