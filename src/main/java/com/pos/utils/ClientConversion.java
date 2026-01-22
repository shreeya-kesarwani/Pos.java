package com.pos.utils;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.ClientPojo;

public class ClientConversion {

    public static ClientPojo convertFormToPojo(ClientForm clientForm) {
        ClientPojo clientPojo = new ClientPojo();
        clientPojo.setName(clientForm.getName());
        clientPojo.setEmail(clientForm.getEmail());
        return clientPojo;
    }

    public static ClientData convertPojoToData(Integer id, ClientPojo clientPojo) {
        ClientData clientData = new ClientData();
        clientData.setId(id);
        clientData.setName(clientPojo.getName());
        clientData.setEmail(clientPojo.getEmail());
        return clientData;
    }
}