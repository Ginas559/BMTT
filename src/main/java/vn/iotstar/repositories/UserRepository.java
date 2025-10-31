// tung - filepath: src/main/java/vn/iotstar/repositories/UserRepository.java
package vn.iotstar.repositories;

import jakarta.persistence.*;
import vn.iotstar.entities.User;

public class UserRepository {

    private static final EntityManagerFactory emf = Persistence.createEntityManagerFactory("dataSource");

    public boolean existsByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class);
            q.setParameter("email", email.toLowerCase());
            return q.getSingleResult() > 0L;
        } finally {
            em.close();
        }
    }

    public boolean existsByPhone(String phone) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<Long> q = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.phone = :phone", Long.class);
            q.setParameter("phone", phone);
            return q.getSingleResult() > 0L;
        } finally {
            em.close();
        }
    }

    public User findByEmail(String email) {
        EntityManager em = emf.createEntityManager();
        try {
            TypedQuery<User> q = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
            q.setParameter("email", email.toLowerCase());
            q.setMaxResults(1);
            return q.getResultList().stream().findFirst().orElse(null);
        } finally {
            em.close();
        }
    }

    public User findById(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(User.class, id);
        } finally {
            em.close();
        }
    }

    public void save(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void update(User user) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
