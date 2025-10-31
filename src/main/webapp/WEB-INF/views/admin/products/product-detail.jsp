<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4">📦 Chi Tiết Sản Phẩm #<c:out value="${productDetail.productId}"/>: <c:out value="${productDetail.productName}"/></h1>

<div class="row">
    <div class="col-lg-8">
        
        <%-- THÔNG TIN CƠ BẢN CỦA SẢN PHẨM --%>
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-light fw-bold">
                Thông Tin Cơ Bản
            </div>
            <div class="card-body">
                <div class="row">
                    <div class="col-md-6 mb-3">
                        **Tên Sản Phẩm:** <span class="fw-bold text-primary"><c:out value="${productDetail.productName}"/></span>
                    </div>
                    <div class="col-md-6 mb-3">
                        **Shop:** <span class="fw-bold"><c:out value="${productDetail.shop.shopName}"/></span>
                    </div>
                    <div class="col-md-6 mb-3">
                        **Danh Mục:** <c:out value="${productDetail.category.categoryName}"/>
                    </div>
                    
                    <div class="col-md-6 mb-3">
                        **Trạng Thái:** <span class="badge 
                            <c:choose>
                                <c:when test="${productDetail.status == 'ACTIVE'}">text-bg-success</c:when>
                                <c:when test="${productDetail.status == 'INACTIVE'}">text-bg-danger</c:when>
                                <c:otherwise>text-bg-secondary</c:otherwise>
                            </c:choose>
                        ">
                            <c:out value="${productDetail.status}"/>
                        </span>
                    </div>
                    <div class="col-md-6 mb-3">
                        **Bị Cấm Bán:** <span class="badge 
                            <c:choose>
                                <c:when test="${productDetail.isBanned}">text-bg-danger</c:when>
                                <c:otherwise>text-bg-success</c:otherwise>
                            </c:choose>
                        ">
                            <c:out value="${productDetail.isBanned ? 'CÓ' : 'KHÔNG'}"/>
                        </span>
                    </div>
                </div>
            </div>
        </div>

        <%-- MÔ TẢ SẢN PHẨM --%>
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-light fw-bold">
                Mô Tả Sản Phẩm
            </div>
            <div class="card-body">
                <p style="white-space: pre-wrap;"><c:out value="${productDetail.description}"/></p>
            </div>
        </div>
        
        <%-- PHẦN BÌNH LUẬN SẢN PHẨM (ProductComment) --%>
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-primary text-white fw-bold">
                💬 Bình Luận (${comments.size()})
            </div>
            <ul class="list-group list-group-flush">
                <c:choose>
                    <c:when test="${not empty comments}">
                        <c:forEach var="comment" items="${comments}">
                            <li class="list-group-item">
                                <p class="mb-1">
                                    **<c:out value="${comment.user.firstname} ${comment.user.lastname}"/>**
                                    
                                </p>
                                <p class="mb-0"><c:out value="${comment.content}"/></p>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <li class="list-group-item text-center text-muted">Chưa có bình luận nào cho sản phẩm này.</li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>

        <%-- PHẦN ĐÁNH GIÁ SẢN PHẨM (ProductReview) --%>
        <div class="card shadow-sm mb-4">
            <div class="card-header bg-warning text-dark fw-bold">
                ⭐ Đánh Giá (${reviews.size()})
            </div>
            <ul class="list-group list-group-flush">
                <c:choose>
                    <c:when test="${not empty reviews}">
                        <c:forEach var="review" items="${reviews}">
                            <li class="list-group-item">
                                <p class="mb-1">
                                    **<c:out value="${review.user.firstname} ${review.user.lastname}"/>**
                                    <span class="badge bg-warning text-dark me-2">
                                        <c:forEach begin="1" end="${review.rating}">⭐</c:forEach> (<c:out value="${review.rating}"/>/5)
                                    </span>
                                    
                                </p>
                                <c:if test="${not empty review.commentText}">
                                    <p class="mb-1">
                                        *Nhận xét:* <c:out value="${review.commentText}"/>
                                    </p>
                                </c:if>
                                
                                <%-- Hiển thị liên kết ảnh và video nếu có --%>
                                <div class="mt-2 small">
                                    <c:if test="${not empty review.imageUrl}">
                                        <a href="${review.imageUrl}" target="_blank" class="text-decoration-none me-3">
                                            [<i class="bi bi-image"></i> Xem Ảnh]
                                        </a>
                                    </c:if>
                                    <c:if test="${not empty review.videoUrl}">
                                        <a href="${review.videoUrl}" target="_blank" class="text-decoration-none">
                                            [<i class="bi bi-camera-video"></i> Xem Video]
                                        </a>
                                    </c:if>
                                </div>
                            </li>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <li class="list-group-item text-center text-muted">Chưa có đánh giá nào cho sản phẩm này.</li>
                    </c:otherwise>
                </c:choose>
            </ul>
        </div>
        
    </div>

    <div class="col-lg-4">
        
        <%-- GIÁ BÁN VÀ TỒN KHO --%>
        <div class="card text-white bg-info shadow-sm mb-4">
            <div class="card-header fw-bold">
                💰 Giá Bán & Tồn Kho
            </div>
            <div class="card-body">
                <c:if test="${productDetail.discountPrice != null && productDetail.discountPrice.compareTo(productDetail.price) < 0}">
                    <p class="mb-2 text-decoration-line-through">
                        **Giá Gốc:** <fmt:formatNumber value="${productDetail.price}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                    </p>
                    <h4 class="card-title fw-bold text-center mb-3">
                        Giá Bán: 
                    </h4>
                    <h2 class="card-title display-5 fw-bold text-center">
                        <fmt:formatNumber value="${productDetail.discountPrice}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                    </h2>
                </c:if>
                <c:if test="${productDetail.discountPrice == null || productDetail.discountPrice.compareTo(productDetail.price) >= 0}">
                    <h4 class="card-title fw-bold text-center mb-3">
                        Giá Bán: 
                    </h4>
                    <h2 class="card-title display-5 fw-bold text-center">
                        <fmt:formatNumber value="${productDetail.price}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                    </h2>
                </c:if>
                
                <hr class="my-3">
                <p class="mb-2">
                    **Tồn Kho:** <span class="fw-bold fs-4"><c:out value="${productDetail.stock}"/></span>
                </p>
                <p class="mb-0">
                    **Đánh Giá TB:** <span class="fw-bold fs-4"><c:out value="${productDetail.ratingAvg}"/>/5</span>
                </p>
            </div>
        </div>

        <%-- HÀNH ĐỘNG --%>
        <div class="card shadow-sm">
            <div class="card-header bg-light fw-bold">
                Hành Động
            </div>
            <div class="card-body d-grid gap-2">
                
                <%-- Giả định đây là trang quản trị, thêm nút chỉnh sửa --%>
                <a href="${pageContext.request.contextPath}/admin/products/edit?id=${productDetail.productId}" class="btn btn-warning">
                    <i class="bi bi-pencil-square"></i> Chỉnh Sửa Sản Phẩm
                </a>
                
                <a href="${pageContext.request.contextPath}/admin/products" class="btn btn-outline-secondary mt-3">
                    Quay lại Danh Sách Sản Phẩm
                </a>
            </div>
        </div>
    </div>
</div>