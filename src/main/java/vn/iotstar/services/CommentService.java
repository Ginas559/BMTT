// filepath: src/main/java/vn/iotstar/services/CommentService.java
package vn.iotstar.services;

import jakarta.persistence.EntityManager;
// Đã XÓA: import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
// Đã XÓA: import jakarta.persistence.Persistence;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

// ... (phần Javadoc giữ nguyên)
public class CommentService {

    // Đã XÓA: private static volatile EntityManagerFactory EMF;
    // Đã XÓA: private static EntityManagerFactory emf() { ... } (logic double-checked locking)

    /**
     * Helper đã SỬA: Lấy EntityManager từ JPAConfig
     */
    private EntityManager em() {
        // Sửa lại để trỏ về JPAConfig
        return vn.iotstar.configs.JPAConfig.getEntityManagerFactory().createEntityManager();
    }

    /* ============================ DTO (ĐÃ SỬA LỖI CHẤM PHẨY) ============================ */

    /** Item hiển thị bình luận cho JSP (phiên bản cũ 1 cấp) */
    public static class CommentItem {
        private final String userName;    // <-- ĐÃ THÊM ;
        private final Date createdAt;     // <-- ĐÃ THÊM ;
        private final String content;     // <-- ĐÃ THÊM ;

        public CommentItem(String userName, Date createdAt, String content) {
            this.userName = userName == null ? "" : userName;   // <-- ĐÃ THÊM ;
            this.createdAt = createdAt;                         // <-- ĐÃ THÊM ;
            this.content = content == null ? "" : content;       // <-- ĐÃ THÊM ;
        }
        public String getUserName()   { return userName; }
        public Date getCreatedAt()    { return createdAt; }
        public String getContent()    { return content; }
    }

    /** Item cho threaded UI (cây comment) */
    public static class ThreadItem {
        private final Long commentId;     // <-- ĐÃ THÊM ;
        private final Long parentId;      // <-- ĐÃ THÊM ;
        private final Long userId;        // <-- ĐÃ THÊM ;
        private final String userName;    // <-- ĐÃ THÊM ;
        private final String content;     // <-- ĐÃ THÊM ;
        private final Date createdAt;     // <-- ĐÃ THÊM ;
        private final int depth;          // <-- ĐÃ THÊM ;
        private final boolean canDelete;  // <-- ĐÃ THÊM ;

        public ThreadItem(Long commentId, Long parentId, Long userId, String userName,
                          String content, Date createdAt, int depth, boolean canDelete) {
            this.commentId = commentId;                       // <-- ĐÃ THÊM ;
            this.parentId = parentId;                         // <-- ĐÃ THÊM ;
            this.userId = userId;                             // <-- ĐÃ THÊM ;
            this.userName = userName == null ? "" : userName;   // <-- ĐÃ THÊM ;
            this.content = content == null ? "" : content;       // <-- ĐÃ THÊM ;
            this.createdAt = createdAt;                       // <-- ĐÃ THÊM ;
            this.depth = depth;                               // <-- ĐÃ THÊM ;
            this.canDelete = canDelete;                       // <-- ĐÃ THÊM ;
        }

        public Long getCommentId() { return commentId; }
        public Long getParentId()  { return parentId; }
        public Long getUserId()    { return userId; }
        public String getUserName(){ return userName; }
        public String getContent() { return content; }
        public Date getCreatedAt() { return createdAt; }
        public int getDepth()      { return depth; }
        public boolean isCanDelete(){ return canDelete; }
    }


    /* ============================ READ ============================ */

