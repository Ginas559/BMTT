<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"   uri="jakarta.tags.core"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<%@ taglib prefix="fn"  uri="jakarta.tags.functions"%>

<c:set var="ctx" value="${pageContext.request.contextPath}" />
<fmt:setLocale value="vi_VN" />

<h1 class="h5 mb-3">Đơn hàng của tôi</h1>

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

<!-- Bộ lọc trạng thái -->
<div class="mb-3">
  <ul class="nav nav-pills flex-wrap gap-2">
    <c:set var="st" value="${empty status ? 'ALL' : status}" />
    <li class="nav-item"><a class="nav-link ${st=='ALL'?'active':''}" href="${ctx}/orders?status=ALL">Tất cả</a></li>
    <li class="nav-item"><a class="nav-link ${st=='NEW'?'active':''}" href="${ctx}/orders?status=NEW">Mới</a></li>
    <li class="nav-item"><a class="nav-link ${st=='CONFIRMED'?'active':''}" href="${ctx}/orders?status=CONFIRMED">Đã xác nhận</a></li>
    <li class="nav-item"><a class="nav-link ${st=='SHIPPING'?'active':''}" href="${ctx}/orders?status=SHIPPING">Đang giao</a></li>
    <li class="nav-item"><a class="nav-link ${st=='DELIVERED'?'active':''}" href="${ctx}/orders?status=DELIVERED">Đã giao</a></li>
    <li class="nav-item"><a class="nav-link ${st=='CANCELED'?'active':''}" href="${ctx}/orders?status=CANCELED">Đã hủy</a></li>
    <li class="nav-item"><a class="nav-link ${st=='RETURNED'?'active':''}" href="${ctx}/orders?status=RETURNED">Hoàn hàng</a></li>
  </ul>
</div>

<c:choose>
  <c:when test="${not empty orders}">
    <div class="table-responsive">
      <table class="table align-middle">
        <thead>
          <tr>
            <th>#</th>
            <th>Ngày tạo</th>
            <th>Mã giao dịch</th>
            <th>Thanh toán</th>
            <th class="text-end">Tổng tiền</th>
            <th>Trạng thái</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="o" items="${orders}">
            <c:set var="txn" value="${paymentsByOrderId[o.orderId]}"/>

            <tr>
              <td>#${o.orderId}</td>

              <%-- Hiển thị ngày: thử tuần tự, cái nào thành công thì in và dừng --%>
              <td>
                <c:set var="printed" value="false"/>

                <%-- TH1: Thử format trực tiếp (nếu là java.util.Date/Timestamp sẽ ok) --%>
                <c:catch var="e1">
                  <fmt:formatDate value="${o.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                  <c:set var="printed" value="true"/>
                </c:catch>

                <%-- TH2: Parse chuỗi ISO có millis: yyyy-MM-dd'T'HH:mm:ss.SSS --%>
                <c:if test="${not printed}">
                  <c:catch var="e2">
                    <fmt:parseDate value="${o.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss.SSS" var="d1"/>
                    <fmt:formatDate value="${d1}" pattern="dd/MM/yyyy HH:mm" />
                    <c:set var="printed" value="true"/>
                  </c:catch>
                </c:if>

                <%-- TH3: Parse chuỗi ISO có giây: yyyy-MM-dd'T'HH:mm:ss --%>
                <c:if test="${not printed}">
                  <c:catch var="e3">
                    <fmt:parseDate value="${o.createdAt}" pattern="yyyy-MM-dd'T'HH:mm:ss" var="d2"/>
                    <fmt:formatDate value="${d2}" pattern="dd/MM/yyyy HH:mm" />
                    <c:set var="printed" value="true"/>
                  </c:catch>
                </c:if>

                <%-- TH4: Parse chuỗi ISO không có giây: yyyy-MM-dd'T'HH:mm --%>
                <c:if test="${not printed}">
                  <c:catch var="e4">
                    <fmt:parseDate value="${o.createdAt}" pattern="yyyy-MM-dd'T'HH:mm" var="d3"/>
                    <fmt:formatDate value="${d3}" pattern="dd/MM/yyyy HH:mm" />
                    <c:set var="printed" value="true"/>
                  </c:catch>
                </c:if>

                <%-- Fallback: in nguyên giá trị nếu mọi cách đều fail (ví dụ LocalDateTime không parse khớp) --%>
                <c:if test="${not printed}">
                  <span class="text-muted small"><c:out value="${o.createdAt}"/></span>
                </c:if>
              </td>

              <%-- Cột mã giao dịch --%>
              <td>
                <c:choose>
                  <c:when test="${not empty txn && groupFirst != null && groupFirst.contains(o.orderId)}">
                    <span class="fw-semibold">${txn}</span>
                  </c:when>
                  <c:otherwise>
                    <span class="text-muted small">—</span>
                  </c:otherwise>
                </c:choose>
              </td>

              <td><span class="badge text-bg-light"><c:out value="${o.paymentMethod}"/></span></td>

              <td class="text-end">
                <fmt:formatNumber value="${o.totalAmount}" type="currency" currencySymbol="₫"/>
              </td>

              <td>
                <c:choose>
                  <c:when test="${o.status=='NEW'}"><span class="badge text-bg-primary">Mới</span></c:when>
                  <c:when test="${o.status=='CONFIRMED'}"><span class="badge text-bg-info">Đã xác nhận</span></c:when>
                  <c:when test="${o.status=='SHIPPING'}"><span class="badge text-bg-warning">Đang giao</span></c:when>
                  <c:when test="${o.status=='DELIVERED'}"><span class="badge text-bg-success">Đã giao</span></c:when>
                  <c:when test="${o.status=='CANCELED'}"><span class="badge text-bg-secondary">Đã hủy</span></c:when>
                  <c:when test="${o.status=='RETURNED'}"><span class="badge text-bg-dark">Hoàn hàng</span></c:when>
                  <c:otherwise><span class="badge text-bg-light"><c:out value="${o.status}"/></span></c:otherwise>
                </c:choose>
              </td>

              <td class="text-end">
                <a class="btn btn-sm btn-outline-primary" href="${ctx}/order/${o.orderId}">Xem chi tiết</a>
              </td>
            </tr>
          </c:forEach>
        </tbody>
      </table>
    </div>
  </c:when>

  <c:otherwise>
    <div class="text-center text-muted py-5">Chưa có đơn nào.</div>
  </c:otherwise>
</c:choose>

<div class="mt-3">
  <a href="${ctx}/products" class="btn btn-outline-secondary">← Tiếp tục mua sắm</a>
</div>
