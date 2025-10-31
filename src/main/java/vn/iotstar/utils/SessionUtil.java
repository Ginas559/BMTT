//src/main/java/vn/iotstar/utils/SessionUtil.java
package vn.iotstar.utils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtil {
    private SessionUtil(){}

    public static Long currentUserId(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        if (ss == null) return null;
        Object idObj = ss.getAttribute("userId");
        if (idObj == null) return null;
        try { return (idObj instanceof Long) ? (Long) idObj : Long.valueOf(String.valueOf(idObj)); }
        catch (Exception e) { return null; }
    }

    public static String currentRole(HttpServletRequest req) {
        HttpSession ss = req.getSession(false);
        return (ss != null && ss.getAttribute("role") != null)
                ? String.valueOf(ss.getAttribute("role")) : null;
    }
}
