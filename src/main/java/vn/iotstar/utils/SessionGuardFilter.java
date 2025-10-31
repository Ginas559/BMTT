// filepath: src/main/java/vn/iotstar/utils/SessionGuardFilter.java
package vn.iotstar.utils;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.*;
import java.io.IOException;
import vn.iotstar.entities.User;

/**
 * Vá session: nếu đã có currentUser nhưng thiếu userId/email/role
 * thì back-fill để các trang (như /product/*) đọc được ngay.
 */
@WebFilter("/*")
public class SessionGuardFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    HttpSession ss = req.getSession(false);

    if (ss != null) {
      Object userObj = ss.getAttribute("currentUser");
      Object uidObj  = ss.getAttribute("userId");

      // nếu đã login (có currentUser) mà thiếu userId -> bổ sung
      if (userObj instanceof User && uidObj == null) {
        try {
          Long uid = ((User) userObj).getId();
          if (uid != null) ss.setAttribute("userId", uid);
        } catch (Throwable ignored) {}
      }

      // bổ sung email/role nếu thiếu (không bắt buộc nhưng hữu ích cho header)
      if (userObj instanceof User) {
        User u = (User) userObj;
        if (ss.getAttribute("email") == null && u.getEmail() != null) {
          ss.setAttribute("email", u.getEmail());
        }
        if (ss.getAttribute("role") == null && u.getRole() != null) {
          ss.setAttribute("role", u.getRole().name());
        }
      }
    }

    chain.doFilter(request, response);
  }
}
