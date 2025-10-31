<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

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
	href="${pageContext.request.contextPath}/assets/css/web.css">

<link rel="stylesheet"
	href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
</head>

<c:set var="ctx" value="${pageContext.request.contextPath}" />

<main class="container py-4 main-dashboard">
	<div
		class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
		<h2 class="mb-0 gradient-text">Vendor Dashboard</h2>

		<form class="row g-2" method="get" action="${ctx}/vendor/dashboard">
			<div class="col-auto">
				<input type="date" class="form-control" name="from"
					value="${filterFrom}" placeholder="From (YYYY-MM-DD)" />
			</div>
			<div class="col-auto">
				<input type="date" class="form-control" name="to"
					value="${filterTo}" placeholder="To (YYYY-MM-DD)" />
			</div>
			<div class="col-auto">
				<input type="number" class="form-control" name="year"
					value="${filterYear}" placeholder="Year (YYYY)" min="2000"
					max="2100" />
			</div>
			<div class="col-auto">
				<button class="btn btn-outline-secondary">Áp dụng</button>
			</div>
		</form>
	</div>

	<div class="mb-4 d-flex flex-wrap gap-3">
		<a href="${ctx}/vendor/products" class="btn btn-primary">🛍️ Quản
			lý sản phẩm</a> <a href="${ctx}/vendor/orders" class="btn btn-success">📦
			Đơn hàng</a>
		<c:if test="${not empty shop and not empty shop.shopId}">
			<a href="${ctx}/chat?shopId=${shop.shopId}"
				class="btn btn-outline-primary btn-chat">💬 Chat nội bộ</a>
			<a href="${ctx}/chat/public?shopId=${shop.shopId}"
				class="btn btn-outline-primary btn-chat">💬 Chat công khai</a>
		</c:if>
	</div>


	<div class="row g-3 kpi-cards">
		<div class="col-md-3">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h6 class="text-muted mb-1">Tổng đơn hàng</h6>
					<p class="display-6 mb-0 gradient-text">
						<c:out value="${stats.orderCount}" />
					</p>
				</div>
			</div>
		</div>
		<div class="col-md-3">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h6 class="text-muted mb-1">Doanh thu (DELIVERED)</h6>
					<p class="display-6 mb-0 gradient-text">
						<c:out value="${stats.revenue}" />
					</p>
				</div>
			</div>
		</div>
		<div class="col-md-3">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h6 class="text-muted mb-1">Tăng trưởng MoM</h6>
					<c:set var="grow" value="${growthPercent}" />
					<div class="d-flex align-items-baseline gap-2">
						<span class="display-6 mb-0 gradient-text"> <c:out
								value="${grow}" />%
						</span>
						<c:choose>
							<c:when test="${grow >= 0}">
								<span class="badge bg-success">↑</span>
							</c:when>
							<c:otherwise>
								<span class="badge bg-danger">↓</span>
							</c:otherwise>
						</c:choose>
					</div>
					<small class="text-muted">So với tháng trước</small>
				</div>
			</div>
		</div>
		<div class="col-md-3">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h6 class="text-muted mb-1">Top sản phẩm</h6>
					<ol class="mb-0 small">
						<c:forEach var="row" items="${stats.topProducts}">
							<li><span class="text-truncate d-inline-block"
								style="max-width: 180px;"> <c:out value="${row[0]}" />
							</span> — SL: <strong><c:out value="${row[1]}" /></strong></li>
						</c:forEach>
						<c:if test="${empty stats.topProducts}">
							<li class="text-muted">Chưa có dữ liệu</li>
						</c:if>
					</ol>
				</div>
			</div>
		</div>
	</div>

	<hr class="my-4" />

	<div class="row g-3 chart-cards">
		<div class="col-lg-7">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h5 class="card-title mb-3">Doanh thu theo tháng</h5>
					<canvas id="revChart" height="140"></canvas>
				</div>
			</div>
		</div>
		<div class="col-lg-5">
			<div class="card shadow-sm h-100 kpi-card">
				<div class="card-body">
					<h5 class="card-title mb-3">Cơ cấu trạng thái đơn hàng</h5>
					<canvas id="statusChart" height="140"></canvas>
				</div>
			</div>
		</div>
	</div>

	<hr class="my-4" />

	<div class="card shadow-sm recent-orders-card">
		<div class="card-body">
			<h5 class="card-title mb-3">Đơn gần đây</h5>
			<div class="table-responsive">
				<table class="table table-sm align-middle table-hover">
					<thead class="table-light">
						<tr>
							<th>ID</th>
							<th>Khách hàng</th>
							<th>Tổng tiền</th>
							<th>Trạng thái</th>
							<th>Ngày tạo</th>
						</tr>
					</thead>
					<tbody id="recentOrdersTable">
						<c:forEach var="o" items="${recentOrders}">
							<tr>
								<td>#<c:out value="${o.orderId}" /></td>
								<td><c:out value="${o.user.firstname}" /> <c:out
										value="${o.user.lastname}" /></td>
								<td><c:out value="${o.totalAmount}" /></td>
								<td>
									<%-- Tui đã bỏ <c:set> và dùng o.status.name() để so sánh --%>
									<c:choose>
										<c:when test="${o.status.name() == 'PENDING'}">
											<span class="badge bg-warning">Chờ xác nhận</span>
										</c:when>
										<c:when test="${o.status.name() == 'CONFIRMED'}">
											<span class="badge bg-info">Đã xác nhận</span>
										</c:when>
										<c:when test="${o.status.name() == 'SHIPPING'}">
											<span class="badge bg-primary">Đang giao</span>
										</c:when>
										<c:when test="${o.status.name() == 'DELIVERED'}">
											<span class="badge bg-success">Đã giao</span>
										</c:when>
										<c:when test="${o.status.name() == 'CANCELLED'}">
											<span class="badge bg-danger">Đã hủy</span>
										</c:when>
										<c:otherwise>
											<span class="badge bg-secondary"><c:out
													value="${o.status}" /></span>
										</c:otherwise>
									</c:choose>
								</td>
								<td><c:out value="${o.createdAt}" /></td>
							</tr>
						</c:forEach>
						<c:if test="${empty recentOrders}">
							<tr>
								<td colspan="5" class="text-muted">Chưa có đơn hàng.</td>
							</tr>
						</c:if>
					</tbody>
				</table>
			</div>
		</div>
	</div>
