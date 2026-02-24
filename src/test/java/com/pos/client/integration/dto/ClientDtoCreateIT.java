package com.pos.client.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.ClientDto;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client;
import com.pos.setup.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientDtoCreateIT extends AbstractIntegrationTest {

    @Autowired ClientDto clientDto;
    @Autowired ClientDao clientDao;

    @Test
    void shouldCreateClient_happyFlow() throws Exception {
        ClientForm form = new ClientForm();
        form.setName("  Acme  ");
        form.setEmail("  a@b.com  ");

        clientDto.add(form);
        flushAndClear();

        Client saved = clientDao.selectByName("Acme");
        assertNotNull(saved);
        assertEquals("Acme", saved.getName());
        assertEquals("a@b.com", saved.getEmail());
    }
}