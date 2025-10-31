<%-- filepath: src/main/webapp/WEB-INF/views/vendor/statistics.jsp --%>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />

<head>
<title>Thống kê</title>
</head>

<div class="main-statistics py-4">
	<div
		class="d-flex align-items-center justify-content-between flex-wrap gap-2 mb-3">
		<h2 class="mb-0 gradient-text">Thống kê doanh thu</h2>

		<div class="card kpi-card filter-card-stats">
			<div class="card-body py-2">
				<form id="statFilter" class="row g-2">
					<div class="col-auto">
						<input type="date" class="form-control" id="statFrom"
							placeholder="From" />
					</div>
					<div class="col-auto">
						<input type="date" class="form-control" id="statTo" placeholder="To" />
					</div>
					<div class="col-auto">
						<input type="number" class="form-control" id="statYear"
							placeholder="Year (YYYY)" min="2000" max="2100" />
					</div>
					<div class="col-auto">
						<button class="btn btn-secondary" type="submit">Áp
							dụng</button>
					</div>
				</form>
			</div>
		</div>
	</div>

	<div id="totalRevenueWrap" class="alert alert-info glass-alert py-2"
		style="display: none;">
		<strong>Tổng doanh thu:</strong> <span id="totalRevenueVal"></span>
	</div>

	<div class="card kpi-card revenue-card-stats shadow-sm mb-3">
		<div class="card-body">
			<h5 class="card-title mb-3">Doanh thu theo tháng</h5>
			<canvas id="revenueChart" height="120"></canvas>
			<div id="noDataMsg" class="text-muted small mt-2"
				style="display: none;">Không có dữ liệu.</div>
		</div>
	</div>

	<div class="card recent-orders-card shadow-sm" id="prodCard">
		<div class="card-body">
			<h5 class="card-title mb-3">Doanh thu theo sản phẩm (Top)</h5>
			<div class="row g-3">
				<div class="col-lg-7">
					<canvas id="prodChart" height="160"></canvas>
				</div>
				<div class="col-lg-5">
					<div class="table-responsive">
						<table class="table table-sm align-middle table-hover">
							<thead>
								<tr>
									<th>#</th>
									<th>Sản phẩm</th>
									<th class="text-end">Doanh thu</th>
								</tr>
							</thead>
							<tbody id="prodTableBody"></tbody>
						</table>
					</div>
				</div>
			</div>
		</div>
	</div>

</div>

<script
	src="https://cdn.jsdelivr.net/npm/chart.js@4.4.1/dist/chart.umd.min.js"></script>
	
