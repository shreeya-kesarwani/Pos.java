package com.pos.auth.integration;

import com.pos.dao.UserDao;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import({UserDao.class})
class UserDaoTest extends AbstractDaoTest {

    @Autowired
    private UserDao userDao;

    private User operatorUser;
    private User supervisorUser;

    @BeforeEach
    void setupData() {
        operatorUser = TestEntities.newUser("a@b.com", "hash", UserRole.OPERATOR);
        userDao.insert(operatorUser);

        supervisorUser = TestEntities.newUser("id@b.com", "hash", UserRole.SUPERVISOR);
        userDao.insert(supervisorUser);

        em.clear();
    }

    @Test
    void findByEmailReturnsUserWhenExists() {
        Optional<User> out = userDao.findByEmail("a@b.com");

        assertTrue(out.isPresent());
        assertEquals(operatorUser.getId(), out.get().getId());
        assertEquals("a@b.com", out.get().getEmail());
        assertEquals(UserRole.OPERATOR, out.get().getRole());
    }

    @Test
    void findByEmailReturnsEmptyWhenMissing() {
        Optional<User> out = userDao.findByEmail("missing@b.com");
        assertTrue(out.isEmpty());
    }

    @Test
    void findByEmailReturnsEmptyWhenEmailNull() {
        Optional<User> out = userDao.findByEmail(null);
        assertTrue(out.isEmpty());
    }

    @Test
    void findByEmailReturnsEmptyWhenEmailBlank() {
        Optional<User> out = userDao.findByEmail("   ");
        assertTrue(out.isEmpty());
    }

    @Test
    void selectByIdReturnsUserWhenExists() {
        User out = userDao.selectById(supervisorUser.getId());

        assertNotNull(out);
        assertEquals(supervisorUser.getId(), out.getId());
        assertEquals("id@b.com", out.getEmail());
        assertEquals(UserRole.SUPERVISOR, out.getRole());
    }

    @Test
    void selectByIdReturnsNullWhenMissing() {
        assertNull(userDao.selectById(999));
    }

    @Test
    void selectByIdThrowsWhenIdNull() {
        assertThrows(IllegalArgumentException.class, () -> userDao.selectById(null));
    }
}