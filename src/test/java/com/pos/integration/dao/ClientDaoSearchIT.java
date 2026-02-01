package com.pos.integration.dao;

import com.pos.dao.ClientDao;
import com.pos.integration.AbstractMySqlIntegrationTest;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class ClientDaoSearchIT extends AbstractMySqlIntegrationTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    void getCount_shouldMatchSearchFilter() {
        for (int i = 1; i <= 5; i++) {
            Client c = new Client();
            c.setName("Alpha " + i);
            c.setEmail("alpha" + i + "@test.com");
            clientDao.insert(c);
        }

        Long count = clientDao.getCount(null, "Alpha", null);
        assertEquals(5L, count);

        List<Client> page0 = clientDao.search(null, "Alpha", null, 0, 2);
        List<Client> page1 = clientDao.search(null, "Alpha", null, 1, 2);
        List<Client> page2 = clientDao.search(null, "Alpha", null, 2, 2);

        assertEquals(2, page0.size());
        assertEquals(2, page1.size());
        assertEquals(1, page2.size());

        assertTrue(page0.stream().allMatch(c -> c.getName().contains("Alpha")));
    }

    @Test
    void search_shouldFilterById() {
        Client c1 = new Client();
        c1.setName("X");
        c1.setEmail("x@test.com");
        clientDao.insert(c1);

        Client c2 = new Client();
        c2.setName("Y");
        c2.setEmail("y@test.com");
        clientDao.insert(c2);

        List<Client> result = clientDao.search(c2.getId(), null, null, 0, 10);
        assertEquals(1, result.size());
        assertEquals(c2.getId(), result.get(0).getId());
    }
}
