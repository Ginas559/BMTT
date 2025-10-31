package vn.iotstar.controllers.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.*;
import java.lang.reflect.Method; // NEW

/**
 * Quản lý giỏ hàng trong DB.
 *
 * URL Mapping (đã khai báo trong web.xml):
 *  GET  /cart              → hiển thị giỏ hàng
 *  POST /cart/add          → thêm sản phẩm
 *  POST /cart/update       → cập nhật số lượng
 *  POST /cart/delete       → xoá sản phẩm
 */
public class CartServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    /** Lấy userId tạm từ session (mock) */
    private Long resolveUserId(HttpSession session) {
        Object val = session.getAttribute("userId");
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        Long demo = 1L;
        session.setAttribute("userId", demo);
        return demo;
    }

    // -------------------- GET --------------------
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);
        EntityManager em = JPAConfig.getEntityManager();

        try {
            TypedQuery<Cart> q = em.createQuery(
                "SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user.id = :uid",
                Cart.class);
            q.setParameter("uid", userId);
            List<Cart> carts = q.getResultList();
            Cart cart = carts.isEmpty() ? null : carts.get(0);

            req.setAttribute("cart", cart);
        } catch (Exception e) {
            req.setAttribute("error", "Không tải được giỏ hàng: " + e.getMessage());
        } finally {
            em.close();
        }

        req.getRequestDispatcher("/WEB-INF/views/cart/cart.jsp").forward(req, resp);
    }

    // -------------------- POST --------------------
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();
        if (path == null) path = "";

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        switch (path.toLowerCase(Locale.ROOT)) {
            case "/add" -> add(req, resp, userId);
            case "/update" -> update(req, resp, userId);
            case "/delete" -> delete(req, resp, userId);
            default -> resp.sendRedirect(req.getContextPath() + "/cart");
        }
    }

    // -------------------- Helpers --------------------

    /** parse helpers */
    private Long parseLongSafe(String s) {
        try { return (s == null || s.isBlank()) ? null : Long.valueOf(s); }
        catch (Exception e) { return null; }
    }
    private int parseIntSafe(String s, int d) {
        try { return (s == null || s.isBlank()) ? d : Integer.parseInt(s); }
        catch (Exception e) { return d; }
    }

    /** Thêm sản phẩm vào giỏ */
    private void add(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        HttpSession session = req.getSession(); // keep
        String productIdStr = req.getParameter("productId");
        String qtyStr = req.getParameter("quantity");
        String variantIdStr = req.getParameter("variantId"); // NEW

        int quantity = parseIntSafe(qtyStr, 1);
        quantity = Math.max(1, quantity);

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Lấy/khởi tạo Cart
            TypedQuery<Cart> q = em.createQuery(
                "SELECT c FROM Cart c WHERE c.user.id = :uid", Cart.class);
            q.setParameter("uid", userId);
            List<Cart> carts = q.getResultList();
            Cart cart = carts.isEmpty() ? null : carts.get(0);
            if (cart == null) {
                cart = new Cart();
                cart.setUser(em.getReference(User.class, userId));
                em.persist(cart);
            }

            Long productId = Long.valueOf(productIdStr);
            Product product = em.getReference(Product.class, productId);

            // === NEW: Kiểm tra sản phẩm có biến thể không
            Long numVar = em.createQuery(
                "SELECT COUNT(v) FROM ProductVariant v WHERE v.product.productId = :pid", Long.class)
                .setParameter("pid", productId)
                .getSingleResult();
            boolean hasVariants = (numVar != null && numVar.longValue() > 0L);

            ProductVariant chosenVariant = null;
            if (hasVariants) {
                Long variantId = parseLongSafe(variantIdStr);
                if (variantId == null) {
                    tx.rollback();
                    session.setAttribute("flash_error", "Vui lòng chọn màu và size (thiếu biến thể).");
                    resp.sendRedirect(req.getContextPath() + "/product/" + productId);
                    return;
                }
                // Tải biến thể & validate product match
                chosenVariant = em.find(ProductVariant.class, variantId);
                if (chosenVariant == null || chosenVariant.getProduct() == null
                        || !chosenVariant.getProduct().getProductId().equals(productId)) {
                    tx.rollback();
                    session.setAttribute("flash_error", "Biến thể không hợp lệ.");
                    resp.sendRedirect(req.getContextPath() + "/product/" + productId);
                    return;
                }
                int stock = (chosenVariant.getStock() == null) ? 0 : chosenVariant.getStock();
                if (stock <= 0) {
                    tx.rollback();
                    session.setAttribute("flash_error", "Biến thể đã hết hàng.");
                    resp.sendRedirect(req.getContextPath() + "/product/" + productId);
                    return;
                }
                if (quantity > stock) {
                    quantity = stock; // chặn vượt tồn, hạ về tối đa
                    if (quantity <= 0) {
                        tx.rollback();
                        session.setAttribute("flash_error", "Số lượng vượt tồn kho.");
                        resp.sendRedirect(req.getContextPath() + "/product/" + productId);
                        return;
                    }
                    // có thể báo nhẹ: giảm về stock
                    session.setAttribute("flash", "Số lượng đã được điều chỉnh về tồn kho tối đa (" + stock + ").");
                }
            }

            // === Tìm CartItem hiện có
            CartItem item = null;

            if (hasVariants && chosenVariant != null && hasCartItemVariantField()) {
                // Nếu CartItem có field productVariant → query theo cả variant
                TypedQuery<CartItem> qi = em.createQuery(
                    "SELECT ci FROM CartItem ci " +
                    "WHERE ci.cart.cartId = :cid AND ci.product.productId = :pid AND ci.productVariant.variantId = :vid",
                    CartItem.class);
                qi.setParameter("cid", cart.getCartId());
                qi.setParameter("pid", productId);
                qi.setParameter("vid", chosenVariant.getVariantId());
                List<CartItem> found = qi.getResultList();
                item = found.isEmpty() ? null : found.get(0);
            } else {
                // Fallback: như cũ (không có field productVariant)
                TypedQuery<CartItem> qi = em.createQuery(
                    "SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cid AND ci.product.productId = :pid",
                    CartItem.class);
                qi.setParameter("cid", cart.getCartId());
                qi.setParameter("pid", productId);
                List<CartItem> found = qi.getResultList();
                item = found.isEmpty() ? null : found.get(0);
            }

            if (item != null) {
                // Tăng số lượng (nếu có variant → vẫn tôn trọng stock của biến thể)
                if (hasVariants && chosenVariant != null) {
                    int stock = (chosenVariant.getStock() == null) ? 0 : chosenVariant.getStock();
                    int newQty = item.getQuantity() + quantity;
                    if (newQty > stock) newQty = stock;
                    item.setQuantity(newQty);
                    // set variant nếu có field
                    setCartItemVariantIfSupported(item, chosenVariant);
                } else {
                    item.setQuantity(item.getQuantity() + quantity);
                }
                em.merge(item);
            } else {
                // Tạo CartItem mới
                item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(quantity);
                // Nếu có biến thể & CartItem hỗ trợ → set bằng reflection
                if (hasVariants && chosenVariant != null) {
                    setCartItemVariantIfSupported(item, chosenVariant);
                }
                em.persist(item);
            }

            tx.commit();
            session.setAttribute("flash", "Đã thêm sản phẩm vào giỏ hàng!");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            session.setAttribute("flash_error", "Lỗi thêm sản phẩm: " + e.getMessage());
        } finally {
            em.close();
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /** Cập nhật số lượng */
    private void update(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        HttpSession session = req.getSession(); // keep
        String itemIdStr = req.getParameter("itemId");
        String qtyStr = req.getParameter("quantity");

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            CartItem item = em.find(CartItem.class, Long.valueOf(itemIdStr));
            if (item != null && item.getCart().getUser().getId().equals(userId)) {
                int newQty = Math.max(1, parseIntSafe(qtyStr, 1));

                // Nếu có biến thể và CartItem hỗ trợ, giới hạn theo stock biến thể
                ProductVariant pv = getCartItemVariantIfSupported(item);
                if (pv != null) {
                    pv = em.find(ProductVariant.class, pv.getVariantId()); // refresh
                    int stock = (pv != null && pv.getStock() != null) ? pv.getStock() : Integer.MAX_VALUE;
                    if (newQty > stock) newQty = stock;
                }

                item.setQuantity(newQty);
                em.merge(item);
            }
            tx.commit();
            session.setAttribute("flash", "Đã cập nhật số lượng.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            session.setAttribute("flash_error", "Không thể cập nhật: " + e.getMessage());
        } finally {
            em.close();
        }
        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    /** Xoá sản phẩm khỏi giỏ */
    private void delete(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        HttpSession session = req.getSession(); // keep
        String itemIdStr = req.getParameter("itemId");

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            CartItem item = em.find(CartItem.class, Long.valueOf(itemIdStr));
            if (item != null && item.getCart().getUser().getId().equals(userId)) {
                em.remove(item);
            }
            tx.commit();
            session.setAttribute("flash", "Đã xoá sản phẩm khỏi giỏ hàng.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            session.setAttribute("flash_error", "Không xoá được: " + e.getMessage());
        } finally {
            em.close();
        }

        resp.sendRedirect(req.getContextPath() + "/cart");
    }

    // ---------- Reflection helpers để không phá build nếu CartItem chưa có field productVariant ----------
    private boolean hasCartItemVariantField() {
        try {
            Class<?> c = CartItem.class;
            // ưu tiên setter
            for (Method m : c.getMethods()) {
                if (m.getName().equals("setProductVariant") && m.getParameterCount() == 1) {
                    return true;
                }
            }
            // hoặc getter field
            for (Method m : c.getMethods()) {
                if (m.getName().equals("getProductVariant") && m.getParameterCount() == 0) {
                    return true;
                }
            }
        } catch (Exception ignore) {}
        return false;
    }

    private void setCartItemVariantIfSupported(CartItem item, ProductVariant pv) {
        try {
            Method m = CartItem.class.getMethod("setProductVariant", ProductVariant.class);
            m.invoke(item, pv);
        } catch (NoSuchMethodException miss) {
            // bỏ qua nếu không có field (fallback sản phẩm-only)
        } catch (Exception ignore) {}
    }

    private ProductVariant getCartItemVariantIfSupported(CartItem item) {
        try {
            Method m = CartItem.class.getMethod("getProductVariant");
            Object r = m.invoke(item);
            if (r instanceof ProductVariant) return (ProductVariant) r;
        } catch (NoSuchMethodException miss) {
            // không có field
        } catch (Exception ignore) {}
        return null;
    }
}
