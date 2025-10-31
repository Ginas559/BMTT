<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<!doctype html>
<html>
<head>
    <title>Xác thực OTP</title>
    <meta charset="utf-8"/>
    
    <link
		href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
		rel="stylesheet">
	
	<link rel="stylesheet"
		href="${pageContext.request.contextPath}/assets/css/web.css">
</head>
<body>
<div class="main-verify-otp py-4">

  <div class="login-card-wrapper">
    <div class="card kpi-card login-card">
      <div class="card-body">

        <h3 class="mb-3 text-center gradient-text">Nhập mã OTP</h3>

        <c:if test="${not empty error}">
            <div class="alert alert-danger glass-alert">${error}</div>
        </c:if>
        <c:if test="${not empty message}">
            <div class="alert alert-success glass-alert">${message}</div>
        </c:if>

        <form method="post" action="${pageContext.request.contextPath}/verify-otp">
            <input type="hidden" name="purpose" value="${param.purpose != null ? param.purpose : 'ACTIVATE'}"/>
            <div class="mb-3">
                <label class="form-label">Mã OTP (6 số)</label>
                <input type="text" name="code" class="form-control" maxlength="6" required/>
            </div>
            
            <button class="btn btn-primary w-100" type="submit">Xác nhận</button>
        </form>

        <div class="text-center mt-3">
            <a href="${ctx}/login">Quay lại đăng nhập</a>
        </div>
        
      </div>
    </div>
  </div>

</div>

<script
	src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
    
</body>
</html>