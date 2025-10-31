<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
<head>
<meta charset="UTF-8">
<title>Thêm Khuyến Mãi</title>
<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
	rel="stylesheet">
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css">
<style>
body {
	background-color: #f8f9fa;
}

.card {
	max-width: 650px; /* Tăng kích thước tối đa để chứa thêm trường */
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
		<div class="card shadow-lg">
			<div class="card-body">
				<h3 class="text-center text-primary mb-4">
					<i class="fas fa-plus-circle me-2"></i>Thêm Khuyến Mãi Mới
				</h3>

				<form
					action="${pageContext.request.contextPath}/admin/promotions/add"
					method="post">

					<%-- 1. Trường Tiêu đề (Title) --%>
					<div class="mb-3">
						<label for="title" class="form-label">Tiêu đề khuyến mãi:</label>
						<input type="text" class="form-control" id="title"
							name="title" placeholder="VD: Khuyến mãi mùa hè giảm 15%" required>
					</div>
                    
                    <%-- 2. Trường Shop ID (Liên kết ManyToOne) --%>
                    <div class="mb-3">
                        <label for="shopId" class="form-label">Cửa hàng áp dụng:</label>
                        <select class="form-select" id="shopId" name="shopId" required>
                            <option value="" disabled selected>Chọn cửa hàng...</option>
                            <%-- GIẢ ĐỊNH: Controller đã truyền danh sách Shop dưới tên 'shops' --%>
                            <c:forEach var="shop" items="${shops}">
                                <option value="${shop.shopId}">${shop.shopName}</option>
                            </c:forEach>
                            <%-- Nếu không có danh sách shops, bạn cần thêm logic này vào Controller --%>
                        </select>
                    </div>

					<%-- 3. Trường Phần trăm giảm (Discount Percent) --%>
					<div class="mb-3">
						<label for="discountPercent" class="form-label">Phần trăm giảm (%):</label>
						<input type="number" step="0.01" min="0" max="100" class="form-control" id="discountPercent"
							name="discountPercent" placeholder="Nhập phần trăm giảm (VD: 15.5)" required>
					</div>

					<%-- 4. Trường Phạm vi áp dụng (Apply To) --%>
					<div class="mb-3">
						<label for="applyTo" class="form-label">Phạm vi áp dụng:</label>
						<select class="form-select" id="applyTo" name="applyTo" required>
							<option value="PRODUCT">Giảm giá Sản phẩm</option>
							<option value="SHIPPING">Giảm phí Vận chuyển</option>
						</select>
					</div>
                    
                    <%-- 5. Trường Status (Trạng thái ban đầu) --%>
                    <div class="mb-3">
						<label for="status" class="form-label">Trạng thái ban đầu:</label>
						<select class="form-select" id="status" name="status" required>
                            <option value="ACTIVE" selected>ACTIVE (Hoạt động)</option>
                            <option value="INACTIVE">INACTIVE (Không hoạt động)</option>
                        </select>
					</div>

					<%-- 6. Trường Ngày bắt đầu và Ngày kết thúc --%>
					<div class="row mb-3">
						<div class="col-md-6">
							<label for="startDate" class="form-label">Ngày bắt đầu:</label>
							<input type="date" class="form-control" id="startDate"
								name="startDate" required>
						</div>
					
						<div class="col-md-6">
							<label for="endDate" class="form-label">Ngày kết thúc:</label>
							<input type="date" class="form-control" id="endDate"
								name="endDate" required>
						</div>
					</div>

					<div class="d-flex justify-content-between mt-4">
						<a href="${pageContext.request.contextPath}/admin/promotions"
							class="btn btn-outline-secondary"> 
							<i class="fas fa-arrow-left me-1"></i> Quay lại
						</a>
						<button type="submit" class="btn btn-primary">
							<i class="fas fa-save me-1"></i> Thêm khuyến mãi
						</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	</body>
</html>