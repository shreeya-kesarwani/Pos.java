package com.pos.dto;

import com.pos.api.ClientApi;
import com.pos.exception.ApiException;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client;
import com.pos.utils.ClientConversion;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ClientDto extends AbstractDto {

    @Autowired
    private ClientApi clientApi;

    public void add(@Valid ClientForm clientForm) throws ApiException {
        normalize(clientForm);
        Client pojo = ClientConversion.convertFormToPojo(clientForm);
        clientApi.add(pojo);
    }

    public void update(String name, @Valid ClientForm clientForm) throws ApiException {
        normalize(clientForm);
        Client ClientPojo = ClientConversion.convertFormToPojo(clientForm);
        clientApi.update(name, ClientPojo);
    }

    public PaginatedResponse<ClientData> getClients(Integer id, String name, String email, Integer pageNumber, Integer pageSize) throws ApiException {

        String normalizedName = normalize(name);
        String normalizedEmail = normalize(email);

        List<Client> clients = clientApi.search(id, normalizedName, normalizedEmail, pageNumber, pageSize);
        Long totalCount = clientApi.getCount(id, normalizedName, normalizedEmail);

        List<ClientData> dataList = clients.stream()
                .map(pojo -> ClientConversion.convertPojoToData(pojo.getId(), pojo))
                .toList();

        return PaginatedResponse.of(dataList, totalCount, pageNumber);
    }

    public Long getTotalClients() {
        return clientApi.getCount(null, null, null);
    }
}
