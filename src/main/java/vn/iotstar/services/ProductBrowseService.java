// filepath: src/main/java/vn/iotstar/services/ProductBrowseService.java

package vn.iotstar.services;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Category;
import vn.iotstar.services.ReviewService; // <-- GIỮ LẠI (Tích hợp Review)

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.List;

public class ProductBrowseService {

    // ==== DTOs (POJO có getter, tránh lỗi JSP EL) ====
    public static class ItemVM {
        private final Long id;
        private final String name;
        private final String categoryName;
        private final Long shopId;
        private final String shopName;
        private final String shopLogoUrl;
        private final BigDecimal price;
        private final BigDecimal discountPrice;
        private final Double ratingAvg;
        private final String coverUrl;

        // BỔ SUNG: brand, gender, style, stockTotal (GIỮ LẠI)
        private final String brand;
        private final String gender;
        private final String style;
        private final Integer stockTotal;

        public ItemVM(Long id, String name, String categoryName,
                      Long shopId, String shopName, String shopLogoUrl,
                      BigDecimal price, BigDecimal discountPrice,
                      Double ratingAvg, String coverUrl,
                      String brand, String gender, String style, Integer stockTotal) {
            this.id = id; this.name = name; this.categoryName = categoryName;
            this.shopId = shopId; this.shopName = shopName; this.shopLogoUrl = shopLogoUrl;
            this.price = price; this.discountPrice = discountPrice;
            this.ratingAvg = ratingAvg; this.coverUrl = coverUrl;
            this.brand = brand; this.gender = gender; this.style = style; this.stockTotal = stockTotal;
        }
        public Long getId() { return id; }
        public String getName() { return name; }
        public String getCategoryName() { return categoryName; }
        public Long getShopId() { return shopId; }
        public String getShopName() { return shopName; }
        public String getShopLogoUrl() { return shopLogoUrl; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getDiscountPrice() { return discountPrice; }
        public Double getRatingAvg() { return ratingAvg; }
        public String getCoverUrl() { return coverUrl; }
        // NEW getters (GIỮ LẠI)
        public String getBrand() { return brand; }
        public String getGender() { return gender; }
        public String getStyle() { return style; }
        public Integer getStockTotal() { return stockTotal; }
    }

    public static class ShopOptionVM {
        private final Long id;
        private final String name;
        public ShopOptionVM(Long id, String name) { this.id = id; this.name = name; }
        public Long getId() { return id; }
        public String getName() { return name; }
    }

    public static class PageResult<T> {
        private final List<T> items;
        private final int number;
        private final int size;
        private final long totalItems;
        private final int totalPages;
        public PageResult(List<T> items, int number, int size, long totalItems, int totalPages) {
            this.items = items; this.number = number; this.size = size;
            this.totalItems = totalItems; this.totalPages = totalPages;
        }
        public List<T> getItems() { return items; }
        public int getNumber() { return number; }
        public int getSize() { return size; }
        public long getTotalItems() { return totalItems; }
        public int getTotalPages() { return totalPages; }
        public boolean isHasPrev() { return number > 1; }
        public boolean isHasNext() { return number < totalPages; }
    }

    // Giữ API cũ (không shopId, không attr filter) -> GỌI OVERLOAD MỚI (GIỮ LẠI)
    public PageResult<ItemVM> page(
            String q, Long catId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer minRating,
            String sort, int page, int size
    ){
        // Thêm null cho shopId, brand, gender, style, và province (NEW)
        return page(q, catId, minPrice, maxPrice, minRating, null, null, null, null, null, sort, page, size);
    }

    // Giữ API cũ (có shopId, chưa attr filter) -> GỌI OVERLOAD MỚI (GIỮ LẠI)
    public PageResult<ItemVM> page(
            String q, Long catId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer minRating,
            Long shopId,
            String sort, int page, int size
    ){
        // Thêm null cho brand, gender, style, và province (NEW)
        return page(q, catId, minPrice, maxPrice, minRating, shopId, null, null, null, null, sort, page, size);
    }

