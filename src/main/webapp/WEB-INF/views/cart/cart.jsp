<%-- filepath: src/main/webapp/WEB-INF/views/public/cart.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<div class="main-cart py-4">

<h1 class="h5 mb-3 gradient-text">Giỏ hàng của bạn</h1>

<c:if test="${not empty flash}">
  <div class="alert alert-success glass-alert">${flash}</div>
  <c:remove var="flash" scope="session" />
</c:if>
<c:if test="${not empty flash_error}">
  <div class="alert alert-danger glass-alert">${flash_error}</div>
  <c:remove var="flash_error" scope="session" />
</c:if>

<c:choose>
  <c:when test="${cart != null && not empty cart.cartItems}">
  
    <div class="card recent-orders-card">
    <div class="card-body">
    <div class="table-responsive">
      <table class="table align-middle table-hover">
        <thead>
          <tr>
            <th style="width:60px">#</th>
            <th>Sản phẩm</th>
            <th class="text-end" style="width:140px">Giá</th>
            <th class="text-center" style="width:180px">Số lượng</th>
            <th class="text-end" style="width:160px">Tổng</th>
            <th class="text-end" style="width:110px"></th>
          </tr>
        </thead>
        <tbody>
          <c:set var="grandTotal" value="0" />
          <c:forEach var="item" items="${cart.cartItems}" varStatus="st">
            <c:set var="lineTotal" value="${item.product.price * (empty item.quantity ? 1 : item.quantity)}" />
            <tr>
              <td>${st.index + 1}</td>

              <td class="js-product-name">
                <a href="${ctx}/product/${item.product.productId}">
                  <c:out value="${item.product.productName}" />
                </a>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${item.product.price}" type="currency" currencySymbol="₫" />
              </td>

              <td class="text-center">
                <form method="post" action="${ctx}/cart/update" class="d-inline-flex align-items-center gap-2 product-actions">
                  <input type="hidden" name="itemId" value="${item.cartItemId}" />
                  <input type="number"
                         class="form-control form-control-sm text-center"
                         name="quantity"
                         value="${empty item.quantity ? 1 : item.quantity}"
                         min="1"
                         style="width:80px"/>
                  <button class="btn btn-sm btn-outline-primary">Cập nhật</button>
                </form>
              </td>

              <td class="text-end">
                <fmt:formatNumber value="${lineTotal}" type="currency" currencySymbol="₫" />
              </td>

              <td class="text-end product-actions">
                <form method="post" action="${ctx}/cart/delete" class="d-inline js-delete-form">
                  <input type="hidden" name="itemId" value="${item.cartItemId}" />
                  <button type="button" class="btn btn-sm btn-outline-danger js-delete-btn">
                    Xoá
                  </button>
                </form>
              </td>
            </tr>
            <c:set var="grandTotal" value="${grandTotal + lineTotal}" />
          </c:forEach>
        </tbody>

        <tfoot>
          <tr>
            <th colspan="4" class="text-end">Tổng cộng:</th>
            <th class="text-end">
              <fmt:formatNumber value="${grandTotal}" type="currency" currencySymbol="₫" />
            </th>
            <th></th>
          </tr>
        </tfoot>
      </table>
    </div>
    </div>
    </div>
    <div class="text-end mt-3">
      <a href="${ctx}/checkout" class="btn btn-primary btn-lg">Tiến hành đặt hàng (COD)</a>
    </div>
  </c:when>

  <c:otherwise>
    <div class="card kpi-card text-center text-muted py-5">
      Giỏ hàng trống. <a href="${ctx}/products" class="fw-bold">Xem sản phẩm</a>
    </div>
  </c:otherwise>
</c:choose>

<div class="cart-modal-wrapper">
<div class="cm-overlay" id="confirmDeleteOverlay" role="dialog" aria-modal="true" aria-labelledby="cmTitle" aria-hidden="true">
  <div class="cm-modal" role="document">
    <div class="cm-header">🗑 Xác nhận xoá sản phẩm</div>
    <div class="cm-body">
      <div id="cmMessage">Bạn có chắc muốn xoá sản phẩm này khỏi giỏ hàng?</div>
    </div>
    <div class="cm-footer">
      <button type="button" class="cm-btn cm-btn-cancel" id="cmCancelBtn">Huỷ</button>
      <button type="button" class="cm-btn cm-btn-danger" id="cmConfirmBtn">Xoá</button>
    </div>
  </div>
</div>
</div>
</div> 
<script>
(function () {
  // (Giữ nguyên 100% script JS "xịn" của bro)
  var overlay = document.getElementById('confirmDeleteOverlay');
  var confirmBtn = document.getElementById('cmConfirmBtn');
  var cancelBtn  = document.getElementById('cmCancelBtn');
  var msgBox     = document.getElementById('cmMessage');
  var pendingForm = null;
  function openModal(message, form) {
    pendingForm = form;
    msgBox.innerHTML = message || "Bạn có chắc muốn xoá sản phẩm này khỏi giỏ hàng?";
    overlay.classList.add('show');
    overlay.setAttribute('aria-hidden', 'false');
    confirmBtn.focus();
  }
  function closeModal() {
    overlay.classList.remove('show');
    overlay.setAttribute('aria-hidden', 'true');
    pendingForm = null;
  }
  confirmBtn.addEventListener('click', function () {
    if (pendingForm) {
      pendingForm.submit();
    }
    closeModal();
  });
  cancelBtn.addEventListener('click', closeModal);
  overlay.addEventListener('click', function (e) {
    if (e.target === overlay) closeModal();
  });
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape' && overlay.classList.contains('show')) {
      e.preventDefault();
      closeModal();
    }
  });
  document.addEventListener('click', function (e) {
    var btn = e.target.closest('.js-delete-btn');
    if (!btn) return;
    var form = btn.closest('.js-delete-form');
    if (!form) return;
    var row = form.closest('tr');
    var nameCell = row ? row.querySelector('.js-product-name') : null;
    var nameText = nameCell ? nameCell.textContent.trim() : 'sản phẩm này';
    e.preventDefault();
    openModal('Bạn có chắc muốn xoá <strong>"' + nameText + '"</strong> khỏi giỏ hàng?', form);
  });
})();
</script>