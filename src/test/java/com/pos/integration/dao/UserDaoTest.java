package com.pos.integration.dao;

import com.pos.dao.UserDao;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserDao.class)
class UserDaoTest {

    @Container
    static final MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("pos_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", mysql::getJdbcUrl);
        r.add("spring.datasource.username", mysql::getUsername);
        r.add("spring.datasource.password", mysql::getPassword);

        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.format_sql", () -> "false");
        r.add("spring.jpa.properties.hibernate.jdbc.time_zone", () -> "UTC");
    }

    @Autowired private UserDao userDao;
    @Autowired private EntityManager em;

    @BeforeEach
    void clean() {
        em.createNativeQuery("DELETE FROM pos_user").executeUpdate();
        em.flush();
        em.clear();
    }

    private User seed(String email, String passwordHash, UserRole role) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setRole(role);

        em.persist(u);
        em.flush();
        em.clear();
        return u;
    }

    @Test
    void findByEmail_shouldReturnEmpty_whenNotFound() {
        Optional<User> out = userDao.findByEmail("missing@example.com");
        assertTrue(out.isEmpty());
    }

    @Test
    void findByEmail_shouldReturnUser_whenExists() {
        User u = seed("a@b.com", "hash1", UserRole.SUPERVISOR);

        Optional<User> out = userDao.findByEmail("a@b.com");

        assertTrue(out.isPresent());
        assertEquals(u.getId(), out.get().getId());
        assertEquals("a@b.com", out.get().getEmail());
        assertEquals(UserRole.SUPERVISOR, out.get().getRole());
    }

    @Test
    void findByEmail_shouldBeExactMatch_notLike() {
        seed("abc@x.com", "h1", UserRole.OPERATOR);
        seed("abc.def@x.com", "h2", UserRole.SUPERVISOR);

        Optional<User> out = userDao.findByEmail("abc@x.com");

        assertTrue(out.isPresent());
        assertEquals("abc@x.com", out.get().getEmail());
    }

    @Test
    void selectById_shouldReturnUser_whenExists() {
        User u = seed("id@x.com", "hash", UserRole.OPERATOR);

        User found = userDao.selectById(u.getId());

        assertNotNull(found);
        assertEquals(u.getId(), found.getId());
        assertEquals("id@x.com", found.getEmail());
    }

    @Test
    void selectById_shouldReturnNull_whenMissing() {
        assertNull(userDao.selectById(999999));
    }
}
