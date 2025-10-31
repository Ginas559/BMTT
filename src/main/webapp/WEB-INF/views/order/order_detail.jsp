<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<h1 class="h5 mb-3">Chi tiết đơn hàng</h1>

<c:if test="${not empty error}">
  <div class="alert alert-danger">${error}</div>
</c:if>
<c:if test="${not empty flash}">
  <div class="alert alert-success">${flash}</div>
  <c:remove var="flash" scope="session"/>
</c:if>
<c:if test="${not empty flash_error}">
  <div class="alert alert-danger">${flash_error}</div>
  <c:remove var="flash_error" scope="session"/>
</c:if>

<!-- Thông báo sau khi hủy -->
<c:choose>
  <c:when test="${param.msg == 'cancel_success'}">
    <div class="alert alert-success">Đã hủy đơn hàng thành công.</div>
  </c:when>
  <c:when test="${param.msg == 'not_allowed'}">
    <div class="alert alert-danger">Bạn không có quyền thực hiện thao tác này hoặc đơn không tồn tại.</div>
  </c:when>
  <c:when test="${fn:startsWith(param.msg, 'cannot_cancel_in_status_')}">
    <div class="alert alert-warning">Trạng thái hiện tại không cho phép hủy: <strong>${order.status}</strong>.</div>
  </c:when>
  <c:when test="${param.msg == 'cancel_failed'}">
    <div class="alert alert-danger">Có lỗi xảy ra khi hủy đơn. Vui lòng thử lại.</div>
  </c:when>
</c:choose>

