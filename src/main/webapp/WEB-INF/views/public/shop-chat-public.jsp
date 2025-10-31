<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="jakarta.tags.core"%>

<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${pageTitle != null ? pageTitle : 'BMTT Shop'}</title>

<sitemesh:write property="head" />

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/web2.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>
<%-- Thiết lập Context Path --%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- 
  [KHÔNG THAY ĐỔI] 
  Logic lấy Nickname của bro đã "xịn" rồi, giữ nguyên 100%.
--%>
<c:set var="u" value="${not empty sessionScope.currentUser ? sessionScope.currentUser : sessionScope.user}" />
<c:set var="roleAttr" value="${sessionScope.role}" />
<c:set var="emailAttr" value="${sessionScope.email}" />
<c:choose>
    <c:when test="${not empty u}">
        <c:set var="rolePart" value="${empty u.role ? (empty roleAttr ? 'GUEST' : roleAttr) : u.role}" />
        <c:set var="namePart"
               value="${not empty u.firstname
                      ? u.firstname
                      : (not empty u.fullName
                        ? u.fullName
                        : (not empty u.email
                          ? fn:substringBefore(u.email,'@')
                          : (not empty emailAttr ? fn:substringBefore(emailAttr,'@') : 'user')))}" />
    </c:when>
    <c:otherwise>
        <c:set var="rolePart" value="${empty roleAttr ? 'GUEST' : roleAttr}" />
        <c:set var="namePart" value="${not empty emailAttr ? fn:substringBefore(emailAttr,'@') : 'user'}" />
    </c:otherwise>
</c:choose>
<c:set var="nick" value="${rolePart}_${namePart}" />

<%--- 
  ĐÃ XÓA: Xóa thẻ <style> inline "lỏ" (5 điểm).
  Tất cả style sẽ được đưa vào web2.css (10 điểm).
---%>

<%-- ĐÃ THÊM: class "main-public-chat" để "ăn" nền pastel --%>
<main class="main-public-chat">
  
  <%-- 
    ĐÃ THÊM: "chat-wrapper" (Khung kính mờ "Pro Max")
    Đây là khung chính chứa toàn bộ giao diện chat
  --%>
  <div class="chat-wrapper">
    
    <%-- ĐÃ NÂNG CẤP: Tiêu đề "gradient-text" --%>
    <h3 class="mb-3 gradient-text" style="font-weight: 700; text-align: center;">
      💬 Phòng chat: <c:out value="${shop.shopName}" />
    </h3>

    <%-- 
      ĐÃ NÂNG CẤP: Khung chat 
      Xóa class "border rounded bg-light p-3"
    --%>
    <div id="chatBox" class="mb-3"></div>

    <%-- ĐÃ NÂNG CẤP: Form nhập tin nhắn --%>
    <form id="chatForm" onsubmit="return false;" class="d-flex gap-2">
      <input id="msgInput" class="form-control" placeholder="Nhập tin nhắn…" />
      
      <%-- ĐÃ NÂNG CẤP: Nút "Gửi" -> icon Paper Plane "cháy" --%>
      <button id="sendBtn" type="button" class="btn btn-primary">
        <i class="bi bi-send-fill"></i>
      </button>
    </form>
  </div>
</main>


<%--- Logic JavaScript cho WebSocket (ĐÃ NÂNG CẤP) ---%>
<script>
(function () {
  const ctx    = '${ctx}';
  const shopId = '${param.shopId}';

  if (!shopId) {
    console.warn('Missing shopId');
    return;
  }

  const nick = encodeURIComponent('${fn:escapeXml(nick)}');
  const wsUrl = (location.protocol === 'https:' ? 'wss://' : 'ws://')
              + location.host + ctx + '/ws/chat/' + shopId + '?room=public&nick=' + nick;

  const box   = document.getElementById('chatBox');
  const input = document.getElementById('msgInput');
  const btn   = document.getElementById('sendBtn');

  // ==================================================================
  // NÂNG CẤP "TUYỆT TRẦN" (HÀM APPEND)
  // Hàm "vẽ" tin nhắn (thay vì chỉ in text)
  // ==================================================================
  function append(t) {
    const d = document.createElement('div');
    d.classList.add('msg');
    
    // 1. Kiểm tra tin nhắn HỆ THỐNG (Connect, Disconnect, Error)
    if (t.startsWith('🔌') || t.startsWith('❌') || t.startsWith('⚠️')) {
      d.classList.add('msg-system'); // Thêm class cho tin nhắn hệ thống
      d.textContent = t;
    } 
    // 2. Xử lý tin nhắn NGƯỜI DÙNG (định dạng "NICK: Message")
    else {
      // Tách tin nhắn tại dấu hai chấm (:) ĐẦU TIÊN
      const parts = t.split(/:(.*)/s);
      
      if (parts.length > 1) { // Nếu đúng định dạng "NICK: Message"
        const nickSpan = document.createElement('strong');
        nickSpan.classList.add('msg-nick');
        nickSpan.textContent = parts[0] + ': '; // Nick (VD: "VENDOR_Tung:")
        
        const textSpan = document.createElement('span');
        textSpan.classList.add('msg-text');
        textSpan.textContent = parts[1].trim(); // Message (VD: "Chào shop")

        d.appendChild(nickSpan);
        d.appendChild(textSpan);
      } else {
        // Fallback: Nếu tin nhắn không có định dạng (VD: tin từ server)
        d.classList.add('msg-system'); // Coi như tin hệ thống
        d.textContent = t;
      }
    }
    
    box.appendChild(d);
    // Cuộn xuống cuối
    box.scrollTop = box.scrollHeight;
  }
  // ==================================================================
  // HẾT PHẦN NÂNG CẤP
  // ==================================================================


  let ws;
  function connect() {
    ws = new WebSocket(wsUrl);
    ws.onopen    = () => append('🔌 Đã kết nối.');
    ws.onmessage = (e) => append(e.data);
    ws.onclose   = () => append('❌ Mất kết nối.');
    ws.onerror   = () => append('⚠️ Lỗi kết nối.');
  }

  // Sự kiện nút Gửi
  btn.addEventListener('click', () => {
    const t = (input.value || '').trim();
    if (!t || !ws || ws.readyState !== 1) return;
    ws.send(t);
    input.value = '';
  });

  // Sự kiện phím Enter
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      btn.click();
    }
  });

  connect();
})();
</script>