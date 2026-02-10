package com.pos.api;

import com.pos.dao.ClientDao;
import com.pos.exception.ApiException;
import com.pos.pojo.Client;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

import static com.pos.model.constants.ErrorMessages.*;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ClientApi {

    @Autowired
    private ClientDao clientDao;

    @Transactional(readOnly = true)
    public Client get(Integer id) {
        return clientDao.selectById(id);
    }

    @Transactional(readOnly = true)
    public Client getCheck(Integer id) throws ApiException {
        Client client = get(id);
        if (client == null) {
            throw new ApiException(CLIENT_ID_NOT_FOUND.value() + ": " + id);
        }
        return client;
    }

    @Transactional(readOnly = true)
    public Client getByName(String name) {
        List<Client> results = clientDao.searchByParams(null, name, null, 0, 1);
        return results.isEmpty() ? null : results.get(0);
    }

    @Transactional(readOnly = true)
    public Client getCheckByName(String name) throws ApiException {
        if(name==null){
            return null;
        }
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

    public void update(String name, Client client) throws ApiException {

        Client existing = getCheckByName(name);
        Client other = getByName(client.getName());
        if (other != null && !other.getId().equals(existing.getId())) {
            throw new ApiException(CLIENT_NAME_TAKEN.value() + ": " + client.getName());
        }
        existing.setName(client.getName());
        existing.setEmail(client.getEmail());
    }

    @Transactional(readOnly = true)
    public List<Client> search(Integer id, String name, String email, int page, int size) {
        return clientDao.searchByParams(id, name, email, page, size);
    }

    @Transactional(readOnly = true)
    public Long getCount(Integer id, String name, String email) {
        return clientDao.getCount(id, name, email);
    }

    @Transactional(readOnly = true)
    public List<Client> getByNames(List<String> names) {
        if (CollectionUtils.isEmpty(names)) return List.of();
        return clientDao.selectByNames(names);
    }

    @Transactional(readOnly = true)
    public List<Client> getByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        return clientDao.selectByIds(ids);
    }
}
