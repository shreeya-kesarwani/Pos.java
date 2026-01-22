package com.pos.dao;

import com.pos.pojo.ClientPojo;
import com.pos.pojo.InventoryPojo;
import com.pos.pojo.ProductPojo;
import org.springframework.stereotype.Repository;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InventoryDao extends BaseDao {

    public List<InventoryPojo> search(String barcode, String productName, String clientName) {
        CriteriaBuilder cb = em().getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> cq = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> inventory = cq.from(InventoryPojo.class);

        List<Predicate> predicates = getPredicates(cb, cq, inventory, barcode, productName, clientName);
        cq.where(predicates.toArray(new Predicate[0]));

        return em().createQuery(cq).getResultList();
    }

    private List<Predicate> getPredicates(CriteriaBuilder cb, CriteriaQuery<?> cq, Root<InventoryPojo> inventory, String b, String pName, String cName) {
        List<Predicate> predicates = new ArrayList<>();

        // 1. Barcode or Product Name Filter (Subquery on Product table)
        if ((b != null && !b.isEmpty()) || (pName != null && !pName.isEmpty())) {
            Subquery<Integer> productSubquery = cq.subquery(Integer.class);
            Root<ProductPojo> productRoot = productSubquery.from(ProductPojo.class);

            List<Predicate> subPredicates = new ArrayList<>();
            subPredicates.add(cb.equal(productRoot.get("id"), inventory.get("productId")));

            if (b != null && !b.isEmpty()) {
                subPredicates.add(cb.equal(productRoot.get("barcode"), b));
            }
            if (pName != null && !pName.isEmpty()) {
                subPredicates.add(cb.like(productRoot.get("name"), "%" + pName + "%"));
            }

            productSubquery.select(productRoot.get("id")).where(subPredicates.toArray(new Predicate[0]));
            predicates.add(cb.exists(productSubquery));
        }

        // 2. Client Name Filter (Nested Subquery: Inventory -> Product -> Client)
        if (cName != null && !cName.isEmpty()) {
            Subquery<Integer> clientSubquery = cq.subquery(Integer.class);
            Root<ProductPojo> pRoot = clientSubquery.from(ProductPojo.class);
            Root<ClientPojo> cRoot = clientSubquery.from(ClientPojo.class);

            clientSubquery.select(pRoot.get("id")).where(
                    cb.equal(pRoot.get("id"), inventory.get("productId")),
                    cb.equal(pRoot.get("clientId"), cRoot.get("id")),
                    cb.equal(cRoot.get("name"), cName)
            );
            predicates.add(cb.exists(clientSubquery));
        }

        return predicates;
    }

}