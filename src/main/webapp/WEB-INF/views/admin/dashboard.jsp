<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %> <%-- Thêm JSTL FMT để định dạng tiền tệ --%>

<sitemesh:page title="Dashboard - ShoesShop Admin">
    
    <h2>📊 Thống Kê Tổng Quan</h2>
    
    <div class="row g-3">
        
        <div class="col-md-3">
            <div class="card shadow-sm border-0 bg-primary text-white">
                <div class="card-body">
                    <h5>👥 Users</h5>
                    <h3>${userCount}</h3>
                </div>
            </div>
        </div>
        
        <div class="col-md-3">
            <div class="card shadow-sm border-0 bg-success text-white">
                <div class="card-body">
                    <h5>📦 Orders</h5>
                    <h3>${orderCount}</h3>
                </div>
            </div>
        </div>
        
        <div class="col-md-3">
            <div class="card shadow-sm border-0 bg-warning text-white">
                <div class="card-body">
                    <h5>👟 Products</h5>
                    <h3>${productCount}</h3>
                </div>
            </div>
        </div>
        
        <div class="col-md-3">
            <div class="card shadow-sm border-0 bg-info text-white">
                <div class="card-body">
                    <h5>💰 Revenue</h5>
                    <%-- Sử dụng fmt:formatNumber để định dạng tiền tệ (Ví dụ: 1,000,000) --%>
                    <h3>
                        <fmt:formatNumber value="${revenueAmount}" type="currency" currencySymbol="VND"/>
                    </h3>
                </div>
            </div>
        </div>
        
        <div class="w-100"></div> 
        
        <div class="col-md-3">
            <div class="card shadow-sm border-0 bg-secondary text-white">
                <div class="card-body">
                    <h5>📈 Total Stock</h5>
                    <h3>${inventoryQuantity}</h3>
                </div>
            </div>
        </div>
        
    </div>

    <hr/>

    <h2>📊 Biểu đồ Doanh Thu Theo Ngày</h2>

    <div class="row mt-4">
        <div class="col-12">
            <div class="card shadow-sm border-0">
                <div class="card-header">
                    Doanh Thu Trong 30 Ngày Gần Nhất
                </div>
                <div class="card-body">
                    <div id="revenueChart">
                        <p class="text-muted">Nơi hiển thị biểu đồ doanh thu.</p>
                        
                        <%-- Ví dụ về cách Controller có thể truyền dữ liệu để JS sử dụng --%>
                        <%-- <script>
                            const revenueData = ${dailyRevenueData}; // Giả sử là JSON String
                            // Khởi tạo biểu đồ với Chart.js
                        </script> --%>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
</sitemesh:page>