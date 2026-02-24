package com.pos.client.integration.dao;

import com.pos.dao.ClientDao;
import com.pos.pojo.Client;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({ClientDao.class, TestFactory.class})
class ClientDaoTest extends AbstractDaoTest {

    @Autowired
    private ClientDao clientDao;

    @Autowired
    private TestFactory testFactory;

    private String acmeName;
    private String betaName;

    @BeforeEach
    void setupData() {
        acmeName = "Acme";
        betaName = "Beta";
    }

    @Test
    void selectByIdWhenExists() {
        Client c = testFactory.createClient(acmeName, "a@acme.com");
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
        testFactory.createClient(acmeName, "a@acme.com");
        em.clear();

        Client out = clientDao.selectByName(acmeName);

        assertNotNull(out);
        assertEquals(acmeName, out.getName());
    }

    @Test
    void selectByNameWhenNotFound() {
        assertNull(clientDao.selectByName("Missing"));
    }

    @Test
    void selectByNameWhenNull() {
        assertNull(clientDao.selectByName(null));
    }

    @Test
    void selectByNamesWhenMatch() {
        Client c1 = testFactory.createClient(acmeName, "a@acme.com");
        Client c2 = testFactory.createClient(betaName, "b@beta.com");
        em.clear();

        List<Client> out = clientDao.selectByNames(List.of(acmeName, betaName));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c1.getId())));
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c2.getId())));
    }

    @Test
    void selectByNamesIgnoresMissingNames() {
        testFactory.createClient(acmeName, "a@acme.com");
        em.clear();

        List<Client> out = clientDao.selectByNames(List.of(acmeName, "Missing"));

        assertEquals(1, out.size());
        assertEquals(acmeName, out.getFirst().getName());
    }

    @Test
    void selectByNamesWhenEmpty() {
        assertEquals(List.of(), clientDao.selectByNames(List.of()));
    }

    @Test
    void selectByIdsWhenMatch() {
        Client c1 = testFactory.createClient(acmeName, "a@acme.com");
        Client c2 = testFactory.createClient(betaName, "b@beta.com");
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
        testFactory.createClient("Acme One", "a1@acme.com");
        testFactory.createClient("Acme Two", "a2@acme.com");
        testFactory.createClient(betaName, "b@beta.com");
        em.clear();

        List<Client> out = clientDao.searchByParams(null, "Acme", null, 0, 10);

        assertEquals(2, out.size());
    }

    @Test
    void searchByParamsWhenEmailFilter() {
        testFactory.createClient(acmeName, "a@acme.com");
        testFactory.createClient(betaName, "beta@beta.com");
        em.clear();

        List<Client> out = clientDao.searchByParams(null, null, "beta@", 0, 10);

        assertEquals(1, out.size());
        assertEquals(betaName, out.getFirst().getName());
    }

    @Test
    void getCountWhenNameFilter() {
        testFactory.createClient("Acme One", "a1@acme.com");
        testFactory.createClient("Acme Two", "a2@acme.com");
        testFactory.createClient(betaName, "b@beta.com");
        em.clear();

        long count = clientDao.getCount(null, "Acme", null);

        assertEquals(2L, count);
    }
}