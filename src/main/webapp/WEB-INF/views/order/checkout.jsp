<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<h1 class="h5 mb-3">Xác nhận thanh toán VNPAY</h1>

<!-- Thông báo -->
<c:if test="${not empty flash}">
	<div class="alert alert-success">${flash}</div>
	<c:remove var="flash" scope="session" />
</c:if>
<c:if test="${not empty flash_error}">
	<div class="alert alert-danger">${flash_error}</div>
	<c:remove var="flash_error" scope="session" />
</c:if>

<c:choose>
	<c:when test="${cart != null && not empty cart.cartItems}">
		<div class="row g-3">
			<!-- Địa chỉ giao hàng -->
			<div class="col-12 col-lg-5">
				<div class="card h-100">
					<div class="card-header fw-semibold">Địa chỉ giao hàng</div>
					<div class="card-body">
						<c:choose>
							<c:when test="${not empty addresses}">
								<div class="vstack gap-2">
									<c:forEach var="a" items="${addresses}" varStatus="st">
										<label
											class="border rounded p-2 d-flex gap-2 align-items-start">
											<input class="form-check-input mt-1" type="radio"
											name="addressId" value="${a.addressId}"
											${a.isDefault || st.first ? 'checked' : ''} /> <span>
												<div class="fw-semibold">${a.receiverName}• ${a.phone}</div>
												<div class="text-muted small">${a.addressDetail}</div> <c:if
													test="${a.isDefault}">
													<span class="badge bg-success mt-1">Mặc định</span>
												</c:if>
										</span>
										</label>
									</c:forEach>
								</div>
							</c:when>
							<c:otherwise>
								<div class="text-muted">Bạn chưa có địa chỉ giao hàng.</div>
								<a class="btn btn-outline-primary mt-2"
									href="${ctx}/user/address/new">➕ Thêm địa chỉ</a>
							</c:otherwise>
						</c:choose>
					</div>
				</div>
			</div>

			<!-- Tóm tắt đơn hàng -->
			<div class="col-12 col-lg-7">
				<div class="card">
					<div class="card-header fw-semibold">Đơn hàng của bạn</div>
					<div class="card-body">
						<div class="table-responsive">
							<table class="table align-middle">
								<thead>
									<tr>
										<th>Sản phẩm</th>
										<th class="text-end">Giá</th>
										<th class="text-center">SL</th>
										<th class="text-end">Tạm tính</th>
									</tr>
								</thead>
								<tbody>
									<c:set var="total" value="0" />
									<c:forEach var="ci" items="${cart.cartItems}">
										<tr>
											<td><a href="${ctx}/product/${ci.product.productId}"
												class="text-decoration-none"> <c:out
														value="${ci.product.productName}" />
											</a></td>
											<td class="text-end"><fmt:formatNumber
													value="${ci.product.price}" type="currency"
													currencySymbol="₫" /></td>
											<td class="text-center">${ci.quantity}</td>
											<td class="text-end"><fmt:formatNumber
													value="${ci.product.price * ci.quantity}" type="currency"
													currencySymbol="₫" /></td>
										</tr>
										<c:set var="total"
											value="${total + (ci.product.price * ci.quantity)}" />
									</c:forEach>
								</tbody>
								<tfoot>
									<!-- Giảm giá nếu có -->
									<c:if
										test="${not empty sessionScope.voucherDiscount && sessionScope.voucherDiscount > 0}">
										<tr>
											<th colspan="3" class="text-end text-success">Giảm giá:</th>
											<th class="text-end text-success">−<fmt:formatNumber
													value="${sessionScope.voucherDiscount}" type="currency"
													currencySymbol="₫" />
											</th>
										</tr>
									</c:if>
									<tr>
										<th colspan="3" class="text-end">Tổng cộng:</th>
										<th class="text-end"><fmt:formatNumber
												value="${total - (sessionScope.voucherDiscount != null ? sessionScope.voucherDiscount : 0)}"
												type="currency" currencySymbol="₫" /></th>
									</tr>
								</tfoot>
							</table>
						</div>

						<!-- Nhập mã giảm giá -->
						<div class="mt-3">
							<label class="form-label fw-semibold">Mã giảm giá
								(Voucher)</label>
							<div class="input-group">
								<input type="text" id="voucherCode" class="form-control"
									placeholder="Nhập mã giảm giá..." />
								<!-- NOTE: thêm data-apply-url + onclick để chắc chắn gọi được hàm -->
								<button type="button" id="applyVoucherBtn"
									class="btn btn-outline-success"
									data-apply-url="<c:url value='/api/voucher/apply'/>"
									onclick="return window.applyVoucher && window.applyVoucher();">
									Áp dụng</button>
							</div>
							<div id="voucherMessage" class="small mt-2"></div>
						</div>


						<!-- Hiển thị mã đang dùng -->
						<c:if test="${not empty sessionScope.voucherCode}">
							<div class="alert alert-success mt-2 py-2 mb-0">
								✅ Đã áp dụng mã: <strong>${sessionScope.voucherCode}</strong>
							</div>
						</c:if>

						<div class="d-flex justify-content-between mt-3">
							<a href="${ctx}/cart" class="btn btn-outline-secondary">←
								Quay lại giỏ hàng</a>
						</div>

						<!-- Thanh toán VNPAY -->
						<c:if test="${not empty addresses}">
							<hr class="my-4" />
							<div class="vstack gap-2">
								<div class="d-flex align-items-center gap-2">
									<span class="fw-semibold">Thanh toán qua</span> <img
										src="https://sandbox.vnpayment.vn/paymentv2/images/icons/logo.svg"
										alt="VNPAY" style="height: 22px" />
								</div>

								<div class="row g-2">
									<div class="col-12 col-md-6">
										<label class="form-label small text-muted">Ngân hàng
											(tùy chọn)</label> <select id="vnpBankCode" class="form-select">
											<option value="">— Chọn ngân hàng —</option>
											<option value="NCB">NCB (test)</option>
											<option value="VNPAYQR">VNPAYQR</option>
											<option value="VNBANK">VNBANK</option>
											<option value="INTCARD">Thẻ quốc tế</option>
										</select>
									</div>
								</div>

								<div>
									<button id="payVnpayBtn" class="btn btn-primary">
										Thanh toán qua VNPAY</button>
									<small class="text-muted ms-2"> Sẽ chuyển hướng sang
										cổng thanh toán VNPAY. </small>
								</div>

								<!-- ✅ Tổng tiền đã trừ giảm giá -->
								<input type="hidden" id="totalAmountHidden"
									value="${total - (sessionScope.voucherDiscount != null ? sessionScope.voucherDiscount : 0)}" />
							</div>
						</c:if>
					</div>
				</div>
			</div>
		</div>
	</c:when>

	<c:otherwise>
		<div class="text-center text-muted py-5">
			Giỏ hàng trống. <a href="${ctx}/products">Xem sản phẩm</a>
		</div>
	</c:otherwise>
