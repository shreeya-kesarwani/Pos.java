package com.pos.client.integration.dto;

import com.pos.dto.ClientDto;
import com.pos.model.data.ClientData;
import com.pos.model.data.PaginatedResponse;
import com.pos.setup.AbstractIntegrationTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.*;

class ClientDtoSearchIT extends AbstractIntegrationTest {

    @Autowired ClientDto clientDto;
    @Autowired TestFactory factory;

    @Test
    void shouldSearchClients_happyFlow() throws Exception {
        factory.createClient("Acme", "a@acme.com");
        factory.createClient("Beta", "b@beta.com");
        flushAndClear();

        PaginatedResponse<ClientData> resp =
                clientDto.getClients(null, "  Ac  ", null, 0, 10);

        assertNotNull(resp);
        assertTrue(resp.getTotalCount() >= 1);
        assertTrue(resp.getData().stream().anyMatch(c -> c.getName().equals("Acme")));
    }
}