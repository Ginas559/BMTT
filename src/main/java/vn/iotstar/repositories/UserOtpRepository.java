// filepath: src/main/java/vn/iotstar/repositories/UserOtpRepository.java
package vn.iotstar.repositories;

import jakarta.persistence.*;
import vn.iotstar.entities.User;
import vn.iotstar.entities.UserOtp;

import java.time.LocalDateTime;
import java.util.List;

public class UserOtpRepository {

    private static final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("dataSource");

    public void save(UserOtp otp) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(otp);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public UserOtp findLatest(User user, UserOtp.Purpose purpose) {
        EntityManager em = emf.createEntityManager();
        try {
            List<UserOtp> list = em.createQuery("""
                    SELECT o FROM UserOtp o
                    WHERE o.user = :u AND o.purpose = :p
                    ORDER BY o.createdAt DESC
                    """, UserOtp.class)
                .setParameter("u", user)
                .setParameter("p", purpose)
                .setMaxResults(1)
                .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    public UserOtp findValid(User user, UserOtp.Purpose purpose, String code, LocalDateTime now) {
        EntityManager em = emf.createEntityManager();
        try {
            List<UserOtp> list = em.createQuery("""
                    SELECT o FROM UserOtp o
                    WHERE o.user = :u AND o.purpose = :p AND o.code = :c AND o.expiresAt >= :now
                    ORDER BY o.createdAt DESC
                    """, UserOtp.class)
                .setParameter("u", user)
                .setParameter("p", purpose)
                .setParameter("c", code)
                .setParameter("now", now)
                .setMaxResults(1)
                .getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    public void deleteByUserAndPurpose(User user, UserOtp.Purpose purpose) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("""
                    DELETE FROM UserOtp o
                    WHERE o.user = :u AND o.purpose = :p
                    """)
              .setParameter("u", user)
              .setParameter("p", purpose)
              .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void increaseAttempts(Long otpId) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.createQuery("UPDATE UserOtp o SET o.attempts = o.attempts + 1 WHERE o.id = :id")
              .setParameter("id", otpId)
              .executeUpdate();
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
