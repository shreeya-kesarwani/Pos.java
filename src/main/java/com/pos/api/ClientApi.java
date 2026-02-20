package com.pos.api;

import com.pos.dao.ClientDao;
import com.pos.exception.ApiException;
import com.pos.model.data.ClientData;
import com.pos.pojo.Client;
import com.pos.utils.ClientConversion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    public Client get(Integer id) {
        return clientDao.selectById(id);
    }

    public Client getCheck(Integer id) throws ApiException {
        Client client = get(id);
        if (client == null) {
            throw new ApiException(CLIENT_ID_NOT_FOUND.value() + ": " + id);
        }
        return client;
    }

    public Client getByName(String name) {
        return clientDao.selectByName(name);
    }

    public Client getCheckByName(String name) throws ApiException {
        if (name == null) return null;

        Client client = getByName(name);
        if (client == null) {
            throw new ApiException(CLIENT_NAME_NOT_FOUND.value() + ": " + name);
        }
        return client;
    }

    public void add(Client clientPojo) throws ApiException {
        if (getByName(clientPojo.getName()) != null) {
            throw new ApiException(CLIENT_ALREADY_EXISTS.value() + ": " + clientPojo.getName());
        }
        clientDao.insert(clientPojo);
    }

    public void update(Integer clientId, Client client) throws ApiException {

        Client existing = getCheck(clientId);

        Client other = getByName(client.getName());
        if (other != null && !other.getId().equals(existing.getId())) {
            throw new ApiException(CLIENT_NAME_TAKEN.value() + ": " + client.getName());
        }

        existing.setName(client.getName());
        existing.setEmail(client.getEmail());
    }

    public List<Client> search(Integer id, String name, String email, int page, int size) {
        return clientDao.searchByParams(id, name, email, page, size);
    }

    public long getCount(Integer id, String name, String email) {
        Long count = clientDao.getCount(id, name, email);
        return count == null ? 0L : count;
    }

    public List<Client> getByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) return List.of();
        return clientDao.selectByNames(names);
    }

    public List<Client> getByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        return clientDao.selectByIds(ids);
    }

    // -------------------- Static helpers --------------------

    public static List<ClientData> toClientDataList(List<Client> clients) {
        if (clients == null || clients.isEmpty()) return List.of();

        return clients.stream()
                .map(pojo -> ClientConversion.convertPojoToData(pojo.getId(), pojo))
                .toList();
    }
}