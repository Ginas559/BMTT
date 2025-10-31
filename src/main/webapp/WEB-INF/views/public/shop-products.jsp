// filepath: src/main/webapp/WEB-INF/views/public/shop-products.jsp
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core"%>
<c:set var="pageTitle" value="${empty shop ? 'Sản phẩm' : shop.shopName}" />
<jsp:include page="/WEB-INF/views/products/list.jsp" />