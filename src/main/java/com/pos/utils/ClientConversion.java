package com.pos.utils;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;
import org.springframework.stereotype.Component;
//make the methods static and import that in dto
@Component
public class ClientConversion {

    public ClientData convert(ClientPojo clientPojo) {
        ClientData data = new ClientData();
        data.setId(clientPojo.getId());
        data.setName(clientPojo.getName());
        data.setEmail(clientPojo.getEmail());
        return data;
    }

    public ClientPojo convert(ClientForm clientForm) {
        ClientPojo pojo = new ClientPojo();
        pojo.setName(clientForm.getName());
        pojo.setEmail(clientForm.getEmail());
        return pojo;
    }
}