package com.pos.controller;

import com.pos.dto.ClientDto;
import com.pos.exception.ApiException;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    @RequestMapping(method = RequestMethod.POST)
    public void add(@Valid @RequestBody ClientForm clientForm) throws ApiException {
        clientDto.add(clientForm);
    }

    @RequestMapping(method = RequestMethod.GET)
    public PaginatedResponse<ClientData> getClients(
            @RequestParam(name = "id", required = false) Integer id,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "email", required = false) String email,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size
    ) throws ApiException {
        return clientDto.getClients(id, name, email, page, size);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(
            @PathVariable Integer id,
            @Valid @RequestBody ClientForm form
    ) throws ApiException {
        clientDto.update(id, form);
    }
}