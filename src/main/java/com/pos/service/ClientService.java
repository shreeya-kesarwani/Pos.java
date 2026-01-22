package com.pos.service;

import com.pos.dao.ClientDao;
import com.pos.pojo.ClientPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ClientService {

    @Autowired
    private ClientDao clientDao;

    public void addClient(ClientPojo p) throws ApiException {
        if (getByName(p.getName()) != null) {
            throw new ApiException(String.format("Client [%s] already exists", p.getName()));
        }
        clientDao.insert(p);
    }

    // Replace your old update method with this one in ClientService.java
    public void update(String name, ClientPojo p) throws ApiException {
        ClientPojo existing = getByName(name);
        if (existing == null) {
            throw new ApiException(String.format("Client with name [%s] does not exist", name));
        }

        ClientPojo other = getByName(p.getName());
        if (other != null && !other.getId().equals(existing.getId())) {
            throw new ApiException(String.format("The name [%s] is already taken by another client", p.getName()));
        }

        existing.setName(p.getName());
        existing.setEmail(p.getEmail());
    }

    public ClientPojo getCheckById(Integer id) throws ApiException {
        ClientPojo p = clientDao.select(id, ClientPojo.class);
        if (p == null) {
            throw new ApiException(String.format("Client ID %d not found", id));
        }
        return p;
    }

    @Transactional(readOnly = true)
    public List<ClientPojo> search(Integer id, String name, String email, int page, int size) {
        return clientDao.search(id, name, email, page, size);
    }

    // UPDATED: Now accepts filters to provide a dynamic count for pagination
    @Transactional(readOnly = true)
    public Long getCount(Integer id, String name, String email) {
        return clientDao.getCount(id, name, email);
    }

    private ClientPojo getByName(String name) {
        List<ClientPojo> results = clientDao.search(null, name, null, 0, 1);
        return results.isEmpty() ? null : results.get(0);
    }
}