<script>
  const chartEl = document.getElementById('revenueChart');
  const prodEl  = document.getElementById('prodChart');
  const noDataMsg = document.getElementById('noDataMsg');

  const totalWrap = document.getElementById('totalRevenueWrap');
  const totalVal  = document.getElementById('totalRevenueVal');

  const prodCard  = document.getElementById('prodCard');
  const prodTBody = document.getElementById('prodTableBody');

  let chart, prodChart;

  async function loadChart(params) {
	    const url = new URL('${ctx}/vendor/statistics', window.location.origin);
	    if (params.from) url.searchParams.set('from', params.from);
	    if (params.to)   url.searchParams.set('to', params.to);
	    if (params.year) url.searchParams.set('year', params.year);

	    const res  = await fetch(url.toString(), { headers: { "Accept": "application/json" } });
	    const data = await res.json(); // {labels,current,previous,values,productRevenue?}

	    const labels = data.labels || [];
	    const cur = data.current || data.values || [];
	    const pre = data.previous || [];

	    const isEmpty = (!labels.length) || (cur.every(v => (+v)===0) && (!pre.length || pre.every(v => (+v)===0)));
	    noDataMsg.style.display = isEmpty ? 'block' : 'none';

	    const total = (cur || []).reduce((s,v)=>s+(Number(v)||0),0);
	    if (total > 0) {
	      totalVal.textContent = Number(total).toLocaleString('vi-VN',{style:'currency',currency:'VND'});
	      totalWrap.style.display = 'block';
	    } else totalWrap.style.display = 'none';

		/* === BẮT ĐẦU "ĐỘ" MÀU CHART 1 === */
		const revGradient = chartEl.getContext('2d').createLinearGradient(0, 0, 0, 400);
		revGradient.addColorStop(0, 'rgba(231, 60, 126, 0.8)'); // Màu hồng (theme)
		revGradient.addColorStop(1, 'rgba(35, 166, 213, 0.8)'); // Màu xanh (theme)
		
		const revGradientPrev = chartEl.getContext('2d').createLinearGradient(0, 0, 0, 400);
		revGradientPrev.addColorStop(0, 'rgba(108, 117, 125, 0.5)'); // Xám nhạt
		revGradientPrev.addColorStop(1, 'rgba(108, 117, 125, 0.2)'); // Xám trong
		/* === KẾT THÚC "ĐỘ" MÀU CHART 1 === */
		
	    if (chart) chart.destroy();
	    chart = new Chart(chartEl, {
	      type: 'bar',
	      data: { labels, datasets: [
	        { 
	        	label: 'Năm trước',   
	        	data: pre,
	        	backgroundColor: revGradientPrev, // Dùng màu xám mờ
	        	borderRadius: 4
	        },
	        { 
	        	label: 'Năm hiện tại', 
	        	data: cur,
	        	backgroundColor: revGradient, // Dùng gradient "cháy"
	        	borderRadius: 4
	        }
	      ]},
	      options: {
	        responsive: true,
	        scales: { 
	        	y: { beginAtZero: true },
	        	x: { grid: { display: false } }
	        },
	        plugins: { tooltip: { callbacks: {
	          label: (ctx) => {
	            const v = ctx.parsed?.y ?? ctx.raw ?? 0;
	            return ctx.dataset.label + ': ' + Number(v).toLocaleString('vi-VN') + ' ₫';
	          }
	        }}}
	      }
	    });

	    const prod = Array.isArray(data.productRevenue) ? data.productRevenue : [];
	    
	    prodCard.style.display = 'block'; 
	    
	    if (prod.length) {
	      const top = [...prod].sort((a,b)=>Number(b.total)-Number(a.total)).slice(0,10);
	      const pLabels = top.map(x=>x.product);
	      const pValues = top.map(x=>Number(x.total)||0);

		  /* === BẮT ĐẦU "ĐỘ" MÀU CHART 2 (SẢN PHẨM) === */
		  /* Đây là gradient ngang (xanh -> xanh ngọc) */
		  const prodGradient = prodEl.getContext('2d').createLinearGradient(0, 0, 400, 0);
		  prodGradient.addColorStop(0, 'rgba(35, 166, 213, 0.8)'); // Xanh
		  prodGradient.addColorStop(1, 'rgba(35, 213, 171, 0.8)'); // Xanh ngọc
		  /* === KẾT THÚC "ĐỘ" MÀU CHART 2 === */

	      if (prodChart) prodChart.destroy();
	      prodChart = new Chart(prodEl, {
	        type: 'bar',
	        data: { 
	        	labels: pLabels, 
	        	datasets: [{ 
	        		label: 'Doanh thu', 
	        		data: pValues,
	        		backgroundColor: prodGradient, // Dùng gradient ngang
	        		borderRadius: 4
	        	}] 
	        },
	        options: {
	          responsive: true,
	          indexAxis: 'y',
	          scales: { 
	        	  x: { beginAtZero: true },
	        	  y: { grid: { display: false } }
	          },
	          plugins: { 
	        	  legend: { display: false }, // Bỏ "Doanh thu"
	        	  tooltip: { callbacks: {
		            label: (ctx) => {
		              const v = ctx.parsed?.x ?? ctx.raw ?? 0;
		              return Number(v).toLocaleString('vi-VN') + ' ₫';
		            }
	          }}}
	        }
	      });

	      // Hiển thị dữ liệu trong bảng
	      prodTBody.innerHTML = top.map((x,i)=>(
	        '<tr>' +
	          '<td>' + (i+1) + '</td>' +
	          '<td><span class="text-truncate d-inline-block" style="max-width:220px" title="' + x.product + '">' + x.product + '</span></td>' +
	          '<td class="text-end">' + Number(x.total||0).toLocaleString('vi-VN') + ' ₫</td>' +
	        '</tr>')).join('');

	    } else {
	      prodTBody.innerHTML = '<tr><td colspan="3" class="text-center text-muted">Không có dữ liệu sản phẩm trong phạm vi này.</td></tr>';
	      if (prodChart) { prodChart.destroy(); prodChart = null; }
	    }
	  }

	  document.getElementById('statFilter').addEventListener('submit', (e)=>{
	    e.preventDefault();
	    const year = (document.getElementById('statYear').value||'').trim();
	    const from = (document.getElementById('statFrom').value||'').trim();
	    const to   = (document.getElementById('statTo').value||'').trim();
	    if (year) loadChart({year}); else loadChart({from: from||null, to: to||null});
	  });

	  loadChart({});
	</script>