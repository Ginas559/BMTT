<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<h1 class="h5 mb-3">Thông tin cá nhân</h1>

<c:if test="${not empty message}">
  <div class="alert alert-success">${message}</div>
</c:if>
<c:if test="${not empty error}">
  <div class="alert alert-danger">${error}</div>
</c:if>

<form class="card p-3" method="post" action="${ctx}/user/profile">
  <div class="mb-3">
    <label class="form-label fw-semibold">Họ</label>
    <input type="text" class="form-control" name="firstname"
           value="${user.firstname}" placeholder="Nhập họ...">
  </div>

  <div class="mb-3">
    <label class="form-label fw-semibold">Tên</label>
    <input type="text" class="form-control" name="lastname"
           value="${user.lastname}" placeholder="Nhập tên...">
  </div>

  <div class="mb-3">
    <label class="form-label fw-semibold">Số điện thoại</label>
    <input type="text" class="form-control" name="phone"
           value="${user.phone}" placeholder="Nhập số điện thoại...">
  </div>

  <div class="mb-3">
    <label class="form-label fw-semibold">Email</label>
    <input type="email" class="form-control" name="email"
           value="${user.email}" placeholder="Nhập email...">
  </div>

  <div class="text-end">
    <button class="btn btn-primary px-4" type="submit">Cập nhật</button>
  </div>
</form>

<div class="mt-3 d-flex flex-wrap gap-2">
  <a class="btn btn-outline-secondary" href="${ctx}/user/addresses">
    📦 Quản lý địa chỉ nhận hàng
  </a>

  <!-- 👥 Nếu user là nhân viên của một shop -->
  <c:if test="${not empty user.staffShop}">
    <a class="btn btn-outline-success" href="${ctx}/vendor/dashboard">
      👥 Vào shop của tôi
    </a>
  </c:if>
</div>

