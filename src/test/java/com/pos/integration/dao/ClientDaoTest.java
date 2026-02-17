package com.pos.integration.dao;

import com.pos.dao.ClientDao;
import com.pos.pojo.Client;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ClientDao.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClientDaoTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        // ✅ IMPORTANT: ensure container is started before Spring tries to read mapped port/JDBC URL
        r.add("spring.datasource.url", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getJdbcUrl();
        });
        r.add("spring.datasource.username", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getUsername();
        });
        r.add("spring.datasource.password", () -> {
            if (!mysql.isRunning()) mysql.start();
            return mysql.getPassword();
        });

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
    }

    @Autowired private ClientDao clientDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        em.createQuery("DELETE FROM Client").executeUpdate();
        em.flush();
        em.clear();
    }

    private Client seed(String name, String email) {
        Client c = new Client();
        c.setName(name);
        c.setEmail(email);
        clientDao.insert(c);
        return c;
    }

    @Test
    void selectById_shouldReturnClient_whenExists() {
        Client c = seed("Acme", "a@acme.com");
        em.flush();
        em.clear();

        Client out = clientDao.selectById(c.getId());

        assertNotNull(out);
        assertEquals(c.getId(), out.getId());
        assertEquals("Acme", out.getName());
    }

    @Test
    void selectByName_shouldReturnNull_whenNameNull() {
        assertNull(clientDao.selectByName(null));
    }

    @Test
    void selectByName_shouldReturnClient_whenExists() {
        seed("Acme", "a@acme.com");
        seed("Other", "o@x.com");
        em.flush();
        em.clear();

        Client out = clientDao.selectByName("Acme");

        assertNotNull(out);
        assertEquals("Acme", out.getName());
    }

    @Test
    void selectByNames_shouldReturnEmpty_whenEmptyInput() {
        assertEquals(List.of(), clientDao.selectByNames(List.of()));
    }

    @Test
    void selectByNames_shouldReturnMatchingClients() {
        Client c1 = seed("Acme", "a@acme.com");
        Client c2 = seed("Beta", "b@beta.com");
        seed("Gamma", "g@g.com");
        em.flush();
        em.clear();

        List<Client> out = clientDao.selectByNames(List.of("Beta", "Acme"));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c1.getId())));
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c2.getId())));
    }

    @Test
    void selectByIds_shouldReturnEmpty_whenEmptyInput() {
        assertEquals(List.of(), clientDao.selectByIds(List.of()));
    }

    @Test
    void selectByIds_shouldReturnMatchingClients() {
        Client c1 = seed("Acme", "a@acme.com");
        Client c2 = seed("Beta", "b@beta.com");
        seed("Gamma", "g@g.com");
        em.flush();
        em.clear();

        List<Client> out = clientDao.selectByIds(List.of(c2.getId(), c1.getId()));

        assertEquals(2, out.size());
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c1.getId())));
        assertTrue(out.stream().anyMatch(c -> c.getId().equals(c2.getId())));
    }

    @Test
    void searchByParams_shouldFilter_andPaginate() {
        seed("Acme", "a@acme.com");     // match
        seed("Acme Two", "a2@acme.com");// match
        seed("Beta", "b@beta.com");    // no match for name=Acme
        em.flush();
        em.clear();

        List<Client> page0 = clientDao.searchByParams(null, "Acme", null, 0, 1);
        List<Client> page1 = clientDao.searchByParams(null, "Acme", null, 1, 1);

        assertEquals(1, page0.size());
        assertEquals(1, page1.size());
        assertNotEquals(page0.get(0).getId(), page1.get(0).getId());
    }

    @Test
    void getCount_shouldMatchFilters() {
        seed("Acme", "a@acme.com");
        seed("Acme Two", "a2@acme.com");
        seed("Beta", "b@beta.com");
        em.flush();
        em.clear();

        assertEquals(2L, clientDao.getCount(null, "Acme", null));
        assertEquals(1L, clientDao.getCount(null, "Beta", null));
        assertEquals(1L, clientDao.getCount(null, null, "beta.com"));
    }
}
