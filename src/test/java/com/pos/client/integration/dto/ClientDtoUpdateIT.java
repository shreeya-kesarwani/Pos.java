package com.pos.client.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.ClientDto;
import com.pos.model.form.ClientForm;
import com.pos.pojo.Client;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientDtoUpdateIT extends AbstractClientDtoIntegrationTest {

    @Autowired ClientDto clientDto;
    @Autowired ClientDao clientDao;

    @Test
    void shouldUpdateClient_happyFlow() throws Exception {
        Client existing = TestEntities.newClient("Old", "old@x.com");
        clientDao.insert(existing);
        flushAndClear();

        ClientForm form = clientForm("  New  ", "  new@x.com  ");

        clientDto.update(existing.getId(), form);
        flushAndClear();

        Client updated = clientDao.selectById(existing.getId());
        assertEquals("New", updated.getName());
        assertEquals("new@x.com", updated.getEmail());
    }
}