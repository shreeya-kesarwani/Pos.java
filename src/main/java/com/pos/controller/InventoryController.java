package com.pos.controller;

import com.pos.dto.InventoryDto;
import com.pos.exception.ApiException;
import com.pos.model.data.InventoryData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.InventorySearchForm;
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
    public PaginatedResponse<InventoryData> get(@ModelAttribute InventorySearchForm form) throws ApiException {
        if (form.getPageNumber() == null) form.setPageNumber(0);
        if (form.getPageSize() == null) form.setPageSize(10);

        return inventoryDto.getAll(form);
    }
}