</main>

<script
	src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
	
<script>
  // Build revenue arrays from server-side 'revenueRows' => [ [month, total], ... ]
  const revLabels = [];
  const revValues = [];
  <%-- JSTL → JS --%>
  <c:forEach var="r" items="${revenueRows}">
    revLabels.push("Tháng ${r[0]}");
    revValues.push(${r[1]});
  </c:forEach>

  const ctxRev = document.getElementById('revChart');
  if (ctxRev) {
	const revGradient = ctxRev.getContext('2d').createLinearGradient(0, 0, 0, 400);
	revGradient.addColorStop(0, 'rgba(231, 60, 126, 0.8)'); // Màu hồng (theme)
	revGradient.addColorStop(1, 'rgba(35, 166, 213, 0.8)'); // Màu xanh (theme)
  
    new Chart(ctxRev, {
      type: 'bar',
      data: { 
    	  labels: revLabels, 
    	  datasets: [{ 
    		  label: 'Doanh thu', 
    		  data: revValues,
    		  backgroundColor: revGradient, // Dùng gradient
    		  borderColor: 'rgba(255, 255, 255, 0.1)',
    		  borderWidth: 1,
    		  borderRadius: 4
    	  }] 
      },
      options: { 
    	  responsive: true, 
    	  scales: { 
    		  y: { beginAtZero: true },
    		  x: { grid: { display: false } }
    	  },
    	  plugins: { legend: { display: false } }
      }
    });
  }

  // Build status chart from 'statusCounts' => Map {status -> count}
  const statusLabels = [];
  const statusValues = [];
  <c:forEach var="e" items="${statusCounts}">
    statusLabels.push("${e.key}");
    statusValues.push(${e.value});
  </c:forEach>

  const ctxStatus = document.getElementById('statusChart');
  if (ctxStatus) {
    new Chart(ctxStatus, {
      type: 'doughnut',
      data: { 
    	  labels: statusLabels, 
    	  datasets: [{ 
    		  label: 'Số đơn', 
    		  data: statusValues,
    		  // Dùng các màu mè trong theme của mình
    		  backgroundColor: [
    			  'rgba(231, 60, 126, 0.8)', // PENDING (Hồng)
                  'rgba(35, 166, 213, 0.8)', // CONFIRMED (Xanh dương)
                  'rgba(25, 135, 84, 0.8)',  // DELIVERED (Xanh lá)
                  'rgba(220, 53, 69, 0.8)',  // CANCELLED (Đỏ)
                  'rgba(35, 213, 171, 0.8)', // SHIPPING (Xanh ngọc)
                  'rgba(108, 117, 125, 0.8)' // OTHER
    		  ],
    		  borderColor: 'rgba(255, 255, 255, 0.1)',
    		  borderWidth: 2
    	  }] 
      },
      options: { 
    	  responsive: true,
    	  plugins: {
              legend: {
                  position: 'bottom', // Đưa chú thích xuống dưới
              }
          }
      }
    });
  }
</script>