    // ==== API MỚI NHẤT: có filter brand/gender/style/province (GIỮ LẠI) ====
    public PageResult<ItemVM> page(
            String q, Long catId,
            BigDecimal minPrice, BigDecimal maxPrice,
            Integer minRating,
            Long shopId,
            String brand, String gender, String style,
            String province, // NEW: Tham số tỉnh/thành
            String sort, int page, int size
    ){
        EntityManager em = JPAConfig.getEntityManager();
        try{
            StringBuilder where = new StringBuilder(" WHERE 1=1 ");
            Map<String,Object> p = new HashMap<>();

            if (q != null && !q.isBlank()) {
                where.append(" AND (LOWER(p.productName) LIKE :kw OR LOWER(p.description) LIKE :kw) ");
                p.put("kw", "%"+q.toLowerCase()+"%");
            }
            if (catId != null) { where.append(" AND c.categoryId = :catId "); p.put("catId", catId); }
            if (minPrice != null) { where.append(" AND p.price >= :minP "); p.put("minP", minPrice); }
            if (maxPrice != null) { where.append(" AND p.price <= :maxP "); p.put("maxP", maxPrice); }
            if (minRating != null) {
                where.append(" AND (p.ratingAvg IS NOT NULL AND p.ratingAvg >= :minR) ");
                p.put("minR", minRating.doubleValue());
            }
            if (shopId != null) { where.append(" AND s.shopId = :shopId "); p.put("shopId", shopId); }

            // Lọc theo thuộc tính sản phẩm (ShoeAttribute) (GIỮ LẠI)
            if (brand != null && !brand.isBlank()) {
                where.append(" AND EXISTS (SELECT 1 FROM ShoeAttribute sa WHERE sa.product = p AND LOWER(sa.brand) = :brand) ");
                p.put("brand", brand.toLowerCase());
            }
            if (gender != null && !gender.isBlank()) {
                where.append(" AND EXISTS (SELECT 1 FROM ShoeAttribute sa2 WHERE sa2.product = p AND LOWER(sa2.gender) = :gender) ");
                p.put("gender", gender.toLowerCase());
            }
            if (style != null && !style.isBlank()) {
                where.append(" AND EXISTS (SELECT 1 FROM ShoeAttribute sa3 WHERE sa3.product = p AND LOWER(sa3.style) = :style) ");
                p.put("style", style.toLowerCase());
            }
            
            // NEW: lọc theo tỉnh/thành của Shop (GIỮ LẠI)
            if (province != null && !province.isBlank()) {
                // Chú ý: s.province là trường của Shop
                where.append(" AND (s.province IS NOT NULL AND LOWER(s.province) = :prov) ");
                p.put("prov", province.toLowerCase());
            }

            String order = switch (sort == null ? "" : sort) {
                case "price_asc"   -> " ORDER BY p.price ASC ";
                case "price_desc"  -> " ORDER BY p.price DESC ";
                case "rating_desc" -> " ORDER BY p.ratingAvg DESC NULLS LAST ";
                case "new_desc"    -> " ORDER BY p.createdAt DESC ";
                default            -> " ORDER BY p.createdAt DESC ";
            };

            // Count
            TypedQuery<Long> cq = em.createQuery(
                "SELECT COUNT(p) FROM Product p JOIN p.category c JOIN p.shop s" + where, Long.class);
            p.forEach(cq::setParameter);
            long total = cq.getSingleResult();
            int totalPages = Math.max(1, (int)Math.ceil(total/(double)size));

            // Page products (fetch shop để lấy logo)
            TypedQuery<Product> qlist = em.createQuery(
                "SELECT p FROM Product p " +
                "JOIN p.category c " +
                "JOIN FETCH p.shop s " +
                where + order, Product.class);
            p.forEach(qlist::setParameter);
            qlist.setFirstResult((page-1)*size).setMaxResults(size);
            List<Product> products = qlist.getResultList();

            if (products.isEmpty()) {
                return new PageResult<>(List.of(), page, size, 0, totalPages);
            }

            // ===== Batch lấy thumbnail (optional: giữ nguyên logic cũ) =====
            TypedQuery<String> coverQ = em.createQuery(
                "SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = :prod " +
                "ORDER BY CASE WHEN pi.isThumbnail = true THEN 0 ELSE 1 END, pi.id", String.class);

            // ===== Batch ShoeAttribute theo productIds (GIỮ LẠI) =====
            List<Long> ids = new ArrayList<>(products.size());
            for (Product pr : products) ids.add(pr.getProductId());

            Map<Long, AttrTriple> attrMap = new HashMap<>();
            try {
                TypedQuery<Object[]> aQ = em.createQuery(
                    "SELECT sa.product.productId, LOWER(sa.brand), LOWER(sa.gender), LOWER(sa.style) " +
                    "FROM ShoeAttribute sa WHERE sa.product.productId IN :ids",
                    Object[].class
                );
                aQ.setParameter("ids", ids);
                for (Object[] r : aQ.getResultList()) {
                    Long pid = (Long) r[0];
                    String b = (String) r[1];
                    String g = (String) r[2];
                    String st = (String) r[3];
                    attrMap.put(pid, new AttrTriple(b, g, st));
                }
            } catch (Exception ignore) {
                // nếu thiếu entity ShoeAttribute, bỏ qua
            }

            // ===== Batch SUM stock theo productIds từ ProductVariant (GIỮ LẠI) =====
            Map<Long, Integer> stockMap = new HashMap<>();
            try {
                TypedQuery<Object[]> sQ = em.createQuery(
                    "SELECT v.product.productId, COALESCE(SUM(v.stock),0) " +
                    "FROM ProductVariant v WHERE v.product.productId IN :ids " +
                    "GROUP BY v.product.productId",
                    Object[].class
                );
                sQ.setParameter("ids", ids);
                for (Object[] r : sQ.getResultList()) {
                    Long pid = (Long) r[0];
                    Long sum = (Long) r[1];
                    stockMap.put(pid, (sum != null ? sum.intValue() : 0));
                }
            } catch (Exception ignore) {
                // nếu thiếu entity ProductVariant, fallback sẽ dùng Product.stock
            }

            // Build items
            List<ItemVM> items = new ArrayList<>(products.size());
            for (Product pr : products) {
                String cover = coverQ.setParameter("prod", pr)
                                     .setMaxResults(1)
                                     .getResultStream().findFirst().orElse(null);

                AttrTriple at = attrMap.get(pr.getProductId());
                Integer stockTotal = stockMap.get(pr.getProductId());
                if (stockTotal == null) {
                    // fallback Product.stock
                    stockTotal = pr.getStock() != null ? pr.getStock() : 0;
                }

                items.add(new ItemVM(
                    pr.getProductId(),
                    pr.getProductName(),
                    pr.getCategory() != null ? pr.getCategory().getCategoryName() : null,
                    (pr.getShop() != null ? pr.getShop().getShopId() : null),
                    (pr.getShop() != null ? pr.getShop().getShopName() : null),
                    (pr.getShop() != null ? pr.getShop().getLogoUrl() : null),
                    pr.getPrice(),
                    pr.getDiscountPrice(),
                    pr.getRatingAvg() != null ? pr.getRatingAvg().doubleValue() : null,
                    cover,
                    at != null ? at.brand : null,
                    at != null ? at.gender : null,
                    at != null ? at.style : null,
                    stockTotal
                ));
            }
            return new PageResult<>(items, page, size, total, totalPages);
        } finally { em.close(); }
    }

