package com.pos.setup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@Transactional
@Tag("integration")
@ActiveProfiles("test")
@Execution(ExecutionMode.SAME_THREAD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfig.class)
public abstract class AbstractDaoTest {

    @Autowired
    protected EntityManager em;

    @BeforeEach
    void baseSetup() {
        em.clear();
    }

    protected <T> T persist(T entity) {
        em.persist(entity);
        em.flush();
        return entity;
    }
}