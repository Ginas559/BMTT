package vn.iotstar.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import vn.iotstar.dto.FavoriteItem;
import vn.iotstar.entities.Favorite;
import vn.iotstar.entities.User;
import vn.iotstar.entities.Product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * FavoriteService
 * - Sửa lỗi UnknownPathException: dùng entity reference thay vì f.user.userId
 * - Lấy danh sách yêu thích kèm ảnh thumbnail (ưu tiên is_thumbnail = 1)
 * - Sửa đường dẫn ảnh "/assset/" -> "/assets/" ngay trong SQL để an toàn
 * - Giữ nguyên toàn bộ logic khác
 */
public class FavoriteService {

    private static volatile EntityManagerFactory EMF;

    private static EntityManagerFactory emf() {
        if (EMF == null) {
            synchronized (FavoriteService.class) {
                if (EMF == null) {
                    EMF = Persistence.createEntityManagerFactory("dataSource");
                }
            }
        }
        return EMF;
    }

    private EntityManager em() {
        return emf().createEntityManager();
    }

    /* ========================= READ-ONLY METHODS =========================== */

    /** Trả true nếu user đã yêu thích product */
    public boolean isFav(long userId, long productId) {
        EntityManager em = em();
        try {
            // Dùng entity reference (User uRef) thay vì cố truy cập uRef.userId trong JPQL
            User uRef = em.getReference(User.class, userId);
            Product pRef = em.getReference(Product.class, productId);
            Long cnt = em.createQuery(
                    "SELECT COUNT(f) FROM Favorite f WHERE f.user = :u AND f.product = :p",
                    Long.class
            )
            .setParameter("u", uRef)
            .setParameter("p", pRef)
            .getSingleResult();
            return cnt != null && cnt > 0;
        } finally {
            em.close();
        }
    }

    /** Đếm tổng số người đã yêu thích 1 sản phẩm */
    public long countByProduct(long productId) {
        EntityManager em = em();
        try {
            Product pRef = em.getReference(Product.class, productId);
            Long cnt = em.createQuery(
                    "SELECT COUNT(f) FROM Favorite f WHERE f.product = :p",
                    Long.class
            )
            .setParameter("p", pRef)
            .getSingleResult();
            return (cnt == null) ? 0L : cnt;
        } finally {
            em.close();
        }
    }

    /** Lấy danh sách yêu thích của user (mới nhất trước) */
    @SuppressWarnings("unchecked")
    public List<FavoriteItem> listByUser(long userId) {
        EntityManager em = em();
        try {
            String sql =
                "SELECT p.product_id, p.product_name, p.price, p.discount_price, " +
                // Kết hợp: fix lỗi chính tả '/assset/' thành '/assets/' (từ commit 7786f02...)
                "       COALESCE(REPLACE(pi.image_url, '/assset/', '/assets/'), '') AS image_url " +
                "FROM Favorite f " +
                "JOIN Product p ON p.product_id = f.product_id " +
                "OUTER APPLY ( " +
                "   SELECT TOP 1 image_url " +
                "   FROM Product_Image " +
                "   WHERE product_id = p.product_id " +
                "   ORDER BY CASE WHEN is_thumbnail = 1 THEN 0 ELSE 1 END, image_id ASC " +
                ") pi " +
                "WHERE f.user_id = :uid " +
                // Kết hợp: Sắp xếp an toàn hơn (từ commit 7786f02...)
                "ORDER BY ISNULL(f.created_at, '1900-01-01') DESC, f.favorite_id DESC";

            Query q = em.createNativeQuery(sql);
            q.setParameter("uid", userId);

            List<Object[]> rows = q.getResultList();
            List<FavoriteItem> list = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                long pid = ((Number) r[0]).longValue();
                String name = (r[1] != null) ? r[1].toString() : "";
                BigDecimal price = (r[2] instanceof BigDecimal) ? (BigDecimal) r[2] : null;
                BigDecimal dprice = (r[3] instanceof BigDecimal) ? (BigDecimal) r[3] : null;
                String img = (r[4] != null) ? r[4].toString() : "";

                list.add(new FavoriteItem(pid, name, price, dprice, img));
            }
            return list;
        } finally {
            em.close();
        }
    }

    /* ========================= WRITE METHODS =============================== */

    /** Toggle yêu thích */
    public boolean toggle(long userId, long productId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User uRef = em.getReference(User.class, userId);
            Product pRef = em.getReference(Product.class, productId);

            List<Favorite> exists = em.createQuery(
                    "SELECT f FROM Favorite f WHERE f.user = :u AND f.product = :p",
                    Favorite.class
            )
            .setParameter("u", uRef)
            .setParameter("p", pRef)
            .getResultList();

            boolean nowFav;
            if (!exists.isEmpty()) {
                for (Favorite f : exists) {
                    em.remove(em.contains(f) ? f : em.merge(f));
                }
                nowFav = false;
            } else {
                Favorite fav = Favorite.builder()
                        .user(uRef)
                        .product(pRef)
                        .createdAt(LocalDateTime.now())
                        .build();
                em.persist(fav);
                nowFav = true;
            }

            tx.commit();
            return nowFav;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}