<c:choose>
  <c:when test="${not empty order}">
    <!-- Thông tin tổng quan -->
    <div class="card mb-3">
      <div class="card-body d-flex flex-wrap gap-4">
        <div>
          <div class="text-muted small">Mã đơn</div>
          <div class="fw-semibold">#${order.orderId}</div>
        </div>
        <div>
          <div class="text-muted small">Ngày tạo</div>
          <div>
            <c:out value="${order.createdAt != null ? order.createdAt.toString().replace('T', ' ') : '-'}"/>
          </div>
        </div>
        <div>
          <div class="text-muted small">Thanh toán</div>
          <span class="badge text-bg-light"><c:out value="${order.paymentMethod}"/></span>
        </div>
        <div>
          <div class="text-muted small">Trạng thái</div>
          <c:choose>
            <c:when test="${order.status=='NEW'}"><span class="badge text-bg-primary">Mới</span></c:when>
            <c:when test="${order.status=='CONFIRMED'}"><span class="badge text-bg-info">Đã xác nhận</span></c:when>
            <c:when test="${order.status=='SHIPPING'}"><span class="badge text-bg-warning">Đang giao</span></c:when>
            <c:when test="${order.status=='DELIVERED'}"><span class="badge text-bg-success">Đã giao</span></c:when>
            <c:when test="${order.status=='CANCELED'}"><span class="badge text-bg-secondary">Đã hủy</span></c:when>
            <c:when test="${order.status=='RETURNED'}"><span class="badge text-bg-dark">Hoàn hàng</span></c:when>
            <c:otherwise><span class="badge text-bg-light"><c:out value="${order.status}"/></span></c:otherwise>
          </c:choose>
        </div>
        <div class="ms-auto">
          <div class="text-muted small text-end">Tổng tiền</div>
          <div class="fw-bold fs-5 text-end">
            <fmt:formatNumber value="${order.totalAmount}" type="currency" currencySymbol="₫"/>
          </div>
        </div>
      </div>
    </div>

    <!-- Nút mở modal xác nhận hủy -->
    <c:if test="${order.status=='NEW' || order.status=='CONFIRMED'}">
      <div class="text-end mb-3">
        <button type="button" class="btn btn-outline-danger" data-bs-toggle="modal" data-bs-target="#cancelModal">
          Hủy đơn hàng
        </button>
      </div>

      <!-- ✅ Modal Bootstrap -->
      <div class="modal fade" id="cancelModal" tabindex="-1" aria-labelledby="cancelModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
          <div class="modal-content">
            <div class="modal-header bg-danger text-white">
              <h5 class="modal-title" id="cancelModalLabel">Xác nhận hủy đơn hàng</h5>
              <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body">
              Bạn có chắc chắn muốn hủy đơn hàng <strong>#${order.orderId}</strong> không?<br>
              Hành động này không thể hoàn tác.
            </div>
            <div class="modal-footer">
              <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Đóng</button>
              <form method="post" action="${ctx}/order/${order.orderId}">
                <input type="hidden" name="action" value="cancel"/>
                <button type="submit" class="btn btn-danger">Xác nhận hủy</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </c:if>

    <!-- Thông tin cửa hàng -->
    <div class="card mb-3">
      <div class="card-header fw-semibold">Thông tin cửa hàng</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${order.shop != null}">
            <div><strong>Tên cửa hàng:</strong> <c:out value="${order.shop.shopName}"/></div>
            <div>
              <strong>Email liên hệ:</strong>
              <c:choose>
                <c:when test="${not empty order.shop.vendor}">
                  <c:out value="${order.shop.vendor.email}"/>
                  <c:if test="${not empty order.shop.vendor.firstname || not empty order.shop.vendor.lastname}">
                    (<c:out value="${order.shop.vendor.firstname}"/> <c:out value="${order.shop.vendor.lastname}"/>)
                  </c:if>
                </c:when>
                <c:otherwise><span class="text-muted">Không có thông tin người quản lý</span></c:otherwise>
              </c:choose>
            </div>
          </c:when>
          <c:otherwise><span class="text-muted">Không có thông tin cửa hàng.</span></c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- Địa chỉ giao hàng -->
    <div class="card mb-3">
      <div class="card-header fw-semibold">Địa chỉ giao hàng</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${order.address != null}">
            <div class="fw-semibold"><c:out value="${order.address.receiverName}"/> • <c:out value="${order.address.phone}"/></div>
            <div class="text-muted"><c:out value="${order.address.addressDetail}"/></div>
          </c:when>
          <c:otherwise><span class="text-muted">Không có địa chỉ (dữ liệu thiếu).</span></c:otherwise>
        </c:choose>
      </div>
    </div>

    <!-- Danh sách sản phẩm -->
    <div class="card mb-3">
      <div class="card-header fw-semibold">Sản phẩm trong đơn</div>
      <div class="card-body">
        <c:choose>
          <c:when test="${not empty items}">
            <div class="table-responsive">
              <table class="table align-middle">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th class="text-end">Giá</th>
                    <th class="text-center">SL</th>
                    <th class="text-end">Giảm (%)</th>
                    <th class="text-end">Thành tiền</th>
                  </tr>
                </thead>
                <tbody>
                  <c:forEach var="it" items="${items}">
                    <tr>
                      <td>
                        <a class="text-decoration-none" href="${ctx}/product/${it.product.productId}">
                          <c:out value="${it.product.productName}"/>
                        </a>
                        <!-- Nút Đánh giá khi đơn đã giao/hoàn -->
                        <c:if test="${order.status=='DELIVERED' || order.status=='RETURNED'}">
                          <div class="mt-1">
                            <a class="btn btn-sm btn-primary"
                               href="${ctx}/product/${it.product.productId}?from=order#reviews">
                              Đánh giá sản phẩm
                            </a>
                          </div>
                        </c:if>
                      </td>
                      <td class="text-end">
                        <fmt:formatNumber value="${it.price}" type="currency" currencySymbol="₫"/>
                      </td>
                      <td class="text-center">${it.quantity}</td>
                      <td class="text-end"><c:out value="${it.discount}"/></td>
                      <td class="text-end">
                        <fmt:formatNumber value="${it.subtotal}" type="currency" currencySymbol="₫"/>
                      </td>
                    </tr>
                  </c:forEach>
                </tbody>
              </table>
            </div>
          </c:when>
          <c:otherwise><div class="text-muted">Đơn hàng chưa có dòng sản phẩm.</div></c:otherwise>
        </c:choose>
      </div>
    </div>

    <div class="d-flex justify-content-between">
      <a href="${ctx}/orders" class="btn btn-outline-secondary">← Quay lại danh sách</a>
      <a href="${ctx}/products" class="btn btn-primary">Tiếp tục mua sắm</a>
    </div>
  </c:when>
  <c:otherwise>
    <div class="text-center text-muted py-5">Không tìm thấy đơn hàng.</div>
    <div class="text-center">
      <a href="${ctx}/orders" class="btn btn-outline-secondary mt-2">← Quay lại danh sách</a>
    </div>
  </c:otherwise>
</c:choose>
