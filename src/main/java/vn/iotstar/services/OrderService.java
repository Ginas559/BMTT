// src/main/java/vn/iotstar/services/OrderService.java

package vn.iotstar.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.util.List;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Order;

public class OrderService {

    // ⭐ NEW: Lớp đóng gói kết quả phân trang
    public static class PageResult<T> {
        public final List<T> items;
        public final int page, size, totalPages;
        public final long totalItems;

        /**
         * @param items Danh sách các mục cho trang hiện tại.
         * @param page Trang hiện tại (bắt đầu từ 1).
         * @param size Kích thước trang.
         * @param totalItems Tổng số mục.
         */
        public PageResult(List<T> items, int page, int size, long totalItems) {
            this.items = items; this.page = page; this.size = size;
            this.totalItems = totalItems;
            // Tính toán tổng số trang, đảm bảo ít nhất là 1
            this.totalPages = (int) Math.max(1, (totalItems + size - 1) / size);
        }
    }

    // ⭐ NEW: Phân trang + Lọc trạng thái + Tìm theo tên khách hàng
    /**
     * Lấy danh sách đơn hàng theo Shop, hỗ trợ phân trang, lọc trạng thái và tìm kiếm theo tên khách hàng.
     * @param shopId ID của Shop.
     * @param statusNullable Trạng thái lọc (null/blank để lấy tất cả).
     * @param qNullable Chuỗi tìm kiếm theo tên/email khách hàng (null/blank để không tìm kiếm).
     * @param page Trang hiện tại (bắt đầu từ 1).
     * @param size Kích thước trang.
     * @return Đối tượng PageResult chứa danh sách đơn hàng và thông tin phân trang.
     */
    public PageResult<Order> findByShopPaged(Long shopId, String statusNullable, String qNullable, int page, int size) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            // Đảm bảo giá trị phân trang hợp lệ
            if (page < 1) page = 1;
            if (size < 1) size = 10;

            // Xây dựng điều kiện WHERE
            StringBuilder where = new StringBuilder(" WHERE o.shop.shopId=:sid ");
            if (statusNullable != null && !statusNullable.isBlank()) where.append(" AND o.status=:st ");
            
            boolean hasQ = qNullable != null && !qNullable.isBlank();
            if (hasQ) {
                // Điều kiện tìm kiếm theo tên/email khách hàng (không phân biệt chữ hoa/thường)
                where.append(" AND (")
                     .append("LOWER(CONCAT(COALESCE(u.firstname,''),' ',COALESCE(u.lastname,''))) LIKE :kw ") // Full Name
                     .append("OR LOWER(u.firstname) LIKE :kw OR LOWER(u.lastname) LIKE :kw OR LOWER(u.email) LIKE :kw")
                     .append(") ");
            }
            
            String keyword = hasQ ? "%" + qNullable.toLowerCase().trim() + "%" : null;
            Order.OrderStatus statusEnum = (statusNullable != null && !statusNullable.isBlank())
                    ? Order.OrderStatus.valueOf(statusNullable) : null;

            // 1. Truy vấn COUNT (Tổng số mục)
            TypedQuery<Long> cq = em.createQuery(
                    "SELECT COUNT(o) FROM Order o JOIN o.user u" + where, Long.class);
            cq.setParameter("sid", shopId);
            if (statusEnum != null) cq.setParameter("st", statusEnum);
            if (hasQ) cq.setParameter("kw", keyword);
            long total = cq.getSingleResult();

            // 2. Truy vấn GET PAGE (Lấy dữ liệu cho trang)
            TypedQuery<Order> q = em.createQuery(
                    "SELECT o FROM Order o JOIN FETCH o.user u" + where + " ORDER BY o.createdAt DESC", Order.class);
            q.setParameter("sid", shopId);
            if (statusEnum != null) q.setParameter("st", statusEnum);
            if (hasQ) q.setParameter("kw", keyword);

            // Thiết lập phân trang
            q.setFirstResult((page - 1) * size);
            q.setMaxResults(size);
            List<Order> items = q.getResultList();

            return new PageResult<>(items, page, size, total);
        } finally { 
            em.close(); 
        }
    }
    
    // Giữ nguyên các hàm cũ
    public List<Order> getOrdersByStatus(Long shopId, String statusNullable) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            if (statusNullable == null || statusNullable.isBlank()) {
                return em.createQuery(
                                "SELECT o FROM Order o " +
                                        "JOIN FETCH o.user u " +
                                        "WHERE o.shop.shopId = :sid " +
                                        "ORDER BY o.createdAt DESC", Order.class)
                        .setParameter("sid", shopId)
                        .getResultList();
            } else {
                Order.OrderStatus st = Order.OrderStatus.valueOf(statusNullable);
                return em.createQuery(
                                "SELECT o FROM Order o " +
                                        "JOIN FETCH o.user u " +
                                        "WHERE o.shop.shopId = :sid AND o.status = :st " +
                                        "ORDER BY o.createdAt DESC", Order.class)
                        .setParameter("sid", shopId)
                        .setParameter("st", st)
                        .getResultList();
            }
        } finally {
            em.close();
        }
    }

    public void updateStatus(Long orderId, String newStatus) {
        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Order o = em.find(Order.class, orderId);
            if (o == null) throw new RuntimeException("Không tìm thấy đơn hàng");
            o.setStatus(Order.OrderStatus.valueOf(newStatus));
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Lấy 1 đơn hàng theo ID, nạp sẵn quan hệ để dùng ngay trong servlet/JSP. */
    public Order findById(Long orderId) {
        if (orderId == null) return null;
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM Order o " +
                                    "JOIN FETCH o.shop s " +
                                    "JOIN FETCH o.user u " +
                                    "WHERE o.orderId = :id", Order.class)
                    .setParameter("id", orderId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }

    /** Ràng buộc đơn thuộc đúng shop. */
    public Order findByIdForShop(Long orderId, Long shopId) {
        if (orderId == null || shopId == null) return null;
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT o FROM Order o " +
                                    "JOIN FETCH o.shop s " +
                                    "JOIN FETCH o.user u " +
                                    "WHERE o.orderId = :id AND s.shopId = :sid", Order.class)
                    .setParameter("id", orderId)
                    .setParameter("sid", shopId)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        } finally {
            em.close();
        }
    }
}