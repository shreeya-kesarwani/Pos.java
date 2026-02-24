package com.pos.client.integration.dto;

import com.pos.model.form.ClientForm;
import com.pos.setup.AbstractIntegrationTest;

public abstract class AbstractClientDtoIntegrationTest extends AbstractIntegrationTest {

    protected ClientForm clientForm(String name, String email) {
        ClientForm form = new ClientForm();
        form.setName(name);
        form.setEmail(email);
        return form;
    }
}