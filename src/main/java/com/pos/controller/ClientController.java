package com.pos.controller;

import com.pos.dto.ClientDto;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import com.pos.model.data.ClientData;
import com.pos.exception.ApiException;
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
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) throws ApiException {

        return clientDto.getClients(id, name, email, page, size);
    }

    @RequestMapping(value = "/{ClientName}", method = RequestMethod.PUT)
    public void update(@PathVariable String ClientName, @Valid @RequestBody ClientForm clientForm) throws ApiException {
        clientDto.update(ClientName, clientForm);
    }

    @RequestMapping(value = "/count", method = RequestMethod.GET)
    public Long getCount() {
        return clientDto.getTotalClients();
    }
}