package com.pos.dto;

import com.pos.api.ClientApi; // Renamed from ClientService
import com.pos.exception.ApiException;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client; // Ensure this is the correct POJO name
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
    private ClientApi clientApi;

    public void add(@Valid ClientForm form) throws ApiException {
        //validateForm(form);
        normalize(form);

        Client pojo = ClientConversion.convertFormToPojo(form);
        clientApi.add(pojo);
    }

    public void update(String name, @Valid ClientForm form) throws ApiException {
        validateForm(form);
        normalize(form);

        Client pojo = ClientConversion.convertFormToPojo(form);
        clientApi.update(name, pojo);
    }

    public PaginatedResponse<ClientData> getClients(Integer id, String name, String email, Integer page, Integer size) throws ApiException {
        int p = (page == null) ? 0 : page;
        int s = (size == null) ? 10 : size;

        String nName = normalize(name);
        String nEmail = normalize(email);

        List<Client> clients = clientApi.search(id, nName, nEmail, p, s);
        Long totalCount = clientApi.getCount(id, nName, nEmail);

        List<ClientData> dataList = clients.stream()
                .map(pojo -> ClientConversion.convertPojoToData(pojo.getId(), pojo))
                .toList();

        return PaginatedResponse.of(dataList, totalCount, p);
    }

    public Long getTotalClients() {
        return clientApi.getCount(null, null, null);
    }
}