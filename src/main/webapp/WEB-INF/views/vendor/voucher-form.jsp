<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
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
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<%-- ĐÃ THÊM: class "main-vendor-voucher-form" để "ăn" nền pastel --%>
<main class="container py-4 main-vendor-voucher-form">
  <div class="d-flex align-items-center justify-content-between mb-3">
    <%-- ĐÃ THÊM: class "gradient-text" (từ v1) cho "cháy" --%>
    <h2 class="mb-0 gradient-text" style="font-weight: 700;">
      <c:out value="${empty v and empty v_id ? 'Tạo voucher mới' : 'Cập nhật voucher'}"/>
    </h2>
  </div>

  <c:if test="${not empty errors}">
    <%-- ĐÃ THÊM: class và style cho "glow" đỏ --%>
    <div class="alert alert-danger" style="box-shadow: 0 0 15px rgba(220, 53, 69, 0.5);">
      <h5 class="alert-heading mb-2">Lỗi!</h5>
      <ul class="mb-0">
        <c:forEach var="e" items="${errors}">
          <li><c:out value="${e}"/></li>
        </c:forEach>
      </ul>
    </div>
  </c:if>

  <%-- 
    ĐÃ NÂNG CẤP: Dùng class "form-card-pink" (từ Fix V8)
    Nó sẽ tự động "hạ gục" tất cả input/select/button bên trong
  --%>
  <div class="card shadow-sm form-card-pink">
    <div class="card-body">
      <h5 class="card-title mb-3" style="font-weight: 600;">Thông tin voucher</h5>
      
      <form method="post" action="${ctx}/vendor/vouchers/save" class="row g-3">

        <c:if test="${not empty v or not empty v_id}">
          <input type="hidden" name="id" value="${not empty v ? v.voucherId : v_id}"/>
        </c:if>

        <c:set var="codeValue" value="${not empty v ? v.code : v_code}"/>
        <c:set var="typeValue" value="${not empty v ? v.type : v_type}"/>

        <div class="col-md-4">
          <label for="code" class="form-label">Code</label>
          <input name="code" id="code" class="form-control" required
                 value="${codeValue}"
                 onblur="this.value=this.value.trim().toUpperCase()"/>
        </div>

        <div class="col-md-3">
          <label for="typeSelect" class="form-label">Loại</label>
          <select name="type" id="typeSelect" class="form-select" required>
            <option value="">-- Chọn --</option>
            <option value="PERCENT" ${typeValue == 'PERCENT' ? 'selected' : ''}>PERCENT (theo sản phẩm)</option>
            <option value="AMOUNT"  ${typeValue == 'AMOUNT'  ? 'selected' : ''}>AMOUNT (theo đơn)</option>
          </select>
        </div>

        <div class="col-md-2 percent-group">
          <label for="percent" class="form-label">Percent (1–100)</label>
          <input name="percent" id="percent" type="number" step="0.01" min="1" max="100" class="form-control"
                 value="${not empty v ? v.percent : v_percent}"/>
        </div>

        <div class="col-md-3 amount-group">
          <label for="amount" class="form-label">Amount (>0)</label>
          <input name="amount" id="amount" type="number" step="0.01" min="0.01" class="form-control"
                 value="${not empty v ? v.amount : v_amount}"/>
        </div>

        <div class="col-md-3">
          <label for="minOrderAmount" class="form-label">Min Order (≥0)</label>
          <input name="minOrderAmount" id="minOrderAmount" type="number" step="0.01" min="0" class="form-control"
                 value="${not empty v ? v.minOrderAmount : v_minOrder}"/>
        </div>

        <div class="col-md-3">
          <label for="startAt" class="form-label">Start At</label>
          <input name="startAt" id="startAt" type="datetime-local" class="form-control"
                 value="${not empty v ? v.startAt : v_startAt}"/>
        </div>
        <div class="col-md-3">
          <label for="endAt" class="form-label">End At</label>
          <input name="endAt" id="endAt" type="datetime-local" class="form-control"
                 value="${not empty v ? v.endAt : v_endAt}"/>
        </div>

        <c:if test="${not empty v}">
          <div class="col-md-3">
            <label for="status" class="form-label">Trạng thái</label>
            <select name="status" id="status" class="form-select">
              <option value="ACTIVE" ${v.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
              <option value="INACTIVE" ${v.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
            </select>
          </div>
        </c:if>

        <div class="col-12 percent-group">
          <label for="productIds" class="form-label">Áp dụng cho sản phẩm</label>
          <%-- ĐÃ THÊM: Style cho select[multiple] cho đẹp --%>
          <select name="productIds" id="productIds" class="form-select" multiple size="8" style="font-family: monospace;">
            <c:forEach var="p" items="${products}">
              <c:set var="isSel" value="${not empty selectedProductIds and selectedProductIds.contains(p.productId)}"/>
              <option value="${p.productId}" ${isSel ? 'selected' : ''}>
                [#${p.productId}] ${p.productName}
              </option>
            </c:forEach>
          </select>
          <small class="text-muted">Giữ Ctrl/Cmd hoặc Shift để chọn nhiều sản phẩm.</small>
        </div>

        <div class="col-12 d-flex justify-content-end">
          <%-- Nút này sẽ "ăn" style từ Fix V9 --%>
          <a class="btn btn-outline-secondary me-2" href="${ctx}/vendor/vouchers">Hủy</a>
          <%-- Nút này sẽ "ăn" style hồng "cháy" từ Fix V8 --%>
          <button class="btn btn-primary" type="submit">Lưu</button>
        </div>
      </form>
    </div>
  </div>
</main>

<script>
(function(){
  const typeSelect = document.getElementById('typeSelect');
  const toggleGroups = () => {
    const t = typeSelect?.value;
    document.querySelectorAll('.percent-group').forEach(el => {
      el.style.display = (t === 'PERCENT') ? '' : 'none';
      el.querySelector('[name="percent"]')?.toggleAttribute('disabled', t !== 'PERCENT');
      el.querySelector('[name="productIds"]')?.toggleAttribute('disabled', t !== 'PERCENT');
    });
    document.querySelectorAll('.amount-group').forEach(el => {
      el.style.display = (t === 'AMOUNT') ? '' : 'none';
      el.querySelector('[name="amount"]')?.toggleAttribute('disabled', t !== 'AMOUNT');
    });
  };
  if (typeSelect) {
    typeSelect.addEventListener('change', toggleGroups);
    toggleGroups(); // Chạy 1 lần khi tải trang
  }
})();
</script>