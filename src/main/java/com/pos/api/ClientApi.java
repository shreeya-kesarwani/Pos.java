package com.pos.api;

import com.pos.dao.ClientDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    // --- GET / GETCHECK PATTERN ---

    public Client get(Integer id) {
        return clientDao.select(id, Client.class);
    }

    public Client getCheck(Integer id) throws ApiException {
        Client p = get(id);
        if (p == null) {
            throw new ApiException(String.format("Client ID %d does not exist", id));
        }
        return p;
    }

    public Client getByName(String name) {
        List<Client> results = clientDao.search(null, name, null, 0, 1);
        return results.isEmpty() ? null : results.get(0);
    }

    public Client getCheckByName(String name) throws ApiException {
        Client p = getByName(name);
        if (p == null) {
            throw new ApiException(String.format("Client with name [%s] does not exist", name));
        }
        return p;
    }

    // --- CORE LOGIC ---

    public void add(Client clientPojo) throws ApiException {
        if (getByName(clientPojo.getName()) != null) {
            throw new ApiException(String.format("Client [%s] already exists", clientPojo.getName()));
        }
        clientDao.insert(clientPojo);
    }

    public void update(String name, Client p) throws ApiException {
        Client existing = getCheckByName(name);

        Client other = getByName(p.getName());
        if (other != null && !other.getId().equals(existing.getId())) {
            throw new ApiException(String.format("The name [%s] is already taken by another client", p.getName()));
        }

        existing.setName(p.getName());
        existing.setEmail(p.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Client> search(Integer id, String name, String email, int page, int size) {
        return clientDao.search(id, name, email, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, String name, String email) {
        return clientDao.getCount(id, name, email);
    }
}