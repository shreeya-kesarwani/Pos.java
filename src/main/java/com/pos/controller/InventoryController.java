package com.pos.controller;

import com.pos.dto.InventoryDto;
import com.pos.model.form.InventoryForm;
import com.pos.service.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @PostMapping("/upload")
    public void upload(@RequestBody List<InventoryForm> forms) throws ApiException {
        inventoryDto.upload(forms);
    }

}