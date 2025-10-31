<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<h1 class="mb-4 text-primary">🤝 Mạng Xã Hội Shipper</h1>
<p class="lead">Nơi chia sẻ kinh nghiệm, cảnh báo tuyến đường và bàn
	tán công việc.</p>

<div class="row">
	<div class="col-lg-8">

		<%-- PHẦN 1: ĐĂNG BÀI MỚI ĐÃ SỬA --%>
		<div class="card mb-3 shadow-sm">
			<div class="card-header bg-primary text-white">Đăng bài mới</div>
			<div class="card-body">
				<form method="post"
					action="${pageContext.request.contextPath}/shipper/social/add">

					<%-- THÊM TRƯỜNG CHỌN LOẠI BÀI ĐĂNG --%>
					<div class="mb-3">
						<label for="postType" class="form-label fw-bold">Chọn Mục
							Đích Bài Đăng</label> <select class="form-select" id="postType"
							name="postType" required>
							<option value="">-- Chọn loại bài --</option>
							<option value="TRAFFIC_ALERT">🚨 Cảnh Báo Giao Thông/Kẹt
								Xe</option>
							<option value="EXPERIENCE">💰 Kinh Nghiệm Giao Hàng/Tips</option>
							<option value="DISCUSSION">💬 Thảo Luận Chung/Hỏi Đáp</option>
						</select>
					</div>

					<div class="mb-3">
						<label for="postTitle" class="form-label fw-bold">Tiêu Đề
							Bài Đăng</label> <input type="text" class="form-control" id="postTitle"
							name="title" maxlength="250" required>
					</div>

					<div class="mb-3">
						<label for="postContent" class="form-label fw-bold">Nội
							Dung</label>
						<textarea class="form-control" id="postContent" name="content"
							rows="3" placeholder="Chia sẻ chi tiết nội dung của bạn..."
							required></textarea>
					</div>
					<button type="submit" class="btn btn-primary">Đăng Bài</button>
				</form>
			</div>
		</div>

		<%-- PHẦN 2: DANH SÁCH BÀI VIẾT (Tải từ Controller) --%>
		<c:choose>
			<c:when test="${not empty posts}">
				<c:forEach var="post" items="${posts}">
					<div class="card mb-3">
						<div class="card-body">
							<%-- Hiển thị Tiêu đề và Người đăng --%>
							<h5
								class="card-title ${post.postType == 'TRAFFIC_ALERT' ? 'text-primary' : 'text-success'}">
								<c:out value="${post.title}" />
							</h5>
							<h6 class="card-subtitle mb-2 text-muted">
								**Đăng bởi:**
								<c:out
									value="${post.shipper.firstname} ${post.shipper.lastname}" />
								•
								<%-- Gán thẳng giá trị String đã được định dạng từ getter --%>
								<c:out value="${post.formattedTime}" />
							</h6>

							<%-- Nội dung bài viết --%>
							<p class="card-text">
								<c:out value="${post.content}" />
							</p>


						</div>
					</div>
				</c:forEach>

				<%-- PHẦN 3: PHÂN TRANG --%>
				<nav aria-label="Phân trang bài viết">
					<ul class="pagination justify-content-center">
						<%-- Nút Previous --%>
						<li class="page-item ${currentPage == 1 ? 'disabled' : ''}">
							<a class="page-link"
							href="<c:url value='/shipper/social'><c:param name='page' value='${currentPage - 1}'/></c:url>">Trước</a>
						</li>

						<%-- Hiển thị các trang --%>
						<c:forEach begin="1" end="${totalPages}" var="i">
							<li class="page-item ${i == currentPage ? 'active' : ''}"><a
								class="page-link"
								href="<c:url value='/shipper/social'><c:param name='page' value='${i}'/></c:url>"><c:out
										value="${i}" /></a></li>
						</c:forEach>

						<%-- Nút Next --%>
						<li
							class="page-item ${currentPage == totalPages ? 'disabled' : ''}">
							<a class="page-link"
							href="<c:url value='/shipper/social'><c:param name='page' value='${currentPage + 1}'/></c:url>">Sau</a>
						</li>
					</ul>
				</nav>

			</c:when>
			<c:otherwise>
				<div class="alert alert-info text-center">Chưa có bài viết nào
					được đăng.</div>
			</c:otherwise>
		</c:choose>

	</div>

	<%-- PHẦN 4: THÀNH VIÊN HOẠT ĐỘNG --%>
	<div class="col-lg-4">
		<div class="card shadow-sm">
			<div class="card-header bg-secondary text-white">Thành viên
				hoạt động</div>
			<ul class="list-group list-group-flush small">
				<%-- Lặp qua dữ liệu giả định từ Controller (ví dụ: Map<String, Long>) --%>
				<c:forEach var="entry" items="${totalCommentsByUser}">
					<li class="list-group-item"><c:out value="${entry.key}" /> (<c:out
							value="${entry.value}" /> bình luận)</li>
				</c:forEach>

				<%-- Thêm một mục ví dụ nếu không có dữ liệu Map --%>
				<c:if test="${empty totalCommentsByUser}">
					<li class="list-group-item">Shipper-Tuan (3 bài hôm nay)</li>
					<li class="list-group-item">Shipper-Lan (12 bình luận)</li>
					<li class="list-group-item">Shipper-Hung (Đang online)</li>
				</c:if>
			</ul>
		</div>
	</div>
</div>