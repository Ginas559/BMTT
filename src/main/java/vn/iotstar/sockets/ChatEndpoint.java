// filepath: src/main/java/vn/iotstar/sockets/ChatEndpoint.java

package vn.iotstar.sockets;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

/**
 * Endpoint dùng chung cho các phòng chat theo Shop.
 *
 * Phòng nội bộ (VENDOR+STAFF): ws://.../ws/chat/{shopId}?nick=...
 * Phòng công khai (mọi người):  ws://.../ws/chat/{shopId}?room=public&nick=...
 */
@ServerEndpoint("/ws/chat/{shopId}")
public class ChatEndpoint {

    // roomKey -> set of sessions
    // Lưu trữ tất cả các Session đang hoạt động, được phân chia theo phòng (roomKey)
    private static final Map<String, Set<Session>> ROOMS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("shopId") String shopId) {
        // 1. Xác định roomKey (shopId hoặc pub:shopId)
        String roomKey = roomKey(session, shopId);

        // 2. Thêm Session vào Set tương ứng với roomKey
        ROOMS.computeIfAbsent(roomKey, k -> ConcurrentHashMap.newKeySet()).add(session);

        // 3. Cache nickname để dùng nhanh
        String display = resolveNick(session);
        session.getUserProperties().put("nick", display);

        // 4. Thông báo mọi người đã tham gia phòng
        broadcast(roomKey, "[JOIN] " + display + " đã tham gia phòng.");
    }

    @OnMessage
    public void onMessage(Session session, @PathParam("shopId") String shopId, String message) {
        String roomKey = roomKey(session, shopId);

        // Phát tin nhắn đến tất cả Session trong phòng
        broadcast(roomKey, nick(session) + ": " + message);
    }

    @OnClose
    public void onClose(Session session, @PathParam("shopId") String shopId) {
        String roomKey = roomKey(session, shopId);
        Set<Session> set = ROOMS.get(roomKey);

        if (set != null) {
            // 1. Xóa Session khỏi Set
            set.remove(session);

            // 2. Nếu Set trống, xóa cả roomKey khỏi Map
            if (set.isEmpty()) {
                ROOMS.remove(roomKey);
            }
        }

        // 3. Thông báo mọi người đã rời phòng
        broadcast(roomKey, "[LEAVE] " + nick(session) + " đã rời phòng.");
    }

    /* -------------------------------------------------------------------------- */
    /* Hàm hỗ trợ (Helpers)                         */
    /* -------------------------------------------------------------------------- */

    /**
     * Phát tin nhắn (payload) đến tất cả các Session trong phòng được chỉ định.
     */
    private static void broadcast(String roomKey, String payload) {
        Set<Session> set = ROOMS.get(roomKey);

        if (set == null) return;

        for (Session s : set) {
            try {
                s.getBasicRemote().sendText(payload);
            } catch (IOException ignored) {
                // Bỏ qua lỗi nếu không gửi được cho 1 Session cụ thể
            }
        }
    }

    /**
     * Lấy nickname từ UserProperties (đã cache), nếu không có thì gọi resolveNick() để giải quyết.
     */
    private static String nick(Session s) {
        Object n = s.getUserProperties().get("nick");
        return (n != null && !n.toString().isBlank()) ? n.toString() : resolveNick(s);
    }

    /**
     * Xác định khóa phòng (roomKey) dựa trên shopId và tham số truy vấn 'room'.
     *
     * room=public => "pub:{shopId}", ngược lại giữ nguyên "{shopId}" (phòng nội bộ).
     */
    private static String roomKey(Session s, String shopId) {
        String room = queryParam(s, "room");

        if ("public".equalsIgnoreCase(room)) {
            return "pub:" + shopId; // Khóa cho phòng chat công khai
        }

        return shopId; // Khóa mặc định cho phòng nội bộ
    }

    /**
     * Giải quyết (tìm) nickname của người dùng, ưu tiên từ query param 'nick', sau đó đến Principal.
     */
    private static String resolveNick(Session s) {
        String fromQuery = queryParam(s, "nick");
        if (fromQuery != null && !fromQuery.isBlank()) {
            return fromQuery.trim();
        }

        try {
            // Thử lấy từ Principal (nếu đã xác thực)
            if (s.getUserPrincipal() != null && s.getUserPrincipal().getName() != null) {
                return s.getUserPrincipal().getName();
            }
        } catch (Exception ignore) {
            // Bỏ qua lỗi
        }
        return "GUEST_user"; // Nickname mặc định
    }

    /**
     * Lấy giá trị của tham số truy vấn (query parameter) từ Session.
     */
    private static String queryParam(Session s, String name) {
        try {
            Map<String, java.util.List<String>> q = s.getRequestParameterMap();

            if (q == null) return null;

            var v = q.get(name);
            if (v == null || v.isEmpty()) return null;

            String val = v.get(0);
            return (val == null || val.isBlank()) ? null : val;

        } catch (Exception ignore) {
            return null;
        }
    }
}