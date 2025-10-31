package vn.iotstar.controllers.order;

import jakarta.persistence.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.*;

/**
 * Xử lý bước E: Checkout (COD)
 *
 * GET  /checkout  → hiển thị form chọn địa chỉ & xác nhận đơn hàng
 * POST /checkout  → tạo đơn hàng từ giỏ hàng, lưu DB (COD)
 */
@WebServlet(urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    private Long resolveUserId(HttpSession session) {
        Object val = session.getAttribute("userId");
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        Long demo = 1L;
        session.setAttribute("userId", demo);
        return demo;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);
        EntityManager em = JPAConfig.getEntityManager();

        try {
            // Load giỏ hàng
            TypedQuery<Cart> q = em.createQuery(
                "SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user.id = :uid",
                Cart.class);
            q.setParameter("uid", userId);
            List<Cart> carts = q.getResultList();
            Cart cart = carts.isEmpty() ? null : carts.get(0);

            // Load danh sách địa chỉ
            TypedQuery<Address> qa = em.createQuery(
                "SELECT a FROM Address a WHERE a.user.id = :uid", Address.class);
            qa.setParameter("uid", userId);
            List<Address> addresses = qa.getResultList();

            req.setAttribute("cart", cart);
            req.setAttribute("addresses", addresses);
        } catch (Exception e) {
            req.setAttribute("error", "Không tải được dữ liệu checkout: " + e.getMessage());
        } finally {
            em.close();
        }

        req.getRequestDispatcher("/WEB-INF/views/order/checkout.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String addressIdStr = req.getParameter("addressId");

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // Lấy giỏ hàng
            TypedQuery<Cart> q = em.createQuery(
                "SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.user.id = :uid",
                Cart.class);
            q.setParameter("uid", userId);
            Cart cart = q.getResultList().isEmpty() ? null : q.getResultList().get(0);
            if (cart == null || cart.getCartItems().isEmpty()) {
                session.setAttribute("flash_error", "Giỏ hàng trống, không thể thanh toán.");
                resp.sendRedirect(req.getContextPath() + "/cart");
                return;
            }

            // Lấy địa chỉ giao hàng
            Address address = em.find(Address.class, Long.valueOf(addressIdStr));
            if (address == null) {
                session.setAttribute("flash_error", "Chưa chọn địa chỉ giao hàng hợp lệ.");
                resp.sendRedirect(req.getContextPath() + "/checkout");
                return;
            }

            // =============================
            // ✅ Tính tổng tiền (có giảm giá)
            // =============================
            BigDecimal total = BigDecimal.ZERO;
            for (CartItem ci : cart.getCartItems()) {
                BigDecimal price = ci.getProduct().getPrice();
                total = total.add(price.multiply(BigDecimal.valueOf(ci.getQuantity())));
            }

            // --- Áp dụng giảm giá voucher (nếu có) ---
            BigDecimal discount = BigDecimal.ZERO;
            Object v = session.getAttribute("voucherDiscount");
            if (v instanceof BigDecimal) discount = (BigDecimal) v;
            if (discount.compareTo(BigDecimal.ZERO) < 0) discount = BigDecimal.ZERO;

            BigDecimal finalTotal = total.subtract(discount);
            if (finalTotal.compareTo(BigDecimal.ZERO) < 0)
                finalTotal = BigDecimal.ZERO;

            // Lấy shop đầu tiên trong giỏ (giả định 1 shop)
            Shop shop = cart.getCartItems().get(0).getProduct().getShop();

            // Tạo đơn hàng
            Order order = new Order();
            order.setUser(em.getReference(User.class, userId));
            order.setShop(shop);
            order.setAddress(address);
            order.setTotalAmount(finalTotal); // ✅ Đã trừ giảm giá
            order.setPaymentMethod(Order.PaymentMethod.COD);
            order.setStatus(Order.OrderStatus.NEW);
            em.persist(order);

            // Chuyển từng item -> OrderItem
            List<OrderItem> orderItems = cart.getCartItems().stream()
                    .map(ci -> OrderItem.builder()
                            .order(order)
                            .product(ci.getProduct())
                            .price(ci.getProduct().getPrice())
                            .quantity(ci.getQuantity())
                            .discount(BigDecimal.ZERO)
                            .build())
                    .collect(Collectors.toList());

            for (OrderItem oi : orderItems) {
                em.persist(oi);
            }

            tx.commit();
            session.setAttribute("flash", "Đặt hàng thành công! Mã đơn #" + order.getOrderId());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            session.setAttribute("flash_error", "Không thể đặt hàng: " + e.getMessage());
        } finally {
            em.close();
        }

        resp.sendRedirect(req.getContextPath() + "/orders");
    }

}
