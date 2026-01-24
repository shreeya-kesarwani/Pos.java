package com.pos.utils;

import com.pos.model.data.ClientData;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client;

public class ClientConversion {

    public static Client convertFormToPojo(ClientForm clientForm) {
        Client clientPojo = new Client();
        clientPojo.setName(clientForm.getName());
        clientPojo.setEmail(clientForm.getEmail());
        return clientPojo;
    }

    public static ClientData convertPojoToData(Integer id, Client clientPojo) {
        ClientData clientData = new ClientData();
        clientData.setId(id);
        clientData.setName(clientPojo.getName());
        clientData.setEmail(clientPojo.getEmail());
        return clientData;
    }
}