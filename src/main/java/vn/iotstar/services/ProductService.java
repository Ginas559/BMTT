// filepath: src/main/java/vn/iotstar/services/ProductService.java
// (ĐÃ GỠ phương thức softDelete(...) vì không còn dùng; toggle là chức năng chung.)
package vn.iotstar.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import jakarta.servlet.http.HttpServletRequest;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Shop;
import vn.iotstar.entities.Category;
import vn.iotstar.utils.SessionUtil;

public class ProductService {

    private final StatisticService helper = new StatisticService();

    public static class PageResult<T> {
        public final List<T> items;
        public final int page;
        public final int size;
        public final int totalPages;
        public PageResult(List<T> items, int page, int size, int totalPages) {
            this.items = items; this.page = page; this.size = size; this.totalPages = totalPages;
        }
    }

    public List<Product> getByVendor(HttpServletRequest req) {
        Long uid = SessionUtil.currentUserId(req);
        if (uid == null) return List.of();
        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) return List.of();

        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT p FROM Product p " +
                "JOIN FETCH p.shop s " +
                "LEFT JOIN FETCH p.category c " +
                "WHERE s.shopId = :sid " +
                "ORDER BY p.productId DESC", Product.class)
            .setParameter("sid", shop.getShopId())
            .getResultList();
        } finally { em.close(); }
    }

    public PageResult<Product> findByShopPaged(Long shopId, int page, int size,
                                               String q, Long categoryId, Product.ProductStatus status) {
        if (shopId == null) return new PageResult<>(List.of(), 1, size, 1);
        page = Math.max(1, page);
        size = Math.max(1, size);
        int first = (page - 1) * size;

        StringBuilder where = new StringBuilder(" WHERE p.shop.shopId = :sid ");
        if (q != null && !q.isBlank()) where.append(" AND LOWER(p.productName) LIKE :kw ");
        if (categoryId != null)       where.append(" AND p.category.categoryId = :cid ");
        if (status != null)           where.append(" AND p.status = :st ");

        EntityManager em = JPAConfig.getEntityManager();
        try {
            TypedQuery<Long> cq = em.createQuery(
                "SELECT COUNT(p) FROM Product p" + where, Long.class);
            cq.setParameter("sid", shopId);
            if (q != null && !q.isBlank()) cq.setParameter("kw", "%" + q.toLowerCase() + "%");
            if (categoryId != null) cq.setParameter("cid", categoryId);
            if (status != null) cq.setParameter("st", status);
            long total = cq.getSingleResult();
            int totalPages = (int) Math.max(1, (total + size - 1) / size);

            TypedQuery<Product> pq = em.createQuery(
                "SELECT DISTINCT p FROM Product p " +
                "JOIN FETCH p.shop s " +
                "LEFT JOIN FETCH p.category c " +
                where.toString() +
                " ORDER BY p.productId DESC", Product.class);
            pq.setParameter("sid", shopId);
            if (q != null && !q.isBlank()) pq.setParameter("kw", "%" + q.toLowerCase() + "%");
            if (categoryId != null) pq.setParameter("cid", categoryId);
            if (status != null) pq.setParameter("st", status);
            List<Product> items = pq.setFirstResult(first).setMaxResults(size).getResultList();

            return new PageResult<>(items, page, size, totalPages);
        } finally { em.close(); }
    }

    public Long add(HttpServletRequest req) {
        Long uid = SessionUtil.currentUserId(req);
        if (uid == null) throw new RuntimeException("Chưa đăng nhập");
        Shop shop = helper.findShopByOwner(uid);
        if (shop == null) throw new RuntimeException("Shop không tồn tại cho vendor");

        String name = req.getParameter("name");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stock");
        String catStr = req.getParameter("categoryId");

        validateCore(name, priceStr, stockStr, catStr);

        BigDecimal price = new BigDecimal(priceStr);
        int stock = Integer.parseInt(stockStr);
        Long categoryId = Long.valueOf(catStr);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = Product.builder()
                .productName(name.trim())
                .price(price)
                .stock(stock)
                .status(Product.ProductStatus.ACTIVE)
                .shop(em.getReference(Shop.class, shop.getShopId()))
                .category(em.getReference(Category.class, categoryId))
                .build();
            em.persist(p);
            em.flush();
            Long newId = p.getProductId();
            tx.commit();
            return newId;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    public Product findById(Long id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Product p " +
                "JOIN FETCH p.shop " +
                "LEFT JOIN FETCH p.category " +
                "WHERE p.productId = :id", Product.class)
            .setParameter("id", id)
            .getSingleResult();
        } finally { em.close(); }
    }

    public Product findByIdWithShop(Long id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Product p " +
                "JOIN FETCH p.shop " +
                "WHERE p.productId = :id", Product.class)
            .setParameter("id", id)
            .getSingleResult();
        } finally { em.close(); }
    }

    public List<Product> findActiveWithShop(int page, int size) {
        int first = Math.max(0, page) * Math.max(1, size);
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Product p " +
                "JOIN FETCH p.shop " +
                "WHERE p.status = :st " +
                "ORDER BY p.productId DESC", Product.class)
            .setParameter("st", Product.ProductStatus.ACTIVE)
            .setFirstResult(first)
            .setMaxResults(size)
            .getResultList();
        } finally { em.close(); }
    }

    public void update(HttpServletRequest req) {
        Long pid = Long.valueOf(req.getParameter("productId"));
        String name = req.getParameter("name");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stock");
        String status = req.getParameter("status");

        validateCore(name, priceStr, stockStr, null);

        BigDecimal price = new BigDecimal(priceStr);
        int stock = Integer.parseInt(stockStr);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, pid);
            if (p == null) throw new RuntimeException("Không tìm thấy sản phẩm");
            p.setProductName(name.trim());
            p.setPrice(price);
            p.setStock(stock);
            if (status != null && !status.isBlank()) {
                p.setStatus(Product.ProductStatus.valueOf(status));
            }
            em.merge(p);
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    /** Gỡ softDelete() — dùng toggleStatus làm Ẩn/Hiện chung. */

    public List<Product> getByShopId(Long shopId) {
        if (shopId == null) return List.of();
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT DISTINCT p FROM Product p " +
                "JOIN FETCH p.shop s " +
                "LEFT JOIN FETCH p.category c " +
                "WHERE s.shopId = :sid " +
                "ORDER BY p.productId DESC", Product.class)
            .setParameter("sid", shopId)
            .getResultList();
        } finally { em.close(); }
    }

    public Long addForShop(HttpServletRequest req, Long shopId) {
        if (shopId == null) throw new RuntimeException("Thiếu shopId");

        String name    = req.getParameter("name");
        String priceSt = req.getParameter("price");
        String stockSt = req.getParameter("stock");
        String catSt   = req.getParameter("categoryId");

        validateCore(name, priceSt, stockSt, catSt);

        BigDecimal price = new BigDecimal(priceSt);
        int stock = Integer.parseInt(stockSt);
        Long categoryId = Long.valueOf(catSt);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = Product.builder()
                    .productName(name.trim())
                    .price(price)
                    .stock(stock)
                    .status(Product.ProductStatus.ACTIVE)
                    .shop(em.getReference(Shop.class, shopId))
                    .category(em.getReference(Category.class, categoryId))
                    .build();
            em.persist(p);
            em.flush();
            Long id = p.getProductId();
            tx.commit();
            return id;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    public void updateForShop(HttpServletRequest req, Long shopId) {
        if (shopId == null) throw new RuntimeException("Thiếu shopId");

        Long pid        = Long.valueOf(req.getParameter("productId"));
        String name     = req.getParameter("name");
        String priceStr = req.getParameter("price");
        String stockStr = req.getParameter("stock");
        String status   = req.getParameter("status");

        validateCore(name, priceStr, stockStr, null);
        BigDecimal price = new BigDecimal(priceStr);
        int stock        = Integer.parseInt(stockStr);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, pid);
            if (p == null) throw new RuntimeException("Không tìm thấy sản phẩm");
            if (p.getShop() == null || !shopId.equals(p.getShop().getShopId())) {
                throw new SecurityException("Không có quyền cập nhật sản phẩm của shop khác");
            }
            p.setProductName(name.trim());
            p.setPrice(price);
            p.setStock(stock);
            if (status != null && !status.isBlank()) {
                p.setStatus(Product.ProductStatus.valueOf(status));
            }
            em.merge(p);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    public Product.ProductStatus toggleStatus(Long productId, Long shopId) {
        if (productId == null || shopId == null) throw new RuntimeException("Thiếu tham số");

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Product p = em.find(Product.class, productId, LockModeType.PESSIMISTIC_WRITE);
            if (p == null) throw new RuntimeException("Không tìm thấy sản phẩm");
            if (p.getShop() == null || !shopId.equals(p.getShop().getShopId())) {
                throw new SecurityException("Không có quyền thao tác sản phẩm này");
            }
            Product.ProductStatus newSt =
                    (p.getStatus() == Product.ProductStatus.ACTIVE)
                            ? Product.ProductStatus.INACTIVE
                            : Product.ProductStatus.ACTIVE;
            p.setStatus(newSt);
            em.merge(p);
            tx.commit();
            return newSt;
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally { em.close(); }
    }

    private void validateCore(String name, String priceStr, String stockStr, String catStrOrNullable) {
        List<String> errs = new ArrayList<>();
        if (name == null || name.trim().length() < 3) errs.add("Tên tối thiểu 3 ký tự.");
        BigDecimal price = null;
        Integer stock = null;
        try { price = new BigDecimal(priceStr); } catch (Exception e) { errs.add("Giá không hợp lệ."); }
        try { stock = Integer.parseInt(stockStr); } catch (Exception e) { errs.add("Tồn không hợp lệ."); }
        if (catStrOrNullable != null) {
            try { Long.valueOf(catStrOrNullable); } catch (Exception e) { errs.add("Danh mục không hợp lệ."); }
        }
        if (price != null && price.compareTo(BigDecimal.ZERO) <= 0) errs.add("Giá phải > 0.");
        if (stock != null && stock < 0) errs.add("Tồn phải ≥ 0.");
        if (!errs.isEmpty()) throw new RuntimeException(String.join(" ", errs));
    }
}
