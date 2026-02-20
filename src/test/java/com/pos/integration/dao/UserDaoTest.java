package com.pos.integration.dao;

import com.pos.dao.UserDao;
import com.pos.integration.setup.AbstractDaoTest;
import com.pos.integration.setup.TestEntities;
import com.pos.model.constants.UserRole;
import com.pos.pojo.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Import(UserDao.class)
class UserDaoTest extends AbstractDaoTest {

    @Autowired
    private UserDao dao;

    @Test
    void findByEmailWhenExists() {
        User u = persist(TestEntities.user("a@b.com", "hash", UserRole.OPERATOR));
        em.clear();

        Optional<User> out = dao.findByEmail("a@b.com");

        assertTrue(out.isPresent());
        assertEquals(u.getId(), out.get().getId());
    }

    @Test
    void selectByIdWhenMissing() {
        assertNull(dao.selectById(999));
    }
}