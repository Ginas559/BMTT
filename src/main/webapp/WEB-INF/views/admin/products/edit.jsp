<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Chỉnh sửa sản phẩm</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 800px; /* Tăng chiều rộng để chứa nhiều trường hơn */
	margin: 40px auto;
	border-radius: 15px;
}

.card h3 {
	color: #0d6efd; /* Đổi màu chủ đạo thành primary */
}

img#preview {
	max-width: 200px;
	max-height: 200px;
	object-fit: cover;
	border-radius: 10px;
	box-shadow: 0 2px 6px rgba(0, 0, 0, 0.1);
}
</style>
</head>
<body>
	<div class="container">
		<div class="card shadow-lg p-4">
			<h3 class="text-center mb-4">Chỉnh sửa Sản phẩm</h3>

			<form
				action="${pageContext.request.contextPath}/admin/products/edit"
				method="post" enctype="multipart/form-data">
				
				<input type="hidden" name="productId"
					value="${product.productId}">

				<div class="row">
					<div class="col-md-6">
						<div class="mb-3">
							<label for="productName" class="form-label">Tên sản phẩm:</label>
							<input type="text" class="form-control" id="productName"
								name="productName" value="${product.productName}" required>
						</div>

						<div class="mb-3">
							<label for="price" class="form-label">Giá gốc (VNĐ):</label>
							<input type="number" class="form-control" id="price"
								name="price" value="${product.price}" min="0" step="0.01" required>
						</div>

						<div class="mb-3">
							<label for="discountPrice" class="form-label">Giá khuyến mãi (Nếu có):</label>
							<input type="number" class="form-control" id="discountPrice"
								name="discountPrice" value="${product.discountPrice}" min="0" step="0.01">
						</div>
						
						<div class="mb-3">
							<label for="stock" class="form-label">Số lượng tồn kho:</label>
							<input type="number" class="form-control" id="stock"
								name="stock" value="${product.stock}" min="0" required>
						</div>
					</div>

					<div class="col-md-6">
						
						<div class="mb-3">
							<label for="categoryId" class="form-label">Danh mục:</label>
							<select class="form-select" id="categoryId" name="categoryId" required>
								<option value="">Chọn danh mục</option>
								<c:forEach var="cat" items="${categories}">
									<option value="${cat.categoryId}"
										${product.category.categoryId == cat.categoryId ? 'selected' : ''}>
										${cat.categoryName}
									</option>
								</c:forEach>
							</select>
						</div>
						
						<div class="mb-3">
							<label for="shopId" class="form-label">Shop:</label>
							<select class="form-select" id="shopId" name="shopId" required>
								<option value="">Chọn shop</option>
								<c:forEach var="s" items="${shops}">
									<option value="${s.shopId}"
										${product.shop.shopId == s.shopId ? 'selected' : ''}>
										${s.shopName}
									</option>
								</c:forEach>
							</select>
						</div>
					</div>
				</div>

				<div class="mb-3">
					<label for="description" class="form-label">Mô tả chi tiết:</label>
					<textarea class="form-control" id="description" name="description"
						rows="5">${product.description}</textarea>
				</div>
				
				<div class="mb-4 form-check">
					<input type="checkbox" class="form-check-input" id="isBanned"
						name="isBanned" <c:if test="${product.isBanned}">checked</c:if>>
					<label class="form-check-label" for="isBanned">Cấm sản phẩm (Bị khóa hiển thị)</label>
				</div>

				<div class="text-center">
					<button type="submit" class="btn btn-primary px-5 me-2">Cập
						nhật Sản phẩm</button>
					<a href="${pageContext.request.contextPath}/admin/products"
						class="btn btn-secondary px-5">Quay lại danh sách</a>
				</div>
			</form>
		</div>
	</div>


	<script
		src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>