</c:choose>

<!-- ====================== -->
<!-- ✅ JS Áp dụng voucher + Thanh toán -->
<!-- ====================== -->
<script>
(function() {
  // Lấy context-path do JSP render
  const ctx = '${ctx}';

  // ====== ÁP DỤNG VOUCHER ======
  window.applyVoucher = function() {
    const btn = document.getElementById('applyVoucherBtn');
    const codeInput = document.getElementById('voucherCode');
    const msgBox = document.getElementById('voucherMessage');

    if (!btn || !codeInput || !msgBox) return false;

    const code = (codeInput.value || '').trim();
    if (!code) {
      msgBox.textContent = '⚠️ Vui lòng nhập mã giảm giá.';
      msgBox.className = 'small mt-2 text-warning';
      return false;
    }

    const applyUrl = btn.getAttribute('data-apply-url') || (ctx + '/api/voucher/apply');

    msgBox.textContent = 'Đang kiểm tra mã...';
    msgBox.className = 'small mt-2 text-muted';

    fetch(applyUrl, {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({ code })
    })
    .then(async res => {
      const text = await res.text();
      try { return JSON.parse(text); }
      catch (e) {
        console.error('Voucher response (not JSON):', text);
        throw new Error('Phản hồi không hợp lệ từ server');
      }
    })
    .then(data => {
      console.log('Voucher API response:', data);
      if (data.success) {
        msgBox.textContent = data.message || '✅ Áp dụng thành công!';
        msgBox.className = 'small mt-2 text-success';
        // reload để bảng tổng tiền cập nhật
        setTimeout(() => location.reload(), 600);
      } else {
        msgBox.textContent = '❌ ' + (data.message || 'Mã không hợp lệ hoặc hết hạn.');
        msgBox.className = 'small mt-2 text-danger';
      }
    })
    .catch(err => {
      console.error('Apply voucher error:', err);
      msgBox.textContent = '❌ Lỗi: ' + err.message;
      msgBox.className = 'small mt-2 text-danger';
    });

    return false;
  };

  // ====== VNPAY ======
  const payBtn = document.getElementById('payVnpayBtn');
  if (payBtn) {
    payBtn.addEventListener('click', function (e) {
      e.preventDefault();

      const addrChecked = document.querySelector('input[name="addressId"]:checked');
      if (!addrChecked) { alert('⚠️ Vui lòng chọn địa chỉ giao hàng trước.'); return; }

      let amountRaw = document.getElementById('totalAmountHidden')?.value || '0';
      let amount = Math.round(Number(amountRaw));
      if (!Number.isFinite(amount) || amount <= 0) { alert('⚠️ Tổng tiền không hợp lệ.'); return; }

      const addressId = addrChecked.value;
      const bankCode  = document.getElementById('vnpBankCode')?.value || '';
      const payUrl = '<c:url value="/api/payment/create"/>';

      fetch(payUrl, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: new URLSearchParams({ amount: String(amount), bankCode, addressId }).toString()
      })
      .then(res => res.json())
      .then(data => {
        if (data && data.success && data.paymentUrl) {
          window.location.href = data.paymentUrl;
        } else {
          alert('❌ ' + (data?.message || 'Không tạo được liên kết thanh toán.'));
        }
      })
      .catch(err => {
        console.error(err);
        alert('❌ Lỗi kết nối. Vui lòng thử lại.');
      });
    });
  }
})();
</script>
