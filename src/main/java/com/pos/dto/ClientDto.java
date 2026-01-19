package com.pos.dto;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;
import com.pos.service.ApiException;
import com.pos.service.ClientService;
import com.pos.utils.ClientConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ClientDto extends AbstractDto {

    @Autowired private ClientService clientService;
    @Autowired private ClientConversion clientConversion;

    public void add(@Valid ClientForm clientForm) throws ApiException {
        clientForm.setName(normalize(clientForm.getName()));
        clientForm.setEmail(normalize(clientForm.getEmail()));

        ClientPojo clientPojo = clientConversion.convert(clientForm);
        clientService.add(clientPojo);
    }

    public List<ClientData> getAll() {
        return clientService.getAll().stream()
                .map(clientPojo -> clientConversion.convert(clientPojo))
                .toList();
    }
}