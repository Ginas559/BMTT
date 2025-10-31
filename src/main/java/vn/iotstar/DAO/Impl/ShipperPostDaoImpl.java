package vn.iotstar.DAO.Impl;

import java.util.List;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import vn.iotstar.DAO.IShipperPostDao;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.ShipperPost;

public class ShipperPostDaoImpl implements IShipperPostDao {
	
	@Override
	public List<ShipperPost> shipperPosts(int page, int pageSize) {
	    EntityManager em = JPAConfig.getEntityManager();
	    
	    // JPQL: Tải Shipper (User) cùng lúc và sắp xếp theo ngày tạo mới nhất
	    String jpql = "SELECT sp FROM ShipperPost sp JOIN FETCH sp.shipper s ORDER BY sp.createdAt DESC";
	    
	    // Không cần Transaction nếu đây là truy vấn SELECT thuần
	    // (Hibernate/JPA sẽ quản lý Read-Only Transaction ngầm định)
	    
	    try {
	        TypedQuery<ShipperPost> query = em.createQuery(jpql, ShipperPost.class);
	        
	        // --- LOGIC PHÂN TRANG CHO TẢI THÊM KHI CUỘN (Infinite Scroll) ---
	        
	        // 1. Tính toán vị trí bắt đầu
	        int startPosition = (page - 1) * pageSize; 
	        
	        // 2. Thiết lập giới hạn kết quả
	        query.setFirstResult(startPosition); // Vị trí bắt đầu
	        query.setMaxResults(pageSize);        // Số lượng kết quả
	        
	        // 3. Thực thi truy vấn
	        return query.getResultList();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Xử lý ngoại lệ thích hợp
	        throw new RuntimeException("Lỗi khi tải bài đăng Shipper: " + e.getMessage(), e);
	    } finally {
	        if (em != null) {
	            em.close();
	        }
	    }
	}
	
	
	@Override
	public int countShipperPosts() {
	    EntityManager em = JPAConfig.getEntityManager();
	    
	    // JPQL: Tải Shipper (User) cùng lúc và sắp xếp theo ngày tạo mới nhất
	    String jpql = "SELECT COUNT(sp) FROM ShipperPost sp";
	    
	    // Không cần Transaction nếu đây là truy vấn SELECT thuần
	    // (Hibernate/JPA sẽ quản lý Read-Only Transaction ngầm định)
	    
	    try {
	        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
	        
	        // 3. Thực thi truy vấn
	        return query.getSingleResult().intValue();
	        
	    } catch (Exception e) {
	        e.printStackTrace();
	        // Xử lý ngoại lệ thích hợp
	        throw new RuntimeException("Lỗi khi tải bài đăng Shipper: " + e.getMessage(), e);
	    } finally {
	        if (em != null) {
	            em.close();
	        }
	    }
	}
	@Override
	public void addPost(ShipperPost post) {
	    EntityManager em = JPAConfig.getEntityManager();
	    EntityTransaction tx = null; // Khai báo Transaction

	    try {
	        tx = em.getTransaction();
	        tx.begin(); // 1. Bắt đầu Transaction

	        // 2. Lưu Entity mới vào cơ sở dữ liệu
	        // persist() được dùng cho các entity mới chưa có ID
	        em.persist(post); 

	        tx.commit(); // 3. Ghi nhận thay đổi vào DB

	    } catch (Exception e) {
	        // Xử lý lỗi nếu Transaction đang hoạt động thì thực hiện rollback
	        if (tx != null && tx.isActive()) {
	            tx.rollback();
	        }
	        e.printStackTrace();
	        // Ném lại ngoại lệ để lớp Controller có thể bắt và xử lý thông báo
	        throw new RuntimeException("Lỗi khi đăng bài: " + e.getMessage(), e);
	        
	    } finally {
	        // Đảm bảo EntityManager được đóng
	        if (em != null) {
	            em.close();
	        }
	    }
	}
}
