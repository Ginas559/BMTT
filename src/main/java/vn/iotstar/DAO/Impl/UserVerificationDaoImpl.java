package vn.iotstar.DAO.Impl;

import java.time.LocalDateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import vn.iotstar.DAO.IUserVerificationDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.User;
import vn.iotstar.entities.UserVerification;

public class UserVerificationDaoImpl implements IUserVerificationDao {
	@Override
	public void save(UserVerification verification) {
		EntityManager em = JPAConfig.getEntityManager();
		EntityTransaction tx = em.getTransaction();
		try {
			tx.begin();
			verification.setUser(em.merge(verification.getUser())); // 🔥 dòng quan trọng
			em.persist(verification);
			tx.commit();
		} catch (Exception e) {
			if (tx.isActive())
				tx.rollback();
			throw e;
		} finally {
			em.close();
		}
	}

	@Override
	public UserVerification findValidOTP(User user, String otp, UserVerification.VerificationType type) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			return em
					.createQuery("SELECT v FROM UserVerification v WHERE v.user = :user AND v.otpCode = :otp "
							+ "AND v.type = :type AND v.expireTime > :now", UserVerification.class)
					.setParameter("user", user).setParameter("otp", otp).setParameter("type", type)
					.setParameter("now", LocalDateTime.now()).getResultStream().findFirst().orElse(null);
		} finally {
			em.close();
		}
	}
}
