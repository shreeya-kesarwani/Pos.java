package com.pos.dto;

import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;
import com.pos.service.ApiException;
import com.pos.service.ClientService;
import com.pos.utils.ClientConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Component
@Validated
public class ClientDto extends AbstractDto {

    @Autowired
    private ClientService clientService;
    public void addClient(@Valid ClientForm form) throws ApiException {
        validateForm(form);
        normalize(form);
        ClientPojo pojo = ClientConversion.convertFormToPojo(form);
        clientService.addClient(pojo);
    }

    public void update(String name, @Valid ClientForm form) throws ApiException {
        validateForm(form);
        normalize(form);
        ClientPojo pojo = ClientConversion.convertFormToPojo(form);
        clientService.update(name, pojo);
    }

    public PaginatedResponse<ClientData> getClients(Integer id, String name, String email, Integer page, Integer size) throws ApiException {
        int p = (page == null) ? 0 : page;
        int s = (size == null) ? 10 : size;

        String nName = normalize(name);
        String nEmail = normalize(email);

        List<ClientPojo> pojos = clientService.search(id, nName, nEmail, p, s);

        // Task 2: Pass the clean strings to Count as well
        Long totalCount = clientService.getCount(id, nName, nEmail);

        List<ClientData> dataList = pojos.stream()
                .map(pojo -> ClientConversion.convertPojoToData(pojo.getId(), pojo))
                .toList();

        return PaginatedResponse.of(dataList, totalCount, p);
    }

    public Long getTotalClients() {
        return clientService.getCount(null,null,null);
    }
}