<%-- filepath: src/main/webapp/WEB-INF/views/public/favorites.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c"  uri="jakarta.tags.core"%>
<%@ taglib prefix="fn" uri="jakarta.tags.functions"%>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt"%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>

<div class="main-favorites py-4">
  <div class="fav-header">
    <h3 class="m-0 gradient-text">Yêu thích của bạn</h3>
    <div class="d-flex gap-2">
      <a class="btn btn-secondary" href="${ctx}/">Trang chủ</a>
      <a class="btn btn-primary"  href="${ctx}/recent">Đã xem gần đây</a>
    </div>
  </div>

  <c:choose>
    <c:when test="${empty favorites}">
      <div class="alert alert-info glass-alert mt-3">
        Chưa có sản phẩm yêu thích. Hãy nhấn ❤️ tại trang chi tiết sản phẩm để thêm vào danh sách.
      </div>
    </c:when>
    <c:otherwise>
      <div class="row row-cols-2 row-cols-md-6 g-3 fav-grid mt-2">
        <c:forEach var="it" items="${favorites}">
          <div class="col product-card-animation">
            <div class="card-3d-hover">
              <a class="card h-100 text-decoration-none vendor-card" href="${ctx}/product/${it.productId}">
                <%-- Tạo URL ảnh an toàn với context-path + fallback --%>
                <c:set var="rawImg" value="${empty it.imageUrl ? '/assets/img/placeholder.png' : it.imageUrl}"/>
                <c:url value="${rawImg}" var="imgUrl"/>
                
                <img class="card-img-top cover" src="${imgUrl}" alt="${fn:escapeXml(it.productName)}">

                <div class="card-body">
                  <div class="fav-title">${fn:escapeXml(it.productName)}</div>

                  <div class="price-block mt-1">
                    <c:choose>
                      <c:when test="${it.discountPrice != null}">
                        <span class="new text-danger">
                          <fmt:formatNumber value="${it.discountPrice}" type="number" pattern="#,##0"/> ₫
                        </span>
                        <span class="old">
                          <fmt:formatNumber value="${it.price}" type="number" pattern="#,##0"/> ₫
                        </span>
                      </c:when>
                      <c:otherwise>
                        <span class="new">
                          <fmt:formatNumber value="${it.price}" type="number" pattern="#,##0"/> ₫
                        </span>
                      </c:otherwise>
                    </c:choose>
                  </div>
                </div>
              </a>
            </div>
          </div>
        </c:forEach>
      </div>
    </c:otherwise>
  </c:choose>
</div>