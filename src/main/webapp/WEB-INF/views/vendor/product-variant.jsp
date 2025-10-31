<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="main-variants py-4">

  <div class="mb-4 d-flex flex-wrap gap-3">
    <a href="${ctx}/vendor/products" class="btn btn-primary">🛍️ Quản lý sản phẩm</a>
    <a href="${ctx}/vendor/orders" class="btn btn-success">📦 Đơn hàng</a>
    <c:if test="${not empty shop and not empty shop.shopId}">
      <a href="${ctx}/chat?shopId=${shop.shopId}" class="btn btn-chat">💬 Chat nội bộ</a>
      <a href="${ctx}/chat/public?shopId=${shop.shopId}" class="btn btn-chat">💬 Chat công khai</a>
    </c:if>
  </div>

  <h2 class="mb-3 gradient-text">Biến thể sản phẩm: ${product.productName}</h2>

  <div class="card kpi-card mb-4">
    <div class="card-body">
      <h5 class="card-title">Thêm biến thể</h5>
      <form method="post" enctype="multipart/form-data" class="row g-3 mt-2">
        <input type="hidden" name="productId" value="${product.productId}"/>
        <div class="col-md-2">
          <input type="text" name="size" class="form-control" placeholder="Size" required>
        </div>
        <div class="col-md-3">
          <input type="text" name="color" class="form-control" placeholder="Màu sắc" required>
        </div>
        <div class="col-md-2">
          <input type="number" name="stock" class="form-control" placeholder="Tồn kho">
        </div>

        <div class="col-md-3">
          <input type="file" name="variantImage" accept=".jpg,.jpeg,.png,.webp,image/*" class="form-control"/>
          <small class="text-muted">Chọn 1 ảnh (≤ 2MB) — ảnh thumbnail của biến thể.</small>
        </div>

        <div class="col-md-2">
          <button class="btn btn-primary w-100">Thêm</button>
        </div>
      </form>
    </div>
  </div>


  <div class="card recent-orders-card">
    <div class="card-body">
      <h5 class="card-title">Danh sách biến thể</h5>
      <table class="table align-middle table-hover">
        <thead>
        <tr>
          <th>#</th>
          <th>Size</th>
          <th>Màu</th>
          <th>Tồn kho</th>
          <th>Ảnh</th>
          <th>Thao tác</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="v" items="${variants}">
          <tr>
            <td>${v.variantId}</td>
            <td>${v.size}</td>
            <td>${v.color}</td>
            <td>${v.stock}</td>
            <td>
              <c:choose>
                <c:when test="${not empty v.imageUrl}">
                  <img src="${v.imageUrl}" alt="ảnh" width="70" class="rounded shadow-sm"/>
                </c:when>
                <c:otherwise>
                  <span class="text-muted">Không có ảnh</span>
                </c:otherwise>
              </c:choose>
            </td>
            
            <td class="product-actions">
              <a href="${ctx}/vendor/product/variant/delete?id=${v.variantId}&productId=${product.productId}"
                 class="btn btn-sm btn-outline-danger">Xóa</a>
            </td>
          </tr>
        </c:forEach>
        </tbody>
      </table>
    </div>
  </div>
  
</div>