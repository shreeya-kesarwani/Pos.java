package com.pos.product.integration;

import com.pos.dao.ProductDao;
import com.pos.setup.AbstractDaoTest;
import com.pos.setup.TestEntities;
import com.pos.pojo.Product;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;

import static org.junit.jupiter.api.Assertions.*;

@Import(ProductDao.class)
class ProductDaoTest extends AbstractDaoTest {

    @Autowired
    private ProductDao dao;

    @Test
    void selectByIdWhenExists() {
        Product p = persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        em.clear();

        assertEquals(p.getId(), dao.selectById(p.getId()).getId());
    }

    @Test
    void searchWhenFiltersProvided() {
        persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        persist(TestEntities.product("B3", "Milk", 2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.search("soap", null, 1, 0, 10).size());
    }

    @Test
    void getCountWhenFiltered() {
        persist(TestEntities.product("B1", "Soap", 1, 100.0, "img"));
        persist(TestEntities.product("B2", "Soap Deluxe", 1, 120.0, "img"));
        persist(TestEntities.product("B3", "Milk", 2, 50.0, "img"));
        em.clear();

        assertEquals(2, dao.getCount("soap", null, 1));
    }
}