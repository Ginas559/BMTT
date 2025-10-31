<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<fmt:setLocale value="vi_VN" scope="page"/>

<%-- 💥 THÊM LIÊN KẾT ĐẾN TỆP CSS HIỆU ỨNG --%>
<%-- (Giả định bạn đã đặt file web.css và recent-view-section.css trong thư mục 'css') --%>
<link rel="stylesheet" href="${ctx}/css/web.css">
<link rel="stylesheet" href="${ctx}/css/recent-view-section.css">

<div class="container py-4 magic-section">
  <div class="recent-header mb-4">
    <h1 class="h4 fw-bold m-0 gradient-text" style="font-size: 1.5rem;">
      <i class="bi bi-clock-history me-2"></i>Bạn đã xem gần đây
    </h1>
    <%-- Nút "Tiếp tục mua sắm" được nâng cấp lên btn-success (có hiệu ứng Pulse) --%>
    <a class="btn btn-sm btn-success btn-shine" href="${ctx}/products">
      <i class="bi bi-bag-check-fill me-1"></i>Tiếp tục mua sắm
    </a>
  </div>

  <c:choose>
    <c:when test="${not empty recentItems}">
      <div class="row row-cols-2 row-cols-md-3 row-cols-lg-6 g-3 recent-grid">
        <c:forEach var="item" items="${recentItems}">
          <%-- Resolve coverUrl an toàn (GIỮ NGUYÊN) --%>
          <c:set var="rvRaw"   value="${empty item.coverUrl ? '' : item.coverUrl}"/>
          <c:set var="rvFixed" value="${fn:replace(rvRaw, '/assset/', '/assets/')}"/>
          <c:choose>
            <c:when test="${fn:startsWith(rvFixed,'http://') or fn:startsWith(rvFixed,'https://')}">
              <c:set var="rvCover" value="${rvFixed}"/>
            </c:when>
            <c:when test="${fn:startsWith(rvFixed,'/assets/')}">
              <c:set var="rvCover" value="${ctx.concat(rvFixed)}"/>
            </c:when>
            <c:when test="${fn:startsWith(rvFixed,'/')}">
              <c:set var="rvCover" value="${rvFixed}"/>
            </c:when>
            <c:otherwise>
              <c:set var="rvCover" value="${ctx.concat('/assets/products/').concat(rvFixed)}"/>
            </c:otherwise>
          </c:choose>

          <div class="col">
            <%-- 💥 THÊM CLASS recent-card VÀ box-shadow-hover --%>
            <div class="card h-100 recent-card box-shadow-hover">
              <a href="${ctx}/product/${item.productId}">
                <img class="card-img-top"
                     src="${empty rvCover ? (ctx.concat('/assets/img/placeholder.png')) : rvCover}"
                     alt="<c:out value='${item.productName}'/>"
                     onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
              </a>
              <div class="card-body p-2">
                <%-- 💥 Thêm hiệu ứng cho tên sản phẩm --%>
                <div class="fw-semibold text-truncate recent-name" title="${item.productName}">
                  <c:out value="${item.productName}"/>
                </div>

                <%-- Giá VNĐ + rút gọn k/triệu (GIỮ NGUYÊN) --%>
                <c:set var="pMain" value="${not empty item.discountPrice ? item.discountPrice : item.price}"/>
                <div class="fw-bold recent-price">
                  <fmt:formatNumber value="${pMain}" type="number" maxFractionDigits="0"/> ₫
                  <span class="text-muted small recent-unit">
                    (
                    <c:choose>
                      <c:when test="${pMain >= 1000000}">
                        <fmt:formatNumber value="${pMain / 1000000.0}" maxFractionDigits="1"/> triệu
                      </c:when>
                      <c:otherwise>
                        <fmt:formatNumber value="${pMain / 1000.0}" maxFractionDigits="0"/>k
                      </c:otherwise>
                    </c:choose>
                    )
                  </span>
                </div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:when>

    <%-- Fallback (GIỮ NGUYÊN) --%>
    <c:when test="${not empty recentProducts}">
      <div class="row row-cols-2 row-cols-md-3 row-cols-lg-6 g-3 recent-grid">
        <c:forEach var="p" items="${recentProducts}">
          <div class="col">
            <%-- 💥 THÊM CLASS recent-card VÀ box-shadow-hover --%>
            <div class="card h-100 recent-card box-shadow-hover">
              <a href="${ctx}/product/${p.productId}">
                <img class="card-img-top"
                     src="${ctx}/assets/img/placeholder.png"
                     alt="<c:out value='${p.productName}'/>"
                     onerror="this.onerror=null;this.src='${ctx}/assets/img/placeholder.png';">
              </a>
              <div class="card-body p-2">
                <%-- 💥 Thêm hiệu ứng cho tên sản phẩm --%>
                <div class="fw-semibold text-truncate recent-name" title="${p.productName}">
                  <c:out value="${p.productName}"/>
                </div>

                <%-- Giá VNĐ + rút gọn k/triệu (GIỮ NGUYÊN) --%>
                <c:set var="pMain2" value="${not empty p.discountPrice ? p.discountPrice : p.price}"/>
                <div class="fw-bold recent-price">
                  <fmt:formatNumber value="${pMain2}" type="number" maxFractionDigits="0"/> ₫
                  <span class="text-muted small recent-unit">
                    (
                    <c:choose>
                      <c:when test="${pMain2 >= 1000000}">
                        <fmt:formatNumber value="${pMain2 / 1000000.0}" maxFractionDigits="1"/> triệu
                      </c:when>
                      <c:otherwise>
                        <fmt:formatNumber value="${pMain2 / 1000.0}" maxFractionDigits="0"/>k
                      </c:otherwise>
                    </c:choose>
                    )
                  </span>
                </div>
              </div>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:when>

    <c:otherwise>
      <%-- Empty state (THÊM HIỆU ỨNG FADE-IN) --%>
      <div class="border rounded p-4 bg-light-subtle shadow-sm fade-in-element">
        <div class="d-flex align-items-center gap-3">
          <div class="rounded bg-white border d-flex align-items-center justify-content-center glow-icon" style="width:64px;height:64px;">
            <span class="text-muted" style="font-size:2rem;">
              <i class="bi bi-hourglass-split"></i>
            </span>
          </div>
          <div>
            <div class="fw-bold text-primary">Chưa có lịch sử đã xem</div>
            <div class="text-muted small">Hãy duyệt vài sản phẩm — chúng sẽ xuất hiện ở đây để bạn mở lại nhanh.</div>
          </div>
        </div>
      </div>
    </c:otherwise>
  </c:choose>
</div>