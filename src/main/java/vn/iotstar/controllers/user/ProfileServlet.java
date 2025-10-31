// path: src/main/java/vn/iotstar/controllers/user/ProfileServlet.java
package vn.iotstar.controllers.user;

import java.io.IOException;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import vn.iotstar.configs.JPAConfig;
import vn.iotstar.entities.User;

/**
 * Xem & cập nhật hồ sơ người dùng.
 * TẠM THỜI lấy userId từ session (mock). Sau này sẽ thay bằng JWT.
 */
public class ProfileServlet extends HttpServlet {
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
            User user = em.find(User.class, userId);
            req.setAttribute("user", user);
        } finally {
            em.close();
        }

        req.getRequestDispatcher("/WEB-INF/views/user/profile.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");
        HttpSession session = req.getSession();
        Long userId = resolveUserId(session);

        String firstname = req.getParameter("firstname");
        String lastname  = req.getParameter("lastname");
        String phone     = req.getParameter("phone");
        String email     = req.getParameter("email");

        EntityManager em = JPAConfig.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user != null) {
                if (firstname != null && !firstname.isBlank()) user.setFirstname(firstname.trim());
                if (lastname  != null && !lastname.isBlank())   user.setLastname(lastname.trim());
                if (phone     != null && !phone.isBlank())      user.setPhone(phone.trim());
                if (email     != null && !email.isBlank())      user.setEmail(email.trim());
                em.merge(user);
            }
            tx.commit();
            req.setAttribute("message", "Cập nhật thông tin thành công!");
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            req.setAttribute("error", "Lỗi khi cập nhật hồ sơ: " + ex.getMessage());
        } finally {
            em.close();
        }

        doGet(req, resp);
    }
}
