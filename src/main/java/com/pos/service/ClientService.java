package com.pos.service;

import com.pos.dao.ClientDao;
import com.pos.pojo.ClientPojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class ClientService {

    @Autowired
    private ClientDao clientDao;

    @Transactional(rollbackFor = ApiException.class)
    public void add(ClientPojo clientPojo) throws ApiException {
        if (clientDao.findByEmail(clientPojo.getEmail()).isPresent()) {
            throw new ApiException("Client with this email already exists: " + clientPojo.getEmail());
        }
        clientDao.insert(clientPojo);
    }

    @Transactional(readOnly = true)
    public ClientPojo getByEmail(String email) throws ApiException {
        return clientDao.findByEmail(email).orElseThrow(() ->
                new ApiException("Client with email " + email + " does not exist in Client Master"));
    }

    @Transactional(readOnly = true)
    public ClientPojo get(Integer id) throws ApiException {
        ClientPojo clientPojo = clientDao.select(id, ClientPojo.class);
        if (clientPojo == null) {
            throw new ApiException("Client with ID " + id + " does not exist");
        }
        return clientPojo;
    }

    @Transactional(readOnly = true)
    public List<ClientPojo> getAll() {
        return clientDao.selectAll(ClientPojo.class);
    }

    public String getSafe(Integer id) {
        try {
            return get(id).getName();
        } catch (Exception exception) {
            return "N/A";
        }
    }
}