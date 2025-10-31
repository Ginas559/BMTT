//src/main/java/vn/iotstar/services/CategoryService.java
package vn.iotstar.services;

import jakarta.persistence.EntityManager;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import java.util.List;

public class CategoryService {
    public List<Category> findAll() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.categoryName", Category.class)
                     .getResultList();
        } finally { em.close(); }
    }
}
