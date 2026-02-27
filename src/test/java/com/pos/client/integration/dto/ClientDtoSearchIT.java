package com.pos.client.integration.dto;

import com.pos.dao.ClientDao;
import com.pos.dto.ClientDto;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientDtoSearchIT extends AbstractClientDtoIntegrationTest {

    @Autowired ClientDto clientDto;
    @Autowired ClientDao clientDao;

    @Test
    void shouldSearchClients_happyFlow() throws Exception {
        clientDao.insert(TestEntities.newClient("Acme", "a@acme.com"));
        clientDao.insert(TestEntities.newClient("Beta", "b@beta.com"));
        flushAndClear();

        PaginatedResponse<ClientData> resp =
                clientDto.getClients(null, "  Ac  ", null, 0, 10);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(c -> c.getName().equals("Acme")));
    }
}