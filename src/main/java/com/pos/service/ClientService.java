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

    public void addClient(ClientPojo clientPojo) throws ApiException {
        //TODO add a getcheckByName returns null/pojo
        //ClientPojo existing client = getcheckByName(ClientPojo.getName());
        //if (objects is non null (existing client){throw new ApiException("Client with this name already exists: " + clientPojo.getEmail());})

//        if (clientDao.selectByEmail(clientPojo.getName()).isPresent()) {
//            throw new ApiException("Client with this name{} already exists: " + clientPojo.getName());
//        } // should be a f string
        clientDao.insert(clientPojo);
    }

    public void update(Integer id, ClientPojo clientPojo) throws ApiException {
        getCheck(id);//store this id in a variable
        clientPojo.setId(id);
        //TODO if new name exists already -> exception throw
        //TODO enitity
//        clientPojo.setEmail(email); --- add this
//        clientDao.update(clientPojo);
    }

    @Transactional(readOnly = true)
    public void getCheck(Integer id) throws ApiException {
        ClientPojo clientPojo = clientDao.selectById(id, ClientPojo.class);
        if (clientPojo == null) {
            throw new ApiException("Client with ID " + id + " does not exist");
        }
    }

    //why readonly = true?, try not to use because we cannot use set ones as its read only
    //return type should be clientPojo
    @Transactional(readOnly = true)
    public ClientPojo getCheckById(Integer id) {
        //naming change of function
        ClientPojo clientPojo = clientDao.selectById(id, ClientPojo.class);
        return clientPojo;
//        try {
//            ClientPojo clientPojo = clientDao.selectById(id, ClientPojo.class);
//            return clientPojo;
//        } catch (Exception e) {
//            return "N/A";
//        }
    }

    // Unified Search Logic (Powers the DTO search)

    @Transactional(readOnly = true)
    public List<ClientPojo> getFiltered(Integer id, String name, String email) throws ApiException {
        if (id != null) {
            ClientPojo clientPojo = clientDao.selectById(id, ClientPojo.class);
            return clientPojo != null ? List.of(clientPojo) : List.of();
        }

        if (email != null && !email.isEmpty()) {
            return clientDao.selectByEmail(email)
                    .map(List::of)
                    .orElse(List.of());
        }

        if (name != null && !name.isEmpty()) {
            return clientDao.selectByName(name);
        }

        return List.of();
    }

    // Standard Pagination and Helper methods
    @Transactional(readOnly = true)
    public List<ClientPojo> getAll() { return clientDao.selectAll(ClientPojo.class); }

    @Transactional(readOnly = true)
    public List<ClientPojo> getPaged(int page, int size) { return clientDao.selectAllPaged(ClientPojo.class, page, size); }

    @Transactional(readOnly = true)
    public Long getCount() { return clientDao.count(ClientPojo.class); }
}