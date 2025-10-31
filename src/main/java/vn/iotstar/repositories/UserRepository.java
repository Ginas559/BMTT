// filepath: src/main/java/vn/iotstar/repositories/UserRepository.java
package vn.iotstar.repositories;

import jakarta.persistence.*;
import vn.iotstar.entities.User;

public class UserRepository {

    // Đã XÓA: private static final EntityManagerFactory emf = ...

    /**
     * Helper mới: Lấy EntityManager từ JPAConfig
     */
    private EntityManager em() {
        return vn.iotstar.configs.JPAConfig.getEntityManagerFactory().createEntityManager();
    }

    public boolean existsByEmail(String email) {
        // Dùng try-with-resources (tự động close em)
        try (EntityManager em = em()) {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            q.setParameter("email", email.toLowerCase());
            return q.getSingleResult() > 0L;
        }
    }

    public boolean existsByPhone(String phone) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.phone = :phone", Long.class);
            q.setParameter("phone", phone);
            return q.getSingleResult() > 0L;
        }
    }

    public User findByEmail(String email) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email.toLowerCase());
            q.setMaxResults(1);
            return q.getResultList().stream().findFirst().orElse(null);
        }
    }

    public User findById(Long id) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            return em.find(User.class, id);
        }
    }

    public void save(User user) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(user);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    public void update(User user) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(user);
                tx.commit();
            } catch (RuntimeException e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }
}