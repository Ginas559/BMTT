package vn.iotstar.services;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.User;
import vn.iotstar.entities.ViewedProduct;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

public class ViewedService {

    /**
     * Ghi nhận việc xem sản phẩm (userId, productId).
     * - Nếu đã có bản ghi (user, product): update viewed_at = now.
     * - Nếu chưa có: tạo bản ghi mới.
     * - KHÔNG còn prune (xóa bớt) — để trang "Xem tất cả" có thể hiển thị toàn bộ lịch sử.
     */
    public void touch(Long userId, Long productId) {
        if (userId == null || productId == null) return;

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Tìm bản ghi hiện có
            TypedQuery<ViewedProduct> q = em.createQuery(
                "SELECT vp FROM ViewedProduct vp " +
                "WHERE vp.user.id = :uid AND vp.product.productId = :pid",
                ViewedProduct.class
            );
            q.setParameter("uid", userId);
            q.setParameter("pid", productId);

            ViewedProduct vp = q.getResultStream().findFirst().orElse(null);

            if (vp == null) {
                // Tạo mới (dùng reference để tránh load full)
                User userRef = em.getReference(User.class, userId);
                Product productRef = em.getReference(Product.class, productId);

                vp = new ViewedProduct();
                vp.setUser(userRef);
                vp.setProduct(productRef);
                vp.setViewedAt(LocalDateTime.now());
                em.persist(vp);
            } else {
                // Cập nhật thời gian xem gần nhất
                vp.setViewedAt(LocalDateTime.now());
                em.merge(vp);
            }

            tx.commit();
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            System.out.println("[ViewedService.touch] ERROR: " + ex.getMessage());
        } finally {
            em.close();
        }
    }

    /**
     * Lấy danh sách sản phẩm user xem gần đây, mới nhất trước.
     * - Nếu limit == null hoặc limit <= 0  -> trả về TOÀN BỘ (không giới hạn).
     * - Nếu limit > 0                     -> giới hạn số lượng theo limit.
     *
     * Gợi ý dùng:
     * - Chi tiết sản phẩm: recentByUser(userId, 6)  // chỉ 6
     * - Trang "Xem tất cả": recentByUser(userId, 0) // toàn bộ
     */
    public List<Product> recentByUser(Long userId, Integer limit) {
        if (userId == null) return List.of();

        EntityManager em = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT vp.product FROM ViewedProduct vp " +
                "WHERE vp.user.id = :uid " +
                "ORDER BY vp.viewedAt DESC",
                Product.class
            );
            q.setParameter("uid", userId);

            if (limit != null && limit > 0) {
                q.setMaxResults(limit);
            }

            List<Product> list = q.getResultList();
            System.out.println("[ViewedService.recentByUser] uid=" + userId + " -> " + list.size()
                    + (limit != null ? (" (limit=" + limit + ")") : " (no-limit)"));
            return list;
        } catch (Exception ex) {
            System.out.println("[ViewedService.recentByUser] ERROR: " + ex.getMessage());
            return List.of();
        } finally {
            em.close();
        }
    }
}
