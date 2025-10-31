<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Chỉnh Sửa Khuyến Mãi</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 600px;
	margin: 40px auto;
	border-radius: 12px;
}

h3 {
	font-weight: 600;
}

label {
	font-weight: 500;
}
</style>
</head>
<body>
	<div class="container">
		<div class="card shadow-sm">
			<div class="card-body">
				<h3 class="text-center text-primary mb-4">
					<i class="fas fa-edit me-2"></i>Chỉnh Sửa Khuyến Mãi
				</h3>

				<%-- Giả định Promotion object được truyền vào là "promotion" --%>

				<form
					action="${pageContext.request.contextPath}/admin/promotions/edit"
					method="post">

					<%-- Trường ẩn để gửi ID của Promotion --%>
					<input type="hidden" name="promotionId"
						value="${promotion.promotionId}">

					<%-- Trường Chọn Shop (Shop) --%>
					<div class="mb-3">
						<label for="shopId" class="form-label">Tạo bởi Shop:</label> <select
							class="form-select" id="shopId" name="shopId" required>
							<option value="">-- Chọn Shop --</option>
							<c:forEach var="shop" items="${shops}">
								<option value="${shop.shopId}"
									${promotion.shop.shopId == shop.shopId ? 'selected' : ''}>
									${shop.shopName}</option>
							</c:forEach>
						</select>
					</div>

					<%-- Trường Tiêu đề (Title) --%>
					<div class="mb-3">
						<label for="title" class="form-label">Tiêu đề khuyến mãi:</label>
						<input type="text" class="form-control" id="title" name="title"
							placeholder="VD: Khuyến mãi mùa hè giảm 15%"
							value="${promotion.title}" required>
					</div>

					<%-- Trường Phần trăm giảm (Discount Percent) --%>
					<div class="mb-3">
						<label for="discountPercent" class="form-label">Phần trăm
							giảm (%):</label> <input type="number" step="0.01" min="0" max="100"
							class="form-control" id="discountPercent" name="discountPercent"
							placeholder="Nhập phần trăm giảm (VD: 15.5)"
							value="${promotion.discountPercent}" required>
					</div>

					<%-- Trường Phạm vi áp dụng (Apply To) --%>
					<div class="mb-3">
						<label for="applyTo" class="form-label">Phạm vi áp dụng:</label> <select
							class="form-select" id="applyTo" name="applyTo" required>
							<option value="PRODUCT"
								${promotion.applyTo == 'PRODUCT' ? 'selected' : ''}>Giảm
								giá Sản phẩm</option>
							<option value="SHIPPING"
								${promotion.applyTo == 'SHIPPING' ? 'selected' : ''}>Giảm
								phí Vận chuyển</option>
						</select>
					</div>
					
					<%-- 5. Trường Status (Trạng thái ban đầu) --%>
                    <div class="mb-3">
						<label for="status" class="form-label">Trạng thái ban đầu:</label>
						<select class="form-select" id="status" name="status" required>
                            <option value="ACTIVE" ${promotion.status == 'ACTIVE' ? 'selected' : ''}>ACTIVE (Hoạt động)</option>
                            <option value="INACTIVE" ${promotion.status == 'INACTIVE' ? 'selected' : ''}>INACTIVE (Không hoạt động)</option>
                        </select>
					</div>
					
					<%-- Trường Ngày bắt đầu (Start Date) và Ngày kết thúc (End Date) --%>
					<%-- Trường Ngày bắt đầu (Start Date) và Ngày kết thúc (End Date) --%>
					<div class="row mb-3">
						<div class="col-md-6">
							<label for="startDate" class="form-label">Ngày bắt đầu:</label>

							<%-- Gọi phương thức Getter mới --%>
							<input type="date" class="form-control" id="startDate"
								name="startDate" value="${promotion.formattedStartDate}"
								required>
						</div>

						<div class="col-md-6">
							<label for="endDate" class="form-label">Ngày kết thúc:</label>

							<%-- Gọi phương thức Getter mới --%>
							<input type="date" class="form-control" id="endDate"
								name="endDate" value="${promotion.formattedEndDate}" required>
						</div>
					</div>

					<div class="d-flex justify-content-between mt-4">
						<a href="${pageContext.request.contextPath}/admin/promotions"
							class="btn btn-outline-secondary"> <i
							class="fas fa-arrow-left me-1"></i> Quay lại
						</a>
						<button type="submit" class="btn btn-primary">
							<i class="fas fa-save me-1"></i> Lưu thay đổi
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	<script src="https://kit.fontawesome.com/a076d05399.js"
		crossorigin="anonymous"></script>

</body>
</html>