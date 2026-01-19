package com.pos.controller;

import com.pos.dto.ClientDto;
import com.pos.model.form.ClientForm;
import com.pos.model.data.ClientData;
import com.pos.service.ApiException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/clients")
//add api to properties, global , /pos/api should be the path
//use @RequestMapping instead of post and get mapping
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @PostMapping
    public void add(@RequestBody ClientForm form) throws ApiException {
        clientDto.add(form);
    }

    @GetMapping
    public List<ClientData> getAll() throws ApiException {
        return clientDto.getAll();
    }
    //update method, getById, CRU wale hone chahiye, apply pagination on ui and backend, @valid in dto, read about idempotency (imp)
    //Use Swagger - it makes interface, add that for testing
}