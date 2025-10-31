<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!doctype html>
<html>
<head>
    <title>Đăng ký tài khoản</title>
    <meta charset="utf-8"/>
    
    <link
		href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
		rel="stylesheet">
	
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}/assets/css/web.css">
    
    <script>
      function onRoleChange() {
        const role = document.getElementById("role").value;
        const secret = document.getElementById("secret");
        if (role === "USER") {
          secret.value = "";
          secret.disabled = true;
        } else {
          secret.disabled = false;
        }
      }
      window.addEventListener('DOMContentLoaded', onRoleChange);
    </script>
</head>
<body>
<div class="main-register py-4">

  <div class="login-card-wrapper">
    <div class="card kpi-card login-card">
      <div class="card-body">

        <h3 class="mb-3 text-center gradient-text">Đăng ký tài khoản</h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger glass-alert">${error}</div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert alert-success glass-alert">${message}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/register">
            <div class="mb-3">
                <label class="form-label">Họ</label>
                <input type="text" name="firstname" class="form-control" maxlength="32" required/>
            </div>
            <div class="mb-3">
                <label class="form-label">Tên</label>
                <input type="text" name="lastname" class="form-control" maxlength="32" required/>
            </div>
            <div class="mb-3">
                <label class="form-label">Email</label>
                <input type="email" name="email" class="form-control" maxlength="128" required/>
            </div>
            <div class="mb-3">
                <label class="form-label">Số điện thoại</label>
                <input type="text" name="phone" class="form-control" maxlength="20" required/>
            </div>

            <div class="mb-3">
                <label class="form-label">Vai trò</label>
                <select id="role" name="role" class="form-select" onchange="onRoleChange()">
                    <option value="USER">User</option>
                    <option value="VENDOR">Vendor</option>
                    <option value="ADMIN">Admin</option>
                    <option value="SHIPPER">Shipper</option>
                </select>
                <div class="form-text">Vendor/Admin/Shipper phải nhập mã bí mật</div>
            </div>

            <div class="mb-3">
                <label class="form-label">Mã bí mật</label>
                <input type="password" id="secret" name="secret" class="form-control" placeholder="Chỉ dành cho Vendor/Admin/Shipper" disabled/>
            </div>

            <div class="mb-3">
                <label class="form-label">Mật khẩu</label>
                <input type="password" name="password" class="form-control" minlength="6" required/>
            </div>
            <div class="mb-3">
                <label class="form-label">Xác nhận mật khẩu</label>
                <input type="password" name="confirm" class="form-control" minlength="6" required/>
            </div>
            
            <button class="btn btn-primary w-100" type="submit">Đăng ký</button>
        </form>

        <div class="text-center mt-3">
            Đã có tài khoản? <a href="${ctx}/login">Đăng nhập</a>
        </div>
        
      </div>
    </div>
  </div>

</div>

<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>