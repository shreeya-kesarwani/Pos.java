package com.pos.integration.dao;

import com.pos.dao.ClientDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.pojo.Client;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import(ClientDao.class)
class ClientDaoTest extends AbstractDaoTest {

    @Autowired
    private ClientDao clientDao;

    @Test
    void selectByIdWhenExists() {
        Client c = persist(TestEntities.client("Acme", "a@acme.com"));
        em.clear();

        Client out = clientDao.selectById(c.getId());

        assertNotNull(out);
        assertEquals(c.getId(), out.getId());
    }

    @Test
    void selectByIdWhenNotFound() {
        assertNull(clientDao.selectById(999));
    }

    @Test
    void selectByNameWhenExists() {
        persist(TestEntities.client("Acme", "a@acme.com"));
        em.clear();

        Client out = clientDao.selectByName("Acme");

        assertNotNull(out);
        assertEquals("Acme", out.getName());
    }

    @Test
    void selectByNameWhenNull() {
        assertNull(clientDao.selectByName(null));
    }

    @Test
    void selectByNamesWhenMatch() {
        Client c1 = persist(TestEntities.client("Acme", "a@acme.com"));
        Client c2 = persist(TestEntities.client("Beta", "b@beta.com"));
        em.clear();

        List<Client> out = clientDao.selectByNames(List.of("Acme", "Beta"));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c1.getId())));
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c2.getId())));
    }

    @Test
    void selectByNamesWhenEmpty() {
        assertEquals(List.of(), clientDao.selectByNames(List.of()));
    }

    @Test
    void selectByIdsWhenMatch() {
        Client c1 = persist(TestEntities.client("Acme", "a@acme.com"));
        Client c2 = persist(TestEntities.client("Beta", "b@beta.com"));
        em.clear();

        List<Client> out = clientDao.selectByIds(List.of(c1.getId(), c2.getId()));

        assertEquals(2, out.size());
    }

    @Test
    void selectByIdsWhenEmpty() {
        assertEquals(List.of(), clientDao.selectByIds(List.of()));
    }

    @Test
    void searchByParamsWhenNameFilter() {
        persist(TestEntities.client("Acme One", "a1@acme.com"));
        persist(TestEntities.client("Acme Two", "a2@acme.com"));
        persist(TestEntities.client("Beta", "b@beta.com"));
        em.clear();

        List<Client> out = clientDao.searchByParams(null, "Acme", null, 0, 10);

        assertEquals(2, out.size());
    }

    @Test
    void getCountWhenNameFilter() {
        persist(TestEntities.client("Acme One", "a1@acme.com"));
        persist(TestEntities.client("Acme Two", "a2@acme.com"));
        persist(TestEntities.client("Beta", "b@beta.com"));
        em.clear();

        long count = clientDao.getCount(null, "Acme", null);

        assertEquals(2L, count);
    }
}