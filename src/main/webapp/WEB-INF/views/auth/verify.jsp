<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="main-verify py-4">

  <div class="login-card-wrapper">
    <div class="card kpi-card login-card">
      <div class="card-body">

        <c:if test="${not empty flash}">
          <div class="alert alert-info glass-alert">${flash}</div>
          <c:remove var="flash" scope="session"/>
        </c:if>

        <h2 class="mb-3 text-center gradient-text">
          <c:choose>
            <c:when test="${param.purpose == 'reset'}">Xác minh OTP đặt lại mật khẩu</c:when>
            <c:otherwise>Xác minh OTP kích hoạt tài khoản</c:otherwise>
          </c:choose>
        </h2>

        <form method="post" action="${ctx}/verify" class="mb-3">
          <input type="hidden" name="email" value="${param.email}"/>
          <input type="hidden" name="purpose" value="${empty param.purpose ? 'register' : param.purpose}"/>
          <div class="mb-3">
            <label class="form-label">Mã OTP</label>
            <input type="text" name="code" class="form-control" maxlength="6" required/>
          </div>
          <button class="btn btn-primary w-100">Xác minh</button>
        </form>

        <div class="d-flex justify-content-between align-items-center">
            <form method="post" action="${ctx}/otp/resend">
              <input type="hidden" name="email" value="${param.email}"/>
              <input type="hidden" name="purpose" value="${empty param.purpose ? 'register' : param.purpose}"/>
              <button class="btn btn-outline-secondary btn-sm">Gửi lại OTP</button>
            </form>
    
            <a href="${ctx}/login">Quay lại đăng nhập</a>
        </div>
        
      </div>
    </div>
  </div>

</div>