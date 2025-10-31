package vn.iotstar.DAO.Impl;

import java.util.EmptyStackException;
import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;

import vn.iotstar.DAO.IUserDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Category;
import vn.iotstar.entities.User;
import vn.iotstar.entities.User.Role;

public class UserDaoImpl implements IUserDao {

	@Override
	public List<User> users(int page, int size, boolean isBanned) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        TypedQuery<User> query = em.createQuery("SELECT u FROM User u where u.isBanned is null or u.isBanned = :isBanned", User.class);
	        query.setFirstResult((page - 1) * size); // vị trí bắt đầu
	        query.setMaxResults(size);               // số bản ghi mỗi trang
	        return query.getResultList();
	    } finally {
	        em.close();
	    }
	}
	
	@Override
	public List<User> searchUsers(String keyword, Boolean banned, int page, int pageSize) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT u FROM User u WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(u.firstname) LIKE LOWER(:kw) OR LOWER(u.lastname) LIKE LOWER(:kw) OR LOWER(u.email) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND u.banned = :banned";
		    }

		    TypedQuery<User> query = em.createQuery(jpql, User.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (banned != null) {
		        query.setParameter("banned", banned);
		    }

		    query.setFirstResult((page - 1) * pageSize);
		    query.setMaxResults(pageSize);

		    return query.getResultList();
		}finally {
			em.close();
		}
		
	}
	@Override
	public int countUsers(String keyword, Boolean banned) {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT COUNT(u) FROM User u WHERE 1=1";
		    if (keyword != null && !keyword.isEmpty()) {
		        jpql += " AND (LOWER(u.firstname) LIKE LOWER(:kw) OR LOWER(u.lastname) LIKE LOWER(:kw) OR LOWER(u.email) LIKE LOWER(:kw))";
		    }
		    if (banned != null) {
		        jpql += " AND u.banned = :banned";
		    }

		    TypedQuery<Long> query = em.createQuery(jpql, Long.class);

		    if (keyword != null && !keyword.isEmpty()) {
		        query.setParameter("kw", "%" + keyword + "%");
		    }
		    if (banned != null) {
		        query.setParameter("banned", banned);
		    }

		    return query.getSingleResult().intValue();
		}finally {
			em.close();
		}
	    
	}

	@Override
	public void save(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.persist(user); // thêm mới user
	        em.flush(); // 🔥 ép Hibernate ghi user vào DB, sinh id ngay lập tức
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}


	@Override
	public void add(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.persist(user); // thêm mới user
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}

	@Override
	public void edit(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        em.merge(user); // cập nhật user
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}

	@Override
	public void delete(User user) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = em.getTransaction();
	    try {
	        tx.begin();
	        User u = em.find(User.class, user.getId());
	        if (u != null) {
	            em.remove(u); // xóa user khỏi DB
	        }
	        tx.commit();
	    } catch (Exception e) {
	        if (tx.isActive()) tx.rollback();
	        e.printStackTrace();
	    } finally {
	        em.close();
	    }
	}

	@Override
	public User findById(long id) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        return em.find(User.class, id); // tìm user theo ID
	    } finally {
	        em.close();
	    }
	}



	@Override
	public User findByEmail(String email) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        TypedQuery<User> query = em.createQuery(
	            "SELECT u FROM User u WHERE u.email = :email", User.class);
	        query.setParameter("email", email);
	        List<User> result = query.getResultList();
	        return result.isEmpty() ? null : result.get(0);
	    } finally {
	        em.close();
	    }
	}

	@Override
	public User findByPhone(String phone) {
	    EntityManager em = JPAConfig.getEntityManager();
	    try {
	        TypedQuery<User> query = em.createQuery(
	            "SELECT u FROM User u WHERE u.phone = :phone", User.class);
	        query.setParameter("phone", phone);
	        List<User> result = query.getResultList();
	        return result.isEmpty() ? null : result.get(0);
	    } finally {
	        em.close();
	    }
	}
	
	//Chức năng đăng ký có gửi otp qua mail//
	
	//save user == add user
	//findByEmail
	
	//Active email
	@Override
	public void activateEmail(Long userId) {
		EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user != null) {
                user.setIsEmailActive(true);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
	
	
	//------------------------------------//
	
	@Override
	public List<User> getAllVendorValidate() {
		EntityManager em = JPAConfig.getEntityManager();
		try {
			String jpql = "SELECT u FROM User u WHERE u.role = :role";
		    
		    
		    TypedQuery<User> query = em.createQuery(jpql, User.class);
		    query.setParameter("role", Role.VENDOR);

		    return query.getResultList();
		}finally {
			em.close();
		}
	}

	@Override
	public List<User> getAllVendorValidate(String keyword, int page, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
