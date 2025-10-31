<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="main-reset-password py-4">

  <div class="login-card-wrapper">
    <div class="card kpi-card login-card">
      <div class="card-body">

        <h3 class="mt-4 mb-3 text-center gradient-text">Đặt lại mật khẩu</h3>

        <c:if test="${not empty error}">
          <div class="alert alert-danger glass-alert">${error}</div>
        </c:if>
        <c:if test="${not empty flash}">
          <div class="alert alert-info glass-alert">${flash}</div>
          <c:remove var="flash" scope="session"/>
        </c:if>

        <form method="post" action="${ctx}/reset-password">
          <input type="hidden" name="email" value="${param.email}"/>

          <div class="mb-3">
            <label class="form-label">Mật khẩu mới</label>
            <input type="password" name="password" class="form-control" minlength="6" required/>
          </div>
          <div class="mb-3">
            <label class="form-label">Xác nhận mật khẩu</label>
            <input type="password" name="confirm" class="form-control" minlength="6" required/>
          </div>

          <button class="btn btn-primary w-100">Cập nhật mật khẩu</button>
        </form>

        <div class="text-center mt-3">
          <a href="${ctx}/login">Quay lại đăng nhập</a>
        </div>
      </div>
    </div>
  </div>

</div>