<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<h1 class="h5 mb-3">
  <c:choose>
    <c:when test="${empty address}">Thêm địa chỉ mới</c:when>
    <c:otherwise>Chỉnh sửa địa chỉ</c:otherwise>
  </c:choose>
</h1>

<form class="card p-3" method="post"
      action="${ctx}/user/address/<c:out value='${empty address ? "new" : "edit"}'/>">

  <c:if test="${not empty address}">
    <input type="hidden" name="id" value="${address.addressId}">
  </c:if>

  <div class="mb-3">
    <label class="form-label fw-semibold">Tên người nhận</label>
    <input type="text" class="form-control" name="receiverName"
           value="${address.receiverName}" placeholder="Nhập họ tên người nhận..." required>
  </div>

  <div class="mb-3">
    <label class="form-label fw-semibold">Số điện thoại</label>
    <input type="text" class="form-control" name="phone"
           value="${address.phone}" placeholder="Nhập số điện thoại..." required>
  </div>

  <div class="mb-3">
    <label class="form-label fw-semibold">Địa chỉ chi tiết</label>
    <textarea class="form-control" name="addressDetail" rows="2"
              placeholder="Ví dụ: Số nhà, đường, phường/xã, quận/huyện, tỉnh/thành phố" required>${address.addressDetail}</textarea>
  </div>

  <div class="form-check mb-3">
    <input class="form-check-input" type="checkbox" name="isDefault" id="isDefault"
           <c:if test="${address.isDefault}">checked</c:if>>
    <label class="form-check-label" for="isDefault">Đặt làm địa chỉ mặc định</label>
  </div>

  <div class="text-end">
    <a href="${ctx}/user/addresses" class="btn btn-outline-secondary">← Quay lại</a>
    <button type="submit" class="btn btn-primary px-4">Lưu</button>
  </div>
</form>
