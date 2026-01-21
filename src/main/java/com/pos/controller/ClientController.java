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
@RequestMapping("/clients")
public class ClientController {

    @Autowired
    private ClientDto clientDto;

    // CORRECT
    @RequestMapping(method = RequestMethod.POST)
    public void add(@Valid @RequestBody ClientForm clientForm) throws ApiException {
        clientDto.addClient(clientForm);
    }

    // READ (Combined Search & Pagination)
    @RequestMapping(method = RequestMethod.GET)
    public List<ClientData> get(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {
        return clientDto.getClients(id, name, email, page, size);
    }

    // UPDATE
    @RequestMapping(value = "/{id}", method = RequestMethod.PUT)
    public void update(@PathVariable Integer id, @Valid @RequestBody ClientForm clientForm) throws ApiException {
        clientDto.update(id, clientForm);
    }

    // PAGINATION METADATA
    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long getCount() {
        return clientDto.getTotalClients();
    }

    // DROPDOWNS (Non-paginated) -- TODO make this paginated
    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<ClientData> getAll() {
        return clientDto.getAll();
    }
}