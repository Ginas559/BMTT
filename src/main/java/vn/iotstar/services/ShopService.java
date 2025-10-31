// src/main/java/vn/iotstar/services/ShopService.java
package vn.iotstar.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Shop;

import java.util.List;

public class ShopService {

    /** Danh sách shop public (mặc định phân trang theo createdAt DESC). */
    public List<Shop> listPublicShops(int page, int size, String q) {
        if (page < 1) page = 1;
        if (size < 1) size = 12;
        int first = (page - 1) * size;

        EntityManager em = JPAConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                "SELECT s FROM Shop s " +
                "WHERE 1=1 "
                // Nếu có trường trạng thái thì mở comment dưới:
                // + "AND s.status = vn.iotstar.entities.Shop$ShopStatus.ACTIVE "
            );
            if (q != null && !q.isBlank()) {
                jpql.append("AND s.shopName LIKE :kw ");
            }
            jpql.append("ORDER BY s.createdAt DESC");

            TypedQuery<Shop> query = em.createQuery(jpql.toString(), Shop.class);
            if (q != null && !q.isBlank()) {
                query.setParameter("kw", "%" + q.trim() + "%");
            }
            return query.setFirstResult(first).setMaxResults(size).getResultList();
        } finally {
            em.close();
        }
    }

    /** Tổng số shop public để tính totalPages. */
    public long countPublicShops(String q) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            StringBuilder jpql = new StringBuilder(
                "SELECT COUNT(s) FROM Shop s " +
                "WHERE 1=1 "
                // Nếu có trường trạng thái thì mở comment dưới:
                // + "AND s.status = vn.iotstar.entities.Shop$ShopStatus.ACTIVE "
            );
            if (q != null && !q.isBlank()) {
                jpql.append("AND s.shopName LIKE :kw ");
            }

            TypedQuery<Long> query = em.createQuery(jpql.toString(), Long.class);
            if (q != null && !q.isBlank()) {
                query.setParameter("kw", "%" + q.trim() + "%");
            }
            return query.getSingleResult();
        } finally {
            em.close();
        }
    }

    /** Tìm shop theo slug (ưu tiên) hoặc id (fallback). */
    public Shop findBySlugOrId(String slugNullable, Long idNullable) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            if (slugNullable != null && !slugNullable.isBlank()) {
                List<Shop> list = em.createQuery(
                        "SELECT s FROM Shop s WHERE s.slug = :slug", Shop.class)
                    .setParameter("slug", slugNullable.trim())
                    .setMaxResults(1)
                    .getResultList();
                return list.isEmpty() ? null : list.get(0);
            }
            if (idNullable != null) {
                return em.find(Shop.class, idNullable);
            }
            return null;
        } finally {
            em.close();
        }
    }
}
