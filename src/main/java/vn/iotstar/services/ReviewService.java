package vn.iotstar.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ReviewService
 * - Product_Review không có product_id -> JOIN Order_Item
 * - Bảng users: [dbo].[users], PK = id
 */
public class ReviewService {

    /* ====== NEW: exception ném khi quá hạn 24h ====== */
    public static class TooLateException extends RuntimeException {
        public TooLateException() { super("too_late"); }
    }

    private static final int MAX_IMAGES = 6;               // Giới hạn hợp lý như các shop
    private static final int URL_MAX_LEN = 500;            // Khớp schema image_url varchar(500)

    private static volatile EntityManagerFactory EMF;

    private static EntityManagerFactory emf() {
        if (EMF == null) {
            synchronized (ReviewService.class) {
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

    /* ============================ DTOs ============================ */

    public static class Stats {
        private final double avg;
        private final long count;
        public Stats(double avg, long count) { this.avg = avg; this.count = count; }
        public double getAvg()   { return avg; }
        public long   getCount() { return count; }
    }

    public static class ReviewItem {
        private final String userName;
        private final LocalDateTime createdAt;
        private final int rating;
        private final String commentText;
        private final String imageUrl;
        private final String videoUrl;

        public ReviewItem(String userName, LocalDateTime createdAt, int rating,
                          String commentText, String imageUrl, String videoUrl) {
            this.userName   = userName == null ? "" : userName;
            this.createdAt  = createdAt;
            this.rating     = rating;
            this.commentText= commentText == null ? "" : commentText;
            this.imageUrl   = imageUrl;
            this.videoUrl   = videoUrl;
        }
        public String getUserName()     { return userName; }
        public LocalDateTime getCreatedAt(){ return createdAt; }
        public int getRating()          { return rating; }
        public String getCommentText()  { return commentText; }
        public String getImageUrl()     { return imageUrl; }
        public String getVideoUrl()     { return videoUrl; }
    }

    /* ============================ READ ============================ */

    public Stats stats(Long productId) {
        EntityManager em = em();
        try {
            String sql =
                "SELECT " +
                "  COALESCE(AVG(CAST(r.rating AS FLOAT)), 0.0) AS avg_rating, " +
                "  COUNT(*) AS cnt " +
                "FROM Product_Review r " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE oi.product_id = :pid";
            Object[] row = (Object[]) em.createNativeQuery(sql)
                    .setParameter("pid", productId)
                    .getSingleResult();
            double avg = row[0] == null ? 0.0 : ((Number) row[0]).doubleValue();
            long cnt   = row[1] == null ? 0L  : ((Number) row[1]).longValue();
            return new Stats(roundHalfUp(avg, 1), cnt);
        } finally {
            em.close();
        }
    }

    @SuppressWarnings("unchecked")
    public List<ReviewItem> list(Long productId, Integer limit) {
        int lim = (limit == null || limit <= 0) ? 10 : limit;
        EntityManager em = em();
        try {
            String sql =
                "SELECT " +
                "  COALESCE(NULLIF(LTRIM(RTRIM(CONCAT(u.firstname,' ',u.lastname))), ''), u.email, CONCAT('User#', CAST(u.id AS VARCHAR(20)))) AS user_name, " +
                "  r.created_at, r.rating, r.comment_text, r.image_url, r.video_url " +
                "FROM Product_Review r " +
                "JOIN [dbo].[users] u ON u.id = r.user_id " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE oi.product_id = :pid " +
                "ORDER BY r.created_at DESC";

            Query q = em.createNativeQuery(sql);
            q.setParameter("pid", productId);
            q.setMaxResults(lim);

            List<Object[]> rows = q.getResultList();
            List<ReviewItem> out = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                String userName   = toStr(r[0]);
                LocalDateTime ts  = toLdt(r[1]);
                int rating        = toInt(r[2]);
                String cmt        = toStr(r[3]);
                String img        = toStr(r[4]);
                String vid        = toStr(r[5]);
                out.add(new ReviewItem(userName, ts, rating, cmt, img, vid));
            }
            return out;
        } finally {
            em.close();
        }
    }

    public ReviewItem findByUser(Long productId, Long userId) {
        if (userId == null) return null;
        EntityManager em = em();
        try {
            String sql =
                "SELECT " +
                "  COALESCE(NULLIF(LTRIM(RTRIM(CONCAT(u.firstname,' ',u.lastname))), ''), u.email, CONCAT('User#', CAST(u.id AS VARCHAR(20)))) AS user_name, " +
                "  r.created_at, r.rating, r.comment_text, r.image_url, r.video_url " +
                "FROM Product_Review r " +
                "JOIN [dbo].[users] u ON u.id = r.user_id " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE oi.product_id = :pid AND r.user_id = :uid " +
                "ORDER BY r.created_at DESC";
            List<?> rows = em.createNativeQuery(sql)
                    .setParameter("pid", productId)
                    .setParameter("uid", userId)
                    .getResultList();
            if (rows.isEmpty()) return null;
            Object[] r = (Object[]) rows.get(0);
            return new ReviewItem(
                    toStr(r[0]),
                    toLdt(r[1]),
                    toInt(r[2]),
                    toStr(r[3]),
                    toStr(r[4]),
                    toStr(r[5])
            );
        } finally {
            em.close();
        }
    }

    /**
     * Chỉ cho phép đánh giá nếu user có đơn hàng DELIVERED cho sản phẩm này.
     */
    public boolean canReview(Long productId, Long userId) {
        if (userId == null) return false;
        EntityManager em = em();
        try {
            String sqlDelivered =
                "SELECT COUNT(*) " +
                "FROM Order_Item oi " +
                "JOIN [Order] o ON o.order_id = oi.order_id " +
                "WHERE o.user_id = :uid AND oi.product_id = :pid " +
                "  AND o.status = 'DELIVERED'";
            Number n = (Number) em.createNativeQuery(sqlDelivered)
                    .setParameter("uid", userId)
                    .setParameter("pid", productId)
                    .getSingleResult();
            long delivered = (n == null) ? 0L : n.longValue();
            return delivered > 0;
        } catch (Exception e) {
            // fail-close
            return false;
        } finally {
            em.close();
        }
    }

    /* ============================ WRITE (giữ nguyên method cũ) ============================ */

    public void saveOrUpdate(Long userId, Long productId, int rating,
                             String comment, String imageUrl, String videoUrl) {
        rating = clamp(rating, 1, 5);
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // A) Kiểm tra đã có đơn DELIVERED cho sản phẩm chưa
            Long oiDeliveredId = findLatestDeliveredOrderItemId(em, userId, productId);
            if (oiDeliveredId == null) {
                throw new IllegalStateException("not_delivered");
            }

            // B) Tìm tất cả review của (user, product) để xử lý 24h
            String selAll =
                "SELECT r.review_id, r.created_at " +
                "FROM Product_Review r " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE r.user_id = :uid AND oi.product_id = :pid " +
                "ORDER BY r.created_at DESC";
            List<Object[]> rows = em.createNativeQuery(selAll)
                    .setParameter("uid", userId)
                    .setParameter("pid", productId)
                    .getResultList();

            if (rows != null && !rows.isEmpty()) {
                // ĐÃ CÓ review trước đó → chỉ cho sửa nếu còn trong 24h
                Object newestId = rows.get(0)[0];
                LocalDateTime newestCreated = toLdt(rows.get(0)[1]);
                if (newestCreated != null) {
                    long hours = Duration.between(newestCreated, LocalDateTime.now()).toHours();
                    if (hours >= 24) {
                        throw new TooLateException();
                    }
                }

                String upd =
                    "UPDATE Product_Review " +
                    "SET rating = :rating, comment_text = :comment, image_url = :img, video_url = :vid " +
                    "WHERE review_id = :rid";
                em.createNativeQuery(upd)
                        .setParameter("rating", rating)
                        .setParameter("comment", safe(comment))
                        .setParameter("img", safe(imageUrl))
                        .setParameter("vid", safe(videoUrl))
                        .setParameter("rid", newestId)
                        .executeUpdate();

                // Xoá dư (nếu trước đây từng lỡ insert nhiều)
                if (rows.size() > 1) {
                    StringBuilder sb = new StringBuilder("DELETE FROM Product_Review WHERE review_id IN (");
                    for (int i = 1; i < rows.size(); i++) {
                        if (i > 1) sb.append(',');
                        sb.append('?');
                    }
                    sb.append(')');
                    var q = em.createNativeQuery(sb.toString());
                    int p = 1;
                    for (int i = 1; i < rows.size(); i++) q.setParameter(p++, rows.get(i)[0]);
                    q.executeUpdate();
                }

            } else {
                // CHƯA CÓ review → chèn mới dựa vào order_item DELIVERED mới nhất
                LocalDateTime now = LocalDateTime.now();
                String ins =
                    "INSERT INTO Product_Review (order_item_id, user_id, rating, comment_text, image_url, video_url, created_at) " +
                    "VALUES (:oiid, :uid, :rating, :comment, :img, :vid, :ts)";
                em.createNativeQuery(ins)
                        .setParameter("oiid", oiDeliveredId)
                        .setParameter("uid", userId)
                        .setParameter("rating", rating)
                        .setParameter("comment", safe(comment))
                        .setParameter("img", safe(imageUrl))
                        .setParameter("vid", safe(videoUrl))
                        .setParameter("ts", Timestamp.valueOf(now))
                        .executeUpdate();
            }

            // C) Cập nhật average product
            updateProductAverage(em, productId);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /* ============================ WRITE (NEW overload: nhiều ảnh + video) ============================ */

    /**
     * Overload cho phép lưu nhiều ảnh (Product_Review_Image) + video.
     * - Ảnh đầu tiên sẽ set vào Product_Review.image_url để tương thích luồng cũ.
     * - Luôn xóa & chèn lại bảng Product_Review_Image cho review mới nhất (nếu đang update).
     */
    public void saveOrUpdate(Long userId, Long productId, int rating,
                             String comment, List<String> imageUrls, String videoUrl) {

        rating = clamp(rating, 1, 5);

        // Chuẩn hóa danh sách ảnh theo giới hạn/định dạng cơ bản
        List<String> imgs = new ArrayList<>();
        if (imageUrls != null) {
            for (String u : imageUrls) {
                String x = safe(u);
                if (x != null && !x.isBlank()) {
                    // cắt về 500 ký tự để khớp schema
                    if (x.length() > URL_MAX_LEN) x = x.substring(0, URL_MAX_LEN);
                    imgs.add(x);
                }
                if (imgs.size() >= MAX_IMAGES) break;
            }
        }
        String firstImg = imgs.isEmpty() ? null : imgs.get(0);
        String video = safe(videoUrl);
        if (video != null && video.length() > URL_MAX_LEN) video = video.substring(0, URL_MAX_LEN);

        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // A) Kiểm tra đã có đơn DELIVERED cho sản phẩm chưa
            Long oiDeliveredId = findLatestDeliveredOrderItemId(em, userId, productId);
            if (oiDeliveredId == null) {
                throw new IllegalStateException("not_delivered");
            }

            // B) Tìm tất cả review của (user, product)
            String selAll =
                "SELECT r.review_id, r.created_at " +
                "FROM Product_Review r " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE r.user_id = :uid AND oi.product_id = :pid " +
                "ORDER BY r.created_at DESC";
            List<Object[]> rows = em.createNativeQuery(selAll)
                    .setParameter("uid", userId)
                    .setParameter("pid", productId)
                    .getResultList();

            Long targetReviewId;

            if (rows != null && !rows.isEmpty()) {
                // ĐÃ CÓ review → chỉ cho sửa trong 24h
                Object newestId = rows.get(0)[0];
                LocalDateTime newestCreated = toLdt(rows.get(0)[1]);
                if (newestCreated != null) {
                    long hours = Duration.between(newestCreated, LocalDateTime.now()).toHours();
                    if (hours >= 24) {
                        throw new TooLateException();
                    }
                }

                String upd =
                    "UPDATE Product_Review " +
                    "SET rating = :rating, comment_text = :comment, image_url = :img, video_url = :vid " +
                    "WHERE review_id = :rid";
                em.createNativeQuery(upd)
                        .setParameter("rating", rating)
                        .setParameter("comment", safe(comment))
                        .setParameter("img", firstImg)
                        .setParameter("vid", video)
                        .setParameter("rid", newestId)
                        .executeUpdate();

                targetReviewId = castLong(newestId);

                // Xoá dư (nếu trước đây từng lỡ insert nhiều review)
                if (rows.size() > 1) {
                    StringBuilder sb = new StringBuilder("DELETE FROM Product_Review WHERE review_id IN (");
                    for (int i = 1; i < rows.size(); i++) {
                        if (i > 1) sb.append(',');
                        sb.append('?');
                    }
                    sb.append(')');
                    var q = em.createNativeQuery(sb.toString());
                    int p = 1;
                    for (int i = 1; i < rows.size(); i++) q.setParameter(p++, rows.get(i)[0]);
                    q.executeUpdate();
                }

            } else {
                // CHƯA CÓ review → chèn mới, lấy review_id vừa chèn
                LocalDateTime now = LocalDateTime.now();
                String ins =
                    "INSERT INTO Product_Review " +
                    " (order_item_id, user_id, rating, comment_text, image_url, video_url, created_at) " +
                    "OUTPUT Inserted.review_id " +
                    "VALUES (:oiid, :uid, :rating, :comment, :img, :vid, :ts)";
                Object ridObj = em.createNativeQuery(ins)
                        .setParameter("oiid", oiDeliveredId)
                        .setParameter("uid", userId)
                        .setParameter("rating", rating)
                        .setParameter("comment", safe(comment))
                        .setParameter("img", firstImg)
                        .setParameter("vid", video)
                        .setParameter("ts", Timestamp.valueOf(now))
                        .getSingleResult();
                targetReviewId = castLong(ridObj);
            }

            // C) Đồng bộ bảng Product_Review_Image (xoá & chèn lại)
            if (targetReviewId != null) {
                String delImgs = "DELETE FROM Product_Review_Image WHERE review_id = :rid";
                em.createNativeQuery(delImgs)
                        .setParameter("rid", targetReviewId)
                        .executeUpdate();

                if (!imgs.isEmpty()) {
                    String insImg = "INSERT INTO Product_Review_Image (image_url, sort_order, review_id) " +
                                    "VALUES (:url, :ord, :rid)";
                    int order = 1;
                    for (String url : imgs) {
                        em.createNativeQuery(insImg)
                                .setParameter("url", url)
                                .setParameter("ord", order++)
                                .setParameter("rid", targetReviewId)
                                .executeUpdate();
                    }
                }
            }

            // D) Cập nhật average product
            updateProductAverage(em, productId);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteByUser(Long userId, Long productId) {
        EntityManager em = em();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            String sel =
                "SELECT TOP 1 r.review_id, r.created_at " +
                "FROM Product_Review r " +
                "JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
                "WHERE r.user_id = :uid AND oi.product_id = :pid " +
                "ORDER BY r.created_at DESC";
            List<Object[]> rows = em.createNativeQuery(sel)
                    .setParameter("uid", userId)
                    .setParameter("pid", productId)
                    .getResultList();

            if (rows != null && !rows.isEmpty()) {
                Object rid = rows.get(0)[0];
                LocalDateTime created = toLdt(rows.get(0)[1]);
                if (created != null) {
                    long hrs = Duration.between(created, LocalDateTime.now()).toHours();
                    if (hrs >= 24) throw new TooLateException();
                }
                // NEW: xóa ảnh con trước
                String delChild = "DELETE FROM Product_Review_Image WHERE review_id = :rid";
                em.createNativeQuery(delChild).setParameter("rid", rid).executeUpdate();

                String del = "DELETE FROM Product_Review WHERE review_id = :rid";
                em.createNativeQuery(del).setParameter("rid", rid).executeUpdate();
            }

            updateProductAverage(em, productId);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /* ============================ Helpers ============================ */

    private Long findLatestDeliveredOrderItemId(EntityManager em, Long userId, Long productId) {
        String findOi =
            "SELECT TOP 1 oi.order_item_id " +
            "FROM Order_Item oi " +
            "JOIN [Order] o ON o.order_id = oi.order_id " +
            "WHERE o.user_id = :uid AND oi.product_id = :pid " +
            "  AND o.status = 'DELIVERED' " +
            "ORDER BY o.created_at DESC";
        List<?> oiIds = em.createNativeQuery(findOi)
                .setParameter("uid", userId)
                .setParameter("pid", productId)
                .getResultList();
        return (oiIds == null || oiIds.isEmpty()) ? null : castLong(oiIds.get(0));
    }

    private static Long castLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.valueOf(String.valueOf(o)); } catch (Exception e){ return null; }
    }

    private void updateProductAverage(EntityManager em, Long productId) {
        String upd =
            "UPDATE [Product] " +
            "SET rating_avg = (" +
            "  SELECT CAST(COALESCE(AVG(CAST(r.rating AS FLOAT)), 0.0) AS DECIMAL(3,2)) " +
            "  FROM Product_Review r " +
            "  JOIN Order_Item oi ON oi.order_item_id = r.order_item_id " +
            "  WHERE oi.product_id = :pid" +
            ") " +
            "WHERE product_id = :pid";
        em.createNativeQuery(upd)
                .setParameter("pid", productId)
                .executeUpdate();
    }

    private static String safe(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
    private static String toStr(Object o) {
        return (o == null) ? "" : o.toString();
    }
    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e){ return 0; }
    }
    private static LocalDateTime toLdt(Object ts) {
        if (ts == null) return null;
        if (ts instanceof Timestamp) return ((Timestamp) ts).toLocalDateTime();
        try {
            return Timestamp.valueOf(String.valueOf(ts)).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    private static double roundHalfUp(double value, int scale) {
        if (Double.isNaN(value)) return 0.0;
        try {
            return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
        } catch (Exception ignore) {
            double pow = Math.pow(10, scale);
            return Math.round(value * pow) / pow;
        }
    }

    private static int clamp(int v, int lo, int hi) {
        if (v < lo) return lo;
        if (v > hi) return hi;
        return v;
    }
}
