// path: src/main/java/vn/iotstar/controllers/user/AddressServlet.java
package vn.iotstar.controllers.user;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.Address;
import vn.iotstar.entities.User;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * CRUD địa chỉ giao hàng cho User.
 *
 * Routes (khớp web.xml):
 *  GET  /user/addresses            -> danh sách địa chỉ
 *  GET  /user/address/new          -> form tạo mới
 *  POST /user/address/new          -> tạo mới
 *  GET  /user/address/edit?id=...  -> form chỉnh sửa
 *  POST /user/address/edit         -> cập nhật
 *  POST /user/address/delete       -> xoá
 *
 * Ghi chú:
 * - TẠM thời lấy userId từ session, nếu chưa có sẽ mock = 1L (demo). Sau này thay JWT.
 * - Entity Address khớp với file bạn gửi: addressId, user (ManyToOne), receiverName, phone, addressDetail, isDefault.
 */
public class AddressServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // -------------------- Helpers --------------------

    private Long resolveUserId(HttpSession session) {
        Object val = session.getAttribute("userId");
        if (val instanceof Long) return (Long) val;
        if (val instanceof Integer) return ((Integer) val).longValue();
        Long demo = 1L; // Fallback demo
        session.setAttribute("userId", demo);
        return demo;
    }

    private boolean parseBool(String v) {
        if (v == null) return false;
        v = v.trim().toLowerCase(Locale.ROOT);
        return v.equals("true") || v.equals("1") || v.equals("on") || v.equals("yes");
    }

    private List<Address> findByUserId(EntityManager em, Long userId) {
        TypedQuery<Address> q = em.createQuery(
            "SELECT a FROM Address a WHERE a.user.id = :uid ORDER BY a.addressId DESC", Address.class);
        q.setParameter("uid", userId);
        return q.getResultList();
    }

    // -------------------- GET --------------------

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String servletPath = req.getServletPath(); // /user/addresses hoặc /user/address
        String pathInfo = req.getPathInfo();       // null | /new | /edit

        if ("/user/addresses".equals(servletPath)) {
            // LIST
            EntityManager em = JPAConfig.getEntityManager();
            try {
                List<Address> list = findByUserId(em, userId);
                req.setAttribute("addresses", list);
            } catch (Exception e) {
                req.setAttribute("error", "Không tải được danh sách địa chỉ: " + e.getMessage());
            } finally {
                em.close();
            }
            req.getRequestDispatcher("/WEB-INF/views/user/address_list.jsp").forward(req, resp);
            return;
        }

        // /user/address/*
        String action = (pathInfo == null) ? "" : pathInfo.trim().toLowerCase(Locale.ROOT);
        switch (action) {
            case "/new" -> {
                // show blank form
                req.getRequestDispatcher("/WEB-INF/views/user/address_form.jsp").forward(req, resp);
            }
            case "/edit" -> {
                String idStr = req.getParameter("id");
                if (idStr == null) {
                    resp.sendRedirect(req.getContextPath() + "/user/addresses");
                    return;
                }
                EntityManager em = JPAConfig.getEntityManager();
                try {
                    Address addr = em.find(Address.class, Long.valueOf(idStr));
                    req.setAttribute("address", addr);
                } catch (Exception e) {
                    req.setAttribute("error", "Không tải được địa chỉ: " + e.getMessage());
                } finally {
                    em.close();
                }
                req.getRequestDispatcher("/WEB-INF/views/user/address_form.jsp").forward(req, resp);
            }
            default -> resp.sendRedirect(req.getContextPath() + "/user/addresses");
        }
    }

    // -------------------- POST --------------------

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String servletPath = req.getServletPath(); // /user/address
        String pathInfo = req.getPathInfo();       // /new | /edit | /delete

        if (!"/user/address".equals(servletPath) || pathInfo == null) {
            resp.sendRedirect(req.getContextPath() + "/user/addresses");
            return;
        }

        String action = pathInfo.trim().toLowerCase(Locale.ROOT);
        switch (action) {
            case "/new" -> create(req, resp, userId);
            case "/edit" -> update(req, resp, userId);
            case "/delete" -> delete(req, resp, userId);
            default -> resp.sendRedirect(req.getContextPath() + "/user/addresses");
        }
    }

    private void create(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        String receiverName = req.getParameter("receiverName");
        String phone        = req.getParameter("phone");
        String addressDetail= req.getParameter("addressDetail");
        Boolean isDefault   = parseBool(req.getParameter("isDefault"));

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Address addr = new Address();
            addr.setReceiverName(receiverName);
            addr.setPhone(phone);
            addr.setAddressDetail(addressDetail);
            addr.setIsDefault(isDefault != null && isDefault);

            User userRef = em.getReference(User.class, userId);
            addr.setUser(userRef);

            em.persist(addr);
            tx.commit();
            req.getSession().setAttribute("flash", "Đã thêm địa chỉ.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            req.getSession().setAttribute("flash_error", "Không thêm được địa chỉ: " + e.getMessage());
        } finally {
            em.close();
        }
        resp.sendRedirect(req.getContextPath() + "/user/addresses");
    }

    private void update(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        String idStr        = req.getParameter("id");
        String receiverName = req.getParameter("receiverName");
        String phone        = req.getParameter("phone");
        String addressDetail= req.getParameter("addressDetail");
        Boolean isDefault   = parseBool(req.getParameter("isDefault"));

        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/user/addresses");
            return;
        }

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Address addr = em.find(Address.class, Long.valueOf(idStr));
            if (addr != null) {
                if (addr.getUser() == null || addr.getUser().getId() == null || !addr.getUser().getId().equals(userId)) {
                    addr.setUser(em.getReference(User.class, userId));
                }

                addr.setReceiverName(receiverName);
                addr.setPhone(phone);
                addr.setAddressDetail(addressDetail);
                addr.setIsDefault(isDefault != null && isDefault);

                em.merge(addr);
            }
            tx.commit();
            req.getSession().setAttribute("flash", "Đã cập nhật địa chỉ.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            req.getSession().setAttribute("flash_error", "Không cập nhật được địa chỉ: " + e.getMessage());
        } finally {
            em.close();
        }
        resp.sendRedirect(req.getContextPath() + "/user/addresses");
    }

    private void delete(HttpServletRequest req, HttpServletResponse resp, Long userId)
            throws IOException {
        String idStr = req.getParameter("id");
        if (idStr == null) {
            resp.sendRedirect(req.getContextPath() + "/user/addresses");
            return;
        }

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Address addr = em.find(Address.class, Long.valueOf(idStr));
            if (addr != null) {
                if (addr.getUser() != null && addr.getUser().getId() != null
                        && !addr.getUser().getId().equals(userId)) {
                    // không xoá nếu không phải của user hiện tại
                } else {
                    em.remove(addr);
                }
            }
            tx.commit();
            req.getSession().setAttribute("flash", "Đã xoá địa chỉ.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            req.getSession().setAttribute("flash_error", "Không xoá được địa chỉ: " + e.getMessage());
        } finally {
            em.close();
        }
        resp.sendRedirect(req.getContextPath() + "/user/addresses");
    }
}
