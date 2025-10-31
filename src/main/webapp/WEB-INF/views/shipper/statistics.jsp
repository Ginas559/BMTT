<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<h1 class="mb-4 text-dark">📈 Thống Kê Hiệu Suất Của Tôi</h1>
<p class="lead">Tổng quan hoạt động.</p>

<div class="row g-4 mb-5">
    <%-- Giả định các biến thống kê được truyền vào --%>
    <c:set var="totalDelivered" value="${totalDelivered}" />
    <c:set var="totalRevenue" value="${totalRevenue}" />
    <c:set var="cancellationRate" value="${cancellationRate}" />

    <div class="col-md-4">
        <div class="card text-center bg-success text-white shadow-sm h-100">
            <div class="card-body">
                <h1 class="card-title display-4 fw-bold"><c:out value="${totalDelivered}"/></h1>
                <p class="card-text">Đơn hàng đã giao thành công</p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-center bg-primary text-white shadow-sm h-100">
            <div class="card-body">
                <h1 class="card-title display-4 fw-bold">
                    <fmt:formatNumber value="${totalRevenue * 0.2}" type="currency" currencyCode="VND" maxFractionDigits="0"/>
                </h1>
                <p class="card-text">Tổng Thu Nhập Ước Tính</p>
            </div>
        </div>
    </div>
    <div class="col-md-4">
        <div class="card text-center bg-danger text-white shadow-sm h-100">
            <div class="card-body">
                <h1 class="card-title display-4 fw-bold">
                    <fmt:formatNumber value="${cancellationRate}" type="percent" maxFractionDigits="1"/>
                </h1>
                <p class="card-text">Tỉ lệ hủy đơn hàng cá nhân (Toàn thời gian)</p>
            </div>
        </div>
    </div>
</div>

<h3 class="mb-3 text-dark">Chi Tiết Năng Suất</h3>
<div class="card shadow-sm">
    <div class="card-header bg-light">
        Hoạt động giao hàng hàng ngày
    </div>
    <div class="card-body">
        <p class="text-muted">
            <i class="bi bi-graph-up"></i>
            [Vùng này dùng để hiển thị biểu đồ đường (Line Chart) về số lượng đơn hàng giao mỗi ngày trong tháng. Bạn có thể dùng thư viện Chart.js để vẽ biểu đồ.]
        </p>
        <p class="text-center fw-bold">Đơn hàng đã giao: </p>
    </div>
</div>

<div class="mt-4">
    <h3 class="mb-3 text-dark">Địa Điểm Nổi Bật</h3>
    <p class="small text-muted">Khu vực bạn giao hàng nhiều nhất (giúp tối ưu tuyến đường).</p>
    <ul class="list-group">
        <li class="list-group-item d-flex justify-content-between align-items-center">
            Quận 3, TP.HCM
            <span class="badge bg-primary rounded-pill">35%</span>
        </li>
        <li class="list-group-item d-flex justify-content-between align-items-center">
            Quận 10, TP.HCM
            <span class="badge bg-primary rounded-pill">25%</span>
        </li>
        <li class="list-group-item d-flex justify-content-between align-items-center">
            Quận Bình Thạnh
            <span class="badge bg-primary rounded-pill">15%</span>
        </li>
    </ul>
</div>