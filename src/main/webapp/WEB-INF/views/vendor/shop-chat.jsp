<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
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

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- [GIỮ NGUYÊN] Logic lấy Nickname "Pro Max" --%>
<c:set var="u" value="${not empty sessionScope.currentUser ? sessionScope.currentUser : sessionScope.user}" />
<c:choose>
  <c:when test="${not empty u}">
    <c:set var="rolePart" value="${empty u.role ? 'USER' : u.role}" />
    <c:set var="namePart"
           value="${not empty u.firstname   
                   ? u.firstname           
                   : (not empty u.fullName
                       ? u.fullName
                       : (not empty u.email ? fn:substringBefore(u.email,'@') : 'guest'))}" />
    <c:set var="nick" value="${rolePart}_${namePart}" />
  </c:when>
  <c:otherwise>
    <c:set var="nick" value="GUEST_user" />
  </c:otherwise>
</c:choose>

<%-- 
  ĐÃ THÊM: class "main-chat" (để ăn nền pastel)
  File JSP của bro đã có sẵn, rất tốt.
--%>
<div class="main-chat py-4">
  <h3 class="mb-3 gradient-text">💬 Phòng chat của shop: <c:out value="${shop.shopName}" /></h3>

  <%-- 
    [GIỮ NGUYÊN] Các class "mồi" (hook) của bro rất chuẩn 
    CSS V11 sẽ "tóm" lấy các class này
  --%>
  <div id="chatBox" class="card kpi-card chat-box-pink p-3 mb-3"
       style="height: 420px; overflow-y: auto;"></div>

  <%-- 
    [GIỮ NGUYÊN] Dùng "chat-form-blue" 
  --%>
  <div class="card kpi-card chat-form-blue">
    <div class="card-body py-2">
      <form id="chatForm" onsubmit="return false;" class="d-flex gap-2">
        <input id="msgInput" class="form-control" placeholder="Nhập tin nhắn…" />
        
        <%-- ĐÃ SỬA: Dùng "btn-primary" để "ăn" style "cháy" của card xanh --%>
        <button id="sendBtn" type="button" class="btn btn-primary">Gửi</button>
      </form>
    </div>
  </div>

</div>

<%--- Logic JavaScript cho WebSocket (ĐÃ NÂNG CẤP "PRO MAX") ---%>
<script>
(function () {
  const ctx    = '${ctx}';
  const shopId = '${param.shopId}';
  if (!shopId) { console.warn('Missing shopId'); return; }

  const nick = encodeURIComponent('${fn:escapeXml(nick)}');

  const wsUrl = (location.protocol === 'https:' ? 'wss://' : 'ws://')
              + location.host + ctx + '/ws/chat/' + shopId + '?nick=' + nick;

  const box   = document.getElementById('chatBox');
  const input = document.getElementById('msgInput');
  const btn   = document.getElementById('sendBtn');

  // ==================================================================
  // NÂNG CẤP "PRO MAX" (HÀM APPEND) (Giống V10)
  // Hàm "vẽ" tin nhắn (thay vì chỉ in text)
  // ==================================================================
  function append(t) {
    const d = document.createElement('div');
    d.classList.add('msg');
    
    // 1. Kiểm tra tin nhắn HỆ THỐNG
    if (t.startsWith('🔌') || t.startsWith('❌') || t.startsWith('⚠️')) {
      d.classList.add('msg-system');
      d.textContent = t;
    } 
    // 2. Xử lý tin nhắn NGƯỜI DÙNG (định dạng "NICK: Message")
    else {
      const parts = t.split(/:(.*)/s); // Tách tại dấu : đầu tiên
      
      if (parts.length > 1) { // Nếu đúng định dạng
        const nickSpan = document.createElement('strong');
        nickSpan.classList.add('msg-nick');
        nickSpan.textContent = parts[0] + ': '; // Nick
        
        const textSpan = document.createElement('span');
        textSpan.classList.add('msg-text');
        textSpan.textContent = parts[1].trim(); // Message

        d.appendChild(nickSpan);
        d.appendChild(textSpan);
      } else {
        // Fallback: Tin không có định dạng
        d.classList.add('msg-system'); 
        d.textContent = t;
      }
    }
    
    box.appendChild(d);
    box.scrollTop = box.scrollHeight; // Cuộn xuống cuối
  }
  // ==================================================================
  // HẾT PHẦN NÂNG CẤP
  // ==================================================================

  let ws;
  function connect(){
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
    if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); btn.click(); }
  });

  connect();
})();
</script>