    /** Danh sách bình luận mới nhất của 1 sản phẩm (giữ nguyên API cũ) */
    @SuppressWarnings("unchecked")
    public List<CommentItem> list(Long productId, Integer limit) {
        int lim = (limit == null || limit <= 0) ? 10 : limit;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            String sql =
                    "SELECT " +
                            "  COALESCE(NULLIF(CONCAT(COALESCE(u.firstname,''),' ',COALESCE(u.lastname,'')),' '), u.email, CONCAT('User#', CAST(u.id AS VARCHAR(20)))) AS user_name, " +
                            "  c.created_at, c.content " +
                            "FROM Product_Comment c " +
                            "JOIN users u ON u.id = c.user_id " +
                            "WHERE c.product_id = :pid " +
                            "ORDER BY c.created_at DESC";
            Query q = em.createNativeQuery(sql);
            q.setParameter("pid", productId);
            q.setMaxResults(lim);

            List<Object[]> rows = q.getResultList();
            List<CommentItem> out = new ArrayList<>(rows.size());
            for (Object[] r : rows) {
                String userName = toStr(r[0]);
                Date ts = toDate(r[1]); // <-- map sang java.util.Date
                String content = toStr(r[2]);
                out.add(new CommentItem(userName, ts, content));
            }
            return out;
            // Không cần finally em.close()
        }
    }

    /** Trả về cây comment (thread) phẳng theo thứ tự hiển thị + depth + quyền xoá */
    @SuppressWarnings("unchecked")
    public List<ThreadItem> listThread(Long productId, Long currentUserId) {
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            // Lấy tất cả comment của product, sort theo created_at ASC để render cha trước con
            String sql =
                    "SELECT " +
                            "  c.comment_id, c.parent_comment_id, c.user_id, " +
                            "  COALESCE(NULLIF(CONCAT(COALESCE(u.firstname,''),' ',COALESCE(u.lastname,'')),' '), u.email, CONCAT('User#', CAST(u.id AS VARCHAR(20)))) AS user_name, " +
                            "  c.content, c.created_at, " +
                            "  CASE WHEN c.user_id = :curUid " +
                            "       AND DATEDIFF(hour, c.created_at, GETDATE()) <= 24 " +
                            "       AND NOT EXISTS (SELECT 1 FROM Product_Comment r WHERE r.parent_comment_id = c.comment_id) " +
                            "  THEN 1 ELSE 0 END AS can_delete " +
                            "FROM Product_Comment c " +
                            "JOIN users u ON u.id = c.user_id " +
                            "WHERE c.product_id = :pid " +
                            "ORDER BY c.created_at ASC";
            Query q = em.createNativeQuery(sql);
            q.setParameter("pid", productId);
            q.setParameter("curUid", currentUserId == null ? -1L : currentUserId);

            List<Object[]> rows = q.getResultList();

            // ... (Logic Dựng map parent->children giữ nguyên)
            class Node {
                Long id, parentId, uid;         // <-- ĐÃ THÊM ;
                String uname, content;            // <-- ĐÃ THÊM ;
                Date ts;                        // <-- ĐÃ THÊM ;
                boolean canDel;                 // <-- ĐÃ THÊM ;
                List<Node> children = new ArrayList<>(); // <-- ĐÃ THÊM ;
            }

            java.util.Map<Long, Node> byId = new java.util.HashMap<>();
            List<Node> roots = new ArrayList<>();

            for (Object[] r : rows) {
                Node n = new Node();
                n.id      = toLong(r[0]);    // <-- ĐÃ THÊM ;
                n.parentId= toLong(r[1]);    // <-- ĐÃ THÊM ;
                n.uid     = toLong(r[2]);    // <-- ĐÃ THÊM ;
                n.uname   = toStr(r[3]);     // <-- ĐÃ THÊM ;
                n.content = toStr(r[4]);     // <-- ĐÃ THÊM ;
                n.ts      = toDate(r[5]);    // <-- ĐÃ THÊM ;
                n.canDel  = toInt(r[6]) == 1; // <-- ĐÃ THÊM ;
                byId.put(n.id, n);
            }
            // build tree
            for (Node n : byId.values()) {
                if (n.parentId == null) {
                    roots.add(n);
                } else {
                    Node p = byId.get(n.parentId);
                    if (p != null) p.children.add(n);
                    else roots.add(n); // parent bị thiếu thì đẩy lên root để không mất comment
                }
            }

            // DFS để ra list phẳng + depth
            List<ThreadItem> out = new ArrayList<>();
            java.util.function.BiConsumer<Node,Integer> dfs = new java.util.function.BiConsumer<Node,Integer>() {
                @Override public void accept(Node node, Integer depth) {
                    out.add(new ThreadItem(
                            node.id, node.parentId, node.uid, node.uname,
                            node.content, node.ts, depth, node.canDel
                    ));
                    // children theo thời gian tạo tăng dần
                    node.children.sort((a,b) -> a.ts == null || b.ts == null ? 0 : a.ts.compareTo(b.ts));
                    for (Node c : node.children) this.accept(c, depth + 1);
                }
            };
            roots.sort((a,b) -> a.ts == null || b.ts == null ? 0 : a.ts.compareTo(b.ts));
            for (Node r0 : roots) dfs.accept(r0, 0);


            return out;
            // Không cần finally em.close()
        }
    }

    /* ============================ WRITE ============================ */

    /** Thêm bình luận cho 1 sản phẩm (giữ nguyên) */
    public void add(Long userId, Long productId, String content) {
        if (userId == null || productId == null) return;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                String ins =
                        "INSERT INTO Product_Comment (product_id, user_id, content, created_at) " +
                                "VALUES (:pid, :uid, :content, :ts)";
                em.createNativeQuery(ins)
                        .setParameter("pid", productId)
                        .setParameter("uid", userId)
                        .setParameter("content", safe(content))
                        .setParameter("ts", Timestamp.valueOf(LocalDateTime.now()))
                        .executeUpdate();

                tx.commit();
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    /** Thêm reply cho 1 comment (parentId có thể là của người khác hoặc của chính mình) */
    public int addReply(Long userId, Long productId, Long parentCommentId, String content) {
        if (userId == null || productId == null || parentCommentId == null) return 0;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                // Xác thực parent thuộc đúng product
                String chk = "SELECT COUNT(1) FROM Product_Comment WHERE comment_id = :pcid AND product_id = :pid";
                Number ok = (Number) em.createNativeQuery(chk)
                        .setParameter("pcid", parentCommentId)
                        .setParameter("pid", productId)
                        .getSingleResult();
                if (ok == null || ok.intValue() == 0) {
                    tx.rollback();
                    return 0; // parent không hợp lệ
                }

                String ins =
                        "INSERT INTO Product_Comment (product_id, user_id, content, created_at, parent_comment_id) " +
                                "VALUES (:pid, :uid, :content, :ts, :parentId)";
                int affected = em.createNativeQuery(ins)
                        .setParameter("pid", productId)
                        .setParameter("uid", userId)
                        .setParameter("content", safe(content))
                        .setParameter("ts", Timestamp.valueOf(LocalDateTime.now()))
                        .setParameter("parentId", parentCommentId)
                        .executeUpdate();

                tx.commit();
                return affected;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    /** Xoá 1 comment theo id (tuỳ chọn: ràng buộc đúng user) – giữ nguyên */
    public int deleteById(Long commentId, Long userId) {
        if (commentId == null) return 0;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                String del = (userId == null)
                        ? "DELETE FROM Product_Comment WHERE comment_id = :cid"
                        : "DELETE FROM Product_Comment WHERE comment_id = :cid AND user_id = :uid";

                Query q = em.createNativeQuery(del).setParameter("cid", commentId);
                if (userId != null) q.setParameter("uid", userId);
                int affected = q.executeUpdate();

                tx.commit();
                return affected;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    /** ✅ Xoá 1 comment nếu là của user đó, chưa quá 24h và CHƯA có reply */
    public int deleteByIdWithin24h(Long commentId, Long userId) {
        if (commentId == null || userId == null) return 0;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();

                // Dùng alias để có thể NOT EXISTS subquery kiểm tra reply
                String del =
                        "DELETE c " +
                                "FROM Product_Comment c " +
                                "WHERE c.comment_id = :cid " +
                                "  AND c.user_id = :uid " +
                                "  AND DATEDIFF(hour, c.created_at, GETDATE()) <= 24 " +
                                "  AND NOT EXISTS (SELECT 1 FROM Product_Comment r WHERE r.parent_comment_id = c.comment_id)";

                int affected = em.createNativeQuery(del)
                        .setParameter("cid", commentId)
                        .setParameter("uid", userId)
                        .executeUpdate();

                tx.commit();
                return affected;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    /** Xoá tất cả bình luận của 1 user cho 1 sản phẩm (tuỳ chọn dùng) – giữ nguyên */
    public int deleteByUser(Long userId, Long productId) {
        if (userId == null || productId == null) return 0;
        // Dùng try-with-resources
        try (EntityManager em = em()) {
            EntityTransaction tx = em.getTransaction();
            try {
                tx.begin();
                String del = "DELETE FROM Product_Comment WHERE product_id = :pid AND user_id = :uid";
                int affected = em.createNativeQuery(del)
                        .setParameter("pid", productId)
                        .setParameter("uid", userId)
                        .executeUpdate();
                tx.commit();
                return affected;
            } catch (Exception e) {
                if (tx.isActive()) tx.rollback();
                throw e;
            }
            // Không cần finally em.close()
        }
    }

    /* ============================ Helpers ============================ */

    // ... (Helpers: safe, toStr, toLong, toInt, toLdt, toDate giữ nguyên)
    private static String safe(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
    private static String toStr(Object o) {
        return (o == null) ? "" : o.toString();
    }
    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number) return ((Number) o).longValue();
        try { return Long.parseLong(String.valueOf(o)); } catch (Exception e) { return null; }
    }
    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return 0; }
    }

    // Vẫn để lại toLdt nếu nơi khác cần dùng
    private static LocalDateTime toLdt(Object ts) {
        if (ts == null) return null;
        if (ts instanceof Timestamp) return ((Timestamp) ts).toLocalDateTime();
        try {
            return Timestamp.valueOf(String.valueOf(ts)).toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

    // Dùng cho JSP fmt:formatDate
    private static Date toDate(Object ts) {
        if (ts == null) return null;
        if (ts instanceof Date) return (Date) ts;
        if (ts instanceof Timestamp) return new Date(((Timestamp) ts).getTime());
        try {
            return new Date(Timestamp.valueOf(String.valueOf(ts)).getTime());
        } catch (Exception e) {
            return null;
        }
    }
}