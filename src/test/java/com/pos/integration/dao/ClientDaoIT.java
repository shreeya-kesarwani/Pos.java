package com.pos.integration.dao;

import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.dao.ClientDao;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClientDaoIT extends AbstractMySqlIntegrationTest {

    @Autowired private ClientDao clientDao;

    @Test
    void insertAndSelect_shouldWork() {
        Client c = new Client();
        c.setName("TestClient");

        clientDao.insert(c);
        assertNotNull(c.getId());

        Client fetched = clientDao.select(c.getId(), Client.class);
        assertNotNull(fetched);
        assertEquals("TestClient", fetched.getName());
    }
}