    private static class AttrTriple {
        final String brand, gender, style;
        AttrTriple(String b, String g, String s){ this.brand=b; this.gender=g; this.style=s; }
    }

    public List<Category> categories() {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Category c ORDER BY c.categoryName", Category.class)
                     .getResultList();
        } finally { em.close(); }
    }

    public List<ShopOptionVM> shops(String keyword) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            String where = "";
            boolean hasKw = (keyword != null && !keyword.isBlank());
            if (hasKw) where = " WHERE LOWER(s.shopName) LIKE :kw ";

            Query q = em.createQuery(
                "SELECT s.shopId, s.shopName FROM Shop s" + where + " ORDER BY s.shopName ASC"
            );
            if (hasKw) q.setParameter("kw", "%"+keyword.toLowerCase()+"%");
            q.setMaxResults(50);

            @SuppressWarnings("unchecked")
            List<Object[]> rows = q.getResultList();
            List<ShopOptionVM> out = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                out.add(new ShopOptionVM((Long) r[0], (String) r[1]));
            }
            return out;
        } finally { em.close(); }
    }

    public Product findById(Long id) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            TypedQuery<Product> q = em.createQuery(
                "SELECT p FROM Product p " +
                "LEFT JOIN FETCH p.category " +
                "LEFT JOIN FETCH p.shop " +
                "WHERE p.productId = :id",
                Product.class
            );
            q.setParameter("id", id);
            List<Product> list = q.getResultList();
            return list.isEmpty() ? null : list.get(0);
        } finally {
            em.close();
        }
    }

    public List<String> imagesOf(Product p) {
        EntityManager em = JPAConfig.getEntityManager();
        try {
            return em.createQuery(
                "SELECT pi.imageUrl FROM ProductImage pi WHERE pi.product = :p " +
                "ORDER BY CASE WHEN pi.isThumbnail = true THEN 0 ELSE 1 END, pi.id", String.class)
                .setParameter("p", p)
                .getResultList();
        } finally { em.close(); }
    }

    /* ================== REVIEW INTEGRATION (GIỮ LẠI) ================== */

    /** Thống kê review cho trang chi tiết: avg & count */
    public ReviewService.Stats reviewStats(Long productId) {
        return new ReviewService().stats(productId);
    }

    /** Danh sách review mới nhất (giới hạn số lượng để hiển thị) */
    public List<ReviewService.ReviewItem> reviews(Long productId, Integer limit) {
        return new ReviewService().list(productId, limit);
    }

    /** Review của chính user (để hiển thị form sửa) */
    public ReviewService.ReviewItem userReview(Long productId, Long userId) {
        return new ReviewService().findByUser(productId, userId);
    }

    /** User có được phép review (đã mua hàng)? */
    public boolean canReview(Long productId, Long userId) {
        return new ReviewService().canReview(productId, userId);
    }
}