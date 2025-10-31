<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<div class="main-staffs py-4">

  <h3 class="mb-3 gradient-text">Nhân viên cửa hàng</h3>

  <c:if test="${not empty sessionScope.flash}">
    <div class="alert alert-success glass-alert">${sessionScope.flash}</div>
    <c:remove var="flash" scope="session"/>
  </c:if>
  <c:if test="${not empty sessionScope.error}">
    <div class="alert alert-danger glass-alert">${sessionScope.error}</div>
    <c:remove var="error" scope="session"/>
  </c:if>

  <div class="card kpi-card mb-4">
    <div class="card-body">
      <form method="post" action="${pageContext.request.contextPath}/vendor/staffs/add" class="row g-2">
        <div class="col-md-6">
          <input type="email" name="email" class="form-control" placeholder="Nhập email user để thêm vào shop" required />
        </div>
        <div class="col-auto">
          <button class="btn btn-secondary">Add staff</button>
        </div>
      </form>
      <small class="text-muted">Chỉ chấp nhận tài khoản USER đã kích hoạt email. Mỗi user chỉ thuộc 1 shop.</small>
    </div>
  </div>

  <div class="card recent-orders-card">
    <div class="card-body">
      <div class="d-flex justify-content-between align-items-center mb-2">
        <div><strong>Shop:</strong> <c:out value="${shop.shopName}"/></div>
        <div class="text-muted">
          <span class="badge bg-secondary">Tổng: <c:out value="${fn:length(staffs)}"/></span>
        </div>
      </div>

      <div class="table-responsive">
        <table class="table table-hover align-middle">
          <thead>
          <tr>
            <th>#</th>
            <th>Họ tên</th>
            <th>Email</th>
            <th>Vai trò</th>
            <th class="text-end">Hành động</th>
          </tr>
          </thead>
          <tbody>
          <c:forEach var="u" items="${staffs}" varStatus="i">
            <tr>
              <td>${i.index + 1}</td>
              <td><c:out value="${u.fullname}"/></td>
              <td><c:out value="${u.email}"/></td>
              <td><span class="badge bg-info">USER</span></td>
              
              <td class="text-end product-actions">
                <form method="post" action="${pageContext.request.contextPath}/vendor/staffs/delete" class="d-inline"
                      onsubmit="return confirm('Xóa nhân viên này khỏi shop?');">
                  <input type="hidden" name="userId" value="${u.id}"/>
                  <button class="btn btn-sm btn-outline-danger">Delete</button>
                </form>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty staffs}">
            <tr><td colspan="5" class="text-center text-muted">Chưa có nhân viên.</td></tr>
          </c:if>
          </tbody>
        </table>
      </div>
    </div>
  </div>
  
</div>