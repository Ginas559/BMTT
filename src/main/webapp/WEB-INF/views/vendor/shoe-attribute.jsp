<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!doctype html>
<html lang="vi">
<head>
<meta charset="utf-8" />
<meta name="viewport" content="width=device-width, initial-scale=1" />
<title>${pageTitle != null ? pageTitle : 'BMTT Shop'}</title>

<sitemesh:write property="head" />

<link
	href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"
	rel="stylesheet">

<link rel="stylesheet"
	href="${pageContext.request.contextPath}/assets/css/web2.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<%-- ĐÃ THÊM: class "main-vendor-attributes" để "ăn" nền pastel --%>
<main class="container py-4 main-vendor-attributes">

    <div class="mb-4 d-flex flex-wrap gap-3">
        <%-- Các nút này sẽ tự "ăn" pulse/shine từ v1 --%>
        <a href="${ctx}/vendor/products" class="btn btn-primary">🛍️ Quản
            lý sản phẩm</a>
        <a href="${ctx}/vendor/orders" class="btn btn-success">📦
            Đơn hàng</a>
        <c:if test="${not empty shop and not empty shop.shopId}">
            <%-- ĐÃ THÊM: class "btn-chat" (từ v1) để "ăn" pulse --%>
            <a href="${ctx}/chat?shopId=${shop.shopId}"
                class="btn btn-outline-primary btn-chat">💬 Chat nội bộ</a>
            <a href="${ctx}/chat/public?shopId=${shop.shopId}"
                class="btn btn-outline-primary btn-chat">💬 Chat công khai</a>
        </c:if>
    </div>
    
    <c:if test="${not empty successMessage}">
        <%-- ĐÃ THÊM: Hiệu ứng "glow" xanh lá cho alert --%>
        <div class="alert alert-success alert-dismissible fade show" role="alert" 
             style="box-shadow: 0 0 15px rgba(25, 135, 84, 0.5);">
            🎉 **Thành công!** ${successMessage}
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    </c:if>

    <%-- ĐÃ THÊM: class "gradient-text" (từ v1) cho "cháy" --%>
    <h2 class="mb-3 gradient-text" style="font-weight: 700;">Thuộc tính giày: ${product.productName}</h2>

    <%-- 
      ĐÃ THÊM: Bọc form vào "form-card-pink"
      Style của card này sẽ được định nghĩa trong web2.css
    --%>
    <div class="card shadow-sm form-card-pink">
        <div class="card-body">
            <h5 class="card-title mb-3" style="font-weight: 600;">Chi tiết thuộc tính</h5>
            <form method="post" class="row g-3">
                <input type="hidden" name="productId" value="${product.productId}"/>

                <div class="col-md-6">
                    <label class="form-label">Thương hiệu</label>
                    <input type="text" name="brand" class="form-control"
                           value="${attr != null ? attr.brand : ''}" placeholder="Nike, Adidas, v.v.">
                </div>

                <div class="col-md-6">
                    <label class="form-label">Chất liệu</label>
                    <input type="text" name="material" class="form-control"
                           value="${attr != null ? attr.material : ''}" placeholder="Da, Vải, PU...">
                </div>

                <div class="col-md-6">
                    <label class="form-label">Giới tính</label>
                    <select name="gender" class="form-select">
                        <option value="">-- Chọn --</option>
                        <option value="Nam" ${attr.gender == 'Nam' ? 'selected' : ''}>Nam</option>
                        <option value="Nữ" ${attr.gender == 'Nữ' ? 'selected' : ''}>Nữ</option>
                        <option value="Unisex" ${attr.gender == 'Unisex' ? 'selected' : ''}>Unisex</option>
                    </select>
                </div>

                <div class="col-md-6">
                    <label class="form-label">Kiểu dáng</label>
                    <input type="text" name="style" class="form-control"
                           value="${attr != null ? attr.style : ''}" placeholder="Sneaker, Boot, Thể thao...">
                </div>

                <div class="col-12">
                    <%-- Nút này sẽ được "nhuộm" hồng "cháy" trong web2.css --%>
                    <button class="btn btn-primary">Lưu thay đổi</button>
                </div>
            </form>
        </div>
    </div>
</main>