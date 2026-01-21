package com.pos.utils;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;

public class ClientConversion {
    //TODO function name change
    public static ClientData convertFormToPojo(ClientPojo clientPojo) {
        //if (clientPojo == null) return null;
        ClientData data = new ClientData();
        data.setId(clientPojo.getId());
        data.setName(clientPojo.getName());
        data.setEmail(clientPojo.getEmail());
        return data;
    }

    public static ClientPojo convertFormToPojo(Integer id, ClientForm clientForm) {
        ClientPojo pojo = new ClientPojo();
        pojo.setName(clientForm.getName());
        pojo.setEmail(clientForm.getEmail());
        pojo.setId(id);
        return pojo;
    }
}