package vn.iotstar.controllers.guest;

import vn.iotstar.services.ProductBrowseService;
import vn.iotstar.entities.Product;
import vn.iotstar.entities.Category;

// + thêm import
import vn.iotstar.services.FavoriteService;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.ArrayList;
// reflection để không phụ thuộc compile-time vào ViewedService
import java.lang.reflect.Method;
import java.util.Enumeration;

//+ ADD
import vn.iotstar.entities.Shop;
import vn.iotstar.services.StatisticService; // dùng hàm findShopById(...) mà bạn đang có

/**
 * Lưu ý cập nhật:
 * - Bổ sung nạp dữ liệu Review/Comment:
 *   (1) Ưu tiên gọi trực tiếp ProductBrowseService.reviewStats/reviews/userReview/canReview (nếu có)
 *   (2) Fallback bằng reflection như cũ để an toàn
 * - Nếu thiếu service tương ứng -> đặt mặc định an toàn, không làm vỡ trang.
 *
 * ĐÃ THÊM DEBUG:
 * - Dump toàn bộ session attributes, chỉ ra key nào có thể chứa user và cách lấy userId.
 * - Log giá trị canReview ở từng nhánh.
 *
 * BẢN VÁ: thêm alias attribute (rvStats/rvList/myReview/canWriteReview/commentList)
 * để tương thích JSP cũ nên review luôn hiển thị khi vào lại chi tiết.
 */
public class ProductBrowseServlet extends HttpServlet {
    private final ProductBrowseService svc = new ProductBrowseService();

    /** Giới hạn lớn để coi như “lấy tất cả” review mà không cần thay đổi SQL ở Service */
    private static final int RV_ALL_LIMIT = 1000;
    private final StatisticService stats = new StatisticService();
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String sp = req.getServletPath(); // "/products" hoặc "/product"
        if ("/products".equals(sp)) list(req, resp);
        else detail(req, resp);
    }

    private void list(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int page = parseInt(req.getParameter("page"), 1);
        int size = parseInt(req.getParameter("size"), 20);

        Long shopId = parseLongObj(req.getParameter("shopId"));
        
        if (shopId != null) {
            Shop shop = stats.findShopById(shopId);
            if (shop != null) {
                req.setAttribute("shop", shop);
                // (optional) đặt tiêu đề trang cho đẹp
                req.setAttribute("pageTitle", "Sản phẩm - " + shop.getShopName());
            }
        }

        // Lấy các tham số filter từ request
        String brand    = req.getParameter("brand");
        String gender   = req.getParameter("gender");
        String style    = req.getParameter("style");
        String province = req.getParameter("province");   // NEW: Tham số tỉnh/thành

        // Gọi API page mới (có 4 tham số filter thuộc tính)
        var result = svc.page(
                req.getParameter("q"),
                parseLongObj(req.getParameter("catId")),
                parseBD(req.getParameter("minPrice")),
                parseBD(req.getParameter("maxPrice")),
                parseIntObj(req.getParameter("minRating")),
                shopId,
                brand, gender, style,
                province,                                  // NEW: Truyền province vào Service
                req.getParameter("sort"),
                page, size
        );

        String shopQ = req.getParameter("shopQ");
        var shops = svc.shops(shopQ);
        List<Category> cats = svc.categories();

        req.setAttribute("pageTitle", "Sản phẩm");
        req.setAttribute("page", result);
        req.setAttribute("categories", cats);
        
        // Gắn lại các tham số filter vào request để giữ trạng thái trên form/link
        req.setAttribute("shopId", shopId);
        req.setAttribute("shops", shops);
        req.setAttribute("shopQ", shopQ);
        
        req.setAttribute("brand", brand);
        req.setAttribute("gender", gender);
        req.setAttribute("style", style);
        req.setAttribute("province", province);           // NEW: Gắn province

        req.getRequestDispatcher("/WEB-INF/views/products/list.jsp").forward(req, resp);
    }

    private void detail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo(); // "/{id}"
        Long id = (path == null || path.length() < 2) ? null : parseLongObj(path.substring(1));
        if (id == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        Product p = svc.findById(id);
        if (p == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        // ✅ set bối cảnh: có đến từ trang đơn hàng không?
        boolean fromOrder = "order".equalsIgnoreCase(req.getParameter("from"));
        req.setAttribute("fromOrder", fromOrder);

        // ====== [DEBUG: dump session] ======
        HttpSession sess = req.getSession(false);
        String sessionDump = dumpSessionForDebug(sess);
        req.setAttribute("debugSessionKeys", sessionDump);
        // ===================================

        // ====== [VIEWED HOOK + LOG] ======
        try {
            Long userId = extractUserIdFromSession(req.getSession(false));
            System.out.println("[DBG] extractUserIdFromSession -> " + userId);

            if (userId != null) {
                // ✅ báo cho JSP biết là đã đăng nhập
                req.setAttribute("loggedIn", Boolean.TRUE);

                System.out.println("[Viewed] userId=" + userId + " -> productId=" + p.getProductId());
                Class<?> clazz = Class.forName("vn.iotstar.services.ViewedService");
                Object viewedSvc = clazz.getDeclaredConstructor().newInstance();

                // touch(userId, productId)
                Method touch = clazz.getMethod("touch", Long.class, Long.class);
                touch.invoke(viewedSvc, userId, p.getProductId());
                System.out.println("[Viewed] touch() done");

                // recentByUser(userId, limit) -> List<Product>
                Method recentByUser = clazz.getMethod("recentByUser", Long.class, Integer.class);
                @SuppressWarnings("unchecked")
                // lấy đúng 6, và KHÔNG loại sản phẩm hiện tại để hiển thị đủ 6
                List<Product> recent = (List<Product>) recentByUser.invoke(viewedSvc, userId, Integer.valueOf(6));

                if (recent != null && !recent.isEmpty()) {
                    // Convert -> VM có đủ field JSP cần
                    List<RecentViewVM> vms = new ArrayList<>(recent.size());
                    for (Product pr : recent) {
                        if (pr == null) continue;
                        String thumb = resolveThumbnailUrl(pr);
                        vms.add(new RecentViewVM(
                                pr.getProductId(),
                                pr.getProductName(),
                                thumb,
                                pr.getPrice(),
                                pr.getDiscountPrice()
                        ));
                    }
                    req.setAttribute("recentViewed", vms);
                    req.setAttribute("viewedDebug", "OK — recent=" + vms.size());
                    System.out.println("[Viewed] recentByUser size=" + vms.size());
                } else {
                    req.setAttribute("viewedDebug", "OK — recent=0");
                    System.out.println("[Viewed] recentByUser empty");
                }

                // ====== [FAVORITE HOOK] ======
                try {
                    FavoriteService favSvc = new FavoriteService();
                    boolean isFav = favSvc.isFav(userId, p.getProductId());
                    long favCount = favSvc.countByProduct(p.getProductId());
                    req.setAttribute("isFav", isFav);
                    req.setAttribute("favoriteCount", favCount);
                    System.out.println("[Favorite] isFav=" + isFav + " count=" + favCount);
                } catch (Exception e) {
                    System.out.println("[Favorite] ERROR: " + e.getMessage());
                    req.setAttribute("isFav", false);
                    req.setAttribute("favoriteCount", 0L);
                }
                // ====== [END FAVORITE HOOK] ======

                // ====== [REVIEW HOOK - user logged in] ======
                loadReviewsAndComments(req, p.getProductId(), userId);
                // ====== [END REVIEW HOOK] ======

            } else {
                // ✅ báo cho JSP biết là chưa đăng nhập
                req.setAttribute("loggedIn", Boolean.FALSE);

                req.setAttribute("viewedDebug", "SKIP — no userId in session");
                System.out.println("[Viewed] SKIP — no userId in session");

                // user chưa đăng nhập vẫn cần count
                try {
                    FavoriteService favSvc = new FavoriteService();
                    long favCount = favSvc.countByProduct(p.getProductId());
                    req.setAttribute("isFav", false);
                    req.setAttribute("favoriteCount", favCount);
                } catch (Exception e) {
                    req.setAttribute("isFav", false);
                    req.setAttribute("favoriteCount", 0L);
                }

                // ====== [REVIEW HOOK - guest] ======
                loadReviewsAndComments(req, p.getProductId(), null);
                // ====== [END REVIEW HOOK] ======
            }
        } catch (ClassNotFoundException e) {
            req.setAttribute("viewedDebug", "SKIP — ViewedService not found");
            System.out.println("[Viewed] SKIP — ViewedService class not found");

            // vẫn nạp review/comment dù thiếu ViewedService
            loadReviewsAndComments(req, p.getProductId(), extractUserIdFromSession(req.getSession(false)));
        } catch (Exception e) {
            req.setAttribute("viewedDebug", "ERROR — " + e.getClass().getSimpleName() + ": " + e.getMessage());
            System.out.println("[Viewed] ERROR — " + e.getMessage());
            log("[ProductBrowseServlet] Viewed hook error: " + e.getMessage());

            // vẫn nạp review/comment nếu có lỗi Viewed
            loadReviewsAndComments(req, p.getProductId(), extractUserIdFromSession(req.getSession(false)));
        }
        // ====== [END VIEWED HOOK + LOG] ======


        // ====== [VARIANT & ATTRIBUTES HOOK] ======
        try {
            // Khai báo full package name vì chưa import
            vn.iotstar.services.ProductVariantService varSvc = new vn.iotstar.services.ProductVariantService();
            vn.iotstar.services.ShoeAttributeService attrSvc = new vn.iotstar.services.ShoeAttributeService();

            Long pid = p.getProductId();

            // 1) Danh sách biến thể
            java.util.List<vn.iotstar.entities.ProductVariant> variants = varSvc.findByProductId(pid);
            req.setAttribute("variants", variants);

            // 2) colorGroups: color -> list<size>
            java.util.Map<String, java.util.List<String>> colorGroups = varSvc.colorToSizes(pid);
            req.setAttribute("colorGroups", colorGroups);

            // 3) variantStock & variantIdByKey
            java.util.Map<String, Integer> variantStock = new java.util.HashMap<>();
            java.util.Map<String, Long> variantIdByKey = varSvc.mapVariantIdByKey(pid);
            for (java.util.Map.Entry<String, Long> e : variantIdByKey.entrySet()) {
                String key = e.getKey(); // "color|size"
                String[] parts = key.split("\\|", -1);
                String color = parts.length > 0 ? parts[0] : "";
                String size  = parts.length > 1 ? parts[1] : "";
                // Gọi stockOf với color và size đã tách
                Integer st = varSvc.stockOf(pid, color, size);
                variantStock.put(key, st != null ? st : 0);
            }
            req.setAttribute("variantStock", variantStock);
            req.setAttribute("variantIdByKey", variantIdByKey);

            // 4) Thuộc tính sản phẩm (brand/material/gender/style)
            // Đảm bảo entity ShoeAttribute đã được định nghĩa
            vn.iotstar.entities.ShoeAttribute attrs = attrSvc.findByProductId(pid);
            req.setAttribute("attrs", attrs);

        } catch (Exception ex) {
            System.out.println("[Variant] ERROR — " + ex.getMessage());
            // đặt default an toàn nếu cần
            req.setAttribute("variants", new java.util.ArrayList<>());
            req.setAttribute("colorGroups", new java.util.HashMap<>());
            req.setAttribute("variantStock", new java.util.HashMap<>());
            req.setAttribute("variantIdByKey", new java.util.HashMap<>());
            req.setAttribute("attrs", null);
        }
        // ====== [END VARIANT & ATTRIBUTES HOOK] ======


        req.setAttribute("pageTitle", p.getProductName());
        req.setAttribute("product", p);
        req.setAttribute("images", svc.imagesOf(p));
        req.getRequestDispatcher("/WEB-INF/views/products/detail.jsp").forward(req, resp);
    }

    /** Nạp reviewStats/reviews/userReview/canReview/comments.
     *  Bước 1: cố gắng dùng trực tiếp ProductBrowseService (nhanh, type-safe).
     *  Bước 2: fallback reflection như cũ để an toàn nếu method không tồn tại.
     *  Bước 3 (mới): nếu stats.count>0 mà list rỗng -> thử thêm nhiều tên hàm phổ biến trong ReviewService.
     *  Bước 4 (mới): nếu có userReview mà list chưa chứa -> chèn vào đầu danh sách. */
    private void loadReviewsAndComments(HttpServletRequest req, Long productId, Long userId) {
        // ==== Defaults trước để trang luôn chạy ====
        SimpleStats defaultStats = new SimpleStats(0.0, 0L);
        req.setAttribute("reviewStats", defaultStats);
        req.setAttribute("rvStats", defaultStats);
        req.setAttribute("reviews", new ArrayList<>());
        req.setAttribute("rvList", new ArrayList<>());
        req.setAttribute("userReview", null);
        req.setAttribute("myReview", null);
        req.setAttribute("canReview", Boolean.FALSE);
        req.setAttribute("canWriteReview", Boolean.FALSE);
        req.setAttribute("comments", new ArrayList<>());
        req.setAttribute("commentList", new ArrayList<>());

        boolean filledFromSvc = false;
        SimpleStats statsHolder = defaultStats;
        List<?> listHolder = null;
        Object myReview = null;

        // ======= B1: dùng trực tiếp ProductBrowseService nếu có =======
        try {
            var st = svc.reviewStats(productId);
            if (st != null) {
                statsHolder = new SimpleStats(st.getAvg(), st.getCount());
                req.setAttribute("reviewStats", statsHolder);
                req.setAttribute("rvStats", statsHolder); // alias cho JSP cũ
            }
            // ⬇️ Lấy "tất cả": dùng giới hạn lớn
            var rv = svc.reviews(productId, RV_ALL_LIMIT);
            if (rv != null) {
                listHolder = rv;
                req.setAttribute("reviews", rv);
                req.setAttribute("rvList", rv); // alias
            }

            if (userId != null) {
                myReview = svc.userReview(productId, userId);
                req.setAttribute("userReview", myReview);
                req.setAttribute("myReview", myReview); // alias
                boolean can = svc.canReview(productId, userId);
                req.setAttribute("canReview", can);
                req.setAttribute("canWriteReview", can); // alias
                System.out.println("[DBG] canReview (svc) userId=" + userId + " productId=" + productId + " -> " + can);
            } else {
                req.setAttribute("canReview", Boolean.FALSE);
                req.setAttribute("canWriteReview", Boolean.FALSE);
                System.out.println("[DBG] canReview (svc) skipped: userId is null");
            }

            filledFromSvc = true;
        } catch (Throwable ignore) {
            // Tiếp tục B2 (reflection) nếu có lỗi/no method
        }

        if (!filledFromSvc) {
            // ======= B2: fallback reflection (giữ như trước) =======
            try {
                Class<?> rCls = Class.forName("vn.iotstar.services.ReviewService");
                Object rSvc = rCls.getDeclaredConstructor().newInstance();

                // stats(productId)
                try {
                    Method mStats = rCls.getMethod("stats", Long.class);
                    Object statsObj = mStats.invoke(rSvc, productId);
                    double avg = readDouble(statsObj, "getAvg", "getAverage", "avg");
                    long cnt   = readLong(statsObj, "getCount", "getTotal", "count");
                    statsHolder = new SimpleStats(avg, cnt);
                    req.setAttribute("reviewStats", statsHolder);
                    req.setAttribute("rvStats", statsHolder); // alias
                } catch (NoSuchMethodException ns) {
                    try {
                        Method mList = rCls.getMethod("list", Long.class, Integer.class);
                        Object list = mList.invoke(rSvc, productId, Integer.valueOf(RV_ALL_LIMIT));
                        if (list instanceof List) {
                            int c = ((List<?>) list).size();
                            statsHolder = new SimpleStats(0.0, c);
                            req.setAttribute("reviewStats", statsHolder);
                            req.setAttribute("rvStats", statsHolder);
                        }
                    } catch (Exception ignore) {}
                }

                // reviews(productId, limit)
                try {
                    Method mList = rCls.getMethod("list", Long.class, Integer.class);
                    @SuppressWarnings("unchecked")
                    List<?> rvList = (List<?>) mList.invoke(rSvc, productId, Integer.valueOf(RV_ALL_LIMIT));
                    if (rvList != null) {
                        listHolder = rvList;
                        req.setAttribute("reviews", rvList);
                        req.setAttribute("rvList", rvList); // alias
                    }
                } catch (NoSuchMethodException ns) {
                    try {
                        Method mList2 = rCls.getMethod("findLatest", Long.class, Integer.class);
                        @SuppressWarnings("unchecked")
                        List<?> rvList2 = (List<?>) mList2.invoke(rSvc, productId, Integer.valueOf(RV_ALL_LIMIT));
                        if (rvList2 != null) {
                            listHolder = rvList2;
                            req.setAttribute("reviews", rvList2);
                            req.setAttribute("rvList", rvList2); // alias
                        }
                    } catch (Exception ignore) {}
                }

                // userReview(productId, userId)
                if (userId != null) {
                    try {
                        Method mMy = rCls.getMethod("findByUser", Long.class, Long.class);
                        myReview = mMy.invoke(rSvc, productId, userId);
                        req.setAttribute("userReview", myReview);
                        req.setAttribute("myReview", myReview); // alias
                    } catch (Exception ignore) {}
                }

                // canReview(productId, userId)
                try {
                    boolean can = false;
                    if (userId != null) {
                        try {
                            Method mCan = rCls.getMethod("canReview", Long.class, Long.class);
                            Object r = mCan.invoke(rSvc, productId, userId);
                            can = toBool(r);
                        } catch (NoSuchMethodException miss) {
                            // nếu service không có, default: có login là cho review
                            can = true;
                        }
                    }
                    req.setAttribute("canReview", can);
                    req.setAttribute("canWriteReview", can); // alias
                    System.out.println("[DBG] canReview (reflect) userId=" + userId + " productId=" + productId + " -> " + can);
                } catch (Exception ignore) {}

            } catch (ClassNotFoundException notFound) {
                System.out.println("[Review] SKIP — ReviewService not found");
                // giữ defaults
            } catch (Exception e) {
                System.out.println("[Review] ERROR — " + e.getMessage());
                // giữ defaults
            }
        }

        // ======= B3 (mới): stats>0 nhưng list rỗng -> thử thêm nhiều tên hàm =======
        long cntStats = (statsHolder != null) ? statsHolder.getCount() : 0L;
        boolean needMore = (cntStats > 0) && (listHolder == null || listHolder.isEmpty());
        if (needMore) {
            try {
                Class<?> rCls = Class.forName("vn.iotstar.services.ReviewService");
                Object rSvc = rCls.getDeclaredConstructor().newInstance();
                String[] candidates = new String[]{
                        "findAllByProduct", "findByProduct", "listAll",
                        "findApproved", "approvedByProduct", "allByProduct"
                };
                for (String name : candidates) {
                    try {
                        // ưu tiên chữ ký (Long, Integer)
                        try {
                            Method m = rCls.getMethod(name, Long.class, Integer.class);
                            @SuppressWarnings("unchecked")
                            List<?> l = (List<?>) m.invoke(rSvc, productId, Integer.valueOf(RV_ALL_LIMIT));
                            if (l != null && !l.isEmpty()) { listHolder = l; break; }
                        } catch (NoSuchMethodException ignore) {
                            // thử (Long) duy nhất
                            try {
                                Method m2 = rCls.getMethod(name, Long.class);
                                @SuppressWarnings("unchecked")
                                List<?> l2 = (List<?>) m2.invoke(rSvc, productId);
                                if (l2 != null && !l2.isEmpty()) { listHolder = l2; break; }
                            } catch (NoSuchMethodException ignore2) {}
                        }
                    } catch (Exception ignore) {}
                }
            } catch (Exception ignore) {}
        }

        // ======= B4 (mới): nếu có myReview mà list chưa chứa -> chèn vào đầu =======
        if (myReview != null) {
            try {
                Long myUid = reflectId(myReview);
                boolean found = false;
                if (listHolder != null) {
                    for (Object it : listHolder) {
                        Long u = reflectId(it);
                        if (u != null && myUid != null && u.longValue() == myUid.longValue()) { found = true; break; }
                    }
                }
                if (!found) {
                    List<Object> merged = new ArrayList<>();
                    merged.add(myReview);
                    if (listHolder != null) merged.addAll((List<?>) listHolder);
                    listHolder = merged;
                }
            } catch (Exception ignore) {}
        }

        // set lại attributes nếu đã có list
        if (listHolder != null) {
            req.setAttribute("reviews", listHolder);
            req.setAttribute("rvList", listHolder);
        }

        // ---- CommentService (reflection) ----
        tryLoadCommentsByReflection(req, productId);
    }

    private void tryLoadCommentsByReflection(HttpServletRequest req, Long productId) {
        try {
            Class<?> cCls = Class.forName("vn.iotstar.services.CommentService");
            Object cSvc = cCls.getDeclaredConstructor().newInstance();

            try {
                Method mList = cCls.getMethod("list", Long.class, Integer.class);
                @SuppressWarnings("unchecked")
                List<?> cmList = (List<?>) mList.invoke(cSvc, productId, Integer.valueOf(10));
                if (cmList != null) {
                    req.setAttribute("comments", cmList);
                    req.setAttribute("commentList", cmList); // alias
                }
            } catch (NoSuchMethodException ns) {
                try {
                    Method mList2 = cCls.getMethod("findLatest", Long.class, Integer.class);
                    @SuppressWarnings("unchecked")
                    List<?> cmList2 = (List<?>) mList2.invoke(cSvc, productId, Integer.valueOf(10));
                    if (cmList2 != null) {
                        req.setAttribute("comments", cmList2);
                        req.setAttribute("commentList", cmList2); // alias
                    }
                } catch (Exception ignore) {}
            }

        } catch (ClassNotFoundException notFound) {
            System.out.println("[Comment] SKIP — CommentService not found");
        } catch (Exception e) {
            System.out.println("[Comment] ERROR — " + e.getMessage());
        }
    }

    /** Nhỏ gọn: model tổng hợp cho reviewStats (JSP: reviewStats.avg / reviewStats.count) */
    public static class SimpleStats {
        private final double avg;
        private final long count;
        public SimpleStats(double avg, long count){ this.avg = avg; this.count = count; }
        public double getAvg(){ return avg; }
        public long getCount(){ return count; }
    }

    /**
     * Lấy URL thumbnail cho 1 Product:
     * - Nếu svc.imagesOf trả List<String>: lấy phần tử đầu
     * - Nếu trả object có getIsThumbnail()/getImageUrl(): ưu tiên isThumbnail, fallback ảnh đầu
     * - Nếu không có ảnh: trả ""
     */
    private String resolveThumbnailUrl(Product pr) {
        try {
            List<?> imgs = svc.imagesOf(pr);
            if (imgs == null || imgs.isEmpty()) return "";

            Object prefer = null;
            for (Object o : imgs) {
                if (o == null) continue;
                if (o instanceof String) { // List<String>
                    prefer = o;
                    break;
                }
                try {
                    Method mIsThumb = o.getClass().getMethod("getIsThumbnail");
                    Object v = mIsThumb.invoke(o);
                    if (v instanceof Boolean && (Boolean) v) {
                        prefer = o;
                        break;
                    }
                } catch (NoSuchMethodException ignore) {
                } catch (Exception ignore) {}
                if (prefer == null) prefer = o;
            }

            String raw;
            if (prefer instanceof String) {
                raw = (String) prefer;
            } else {
                try {
                    Method mUrl = prefer.getClass().getMethod("getImageUrl");
                    Object url = mUrl.invoke(prefer);
                    raw = (url != null) ? url.toString() : "";
                } catch (Exception e) {
                    Object first = imgs.get(0);
                    raw = (first instanceof String) ? (String) first : "";
                }
            }

            if (raw == null) raw = "";
            return raw.replace("/assset/", "/assets/");
        } catch (Exception e) {
            return "";
        }
    }

    // ViewModel nhỏ cho JSP
    public static class RecentViewVM {
        private final Long productId;
        private final String productName;
        private final String coverUrl;
        private final BigDecimal price;
        private final BigDecimal discountPrice;

        public RecentViewVM(Long productId, String productName, String coverUrl,
                            BigDecimal price, BigDecimal discountPrice) {
            this.productId     = productId;
            this.productName   = (productName == null) ? "" : productName;
            this.coverUrl      = (coverUrl   == null) ? "" : coverUrl;
            this.price         = price;
            this.discountPrice = discountPrice;
        }
        public Long getProductId()         { return productId; }
        public String getProductName()     { return productName; }
        public String getCoverUrl()        { return coverUrl; }
        public BigDecimal getPrice()       { return price; }
        public BigDecimal getDiscountPrice(){ return discountPrice; }
    }

    // helpers
    private int parseInt(String s, int d){ try { return Integer.parseInt(s);}catch(Exception e){return d;}}
    private Integer parseIntObj(String s){ try { return (s==null||s.isBlank())?null:Integer.valueOf(s);}catch(Exception e){return null;}}
    private Long parseLongObj(String s){ try { return (s==null||s.isBlank())?null:Long.valueOf(s);}catch(Exception e){return null;}}
    private BigDecimal parseBD(String s){ try { return (s==null||s.isBlank())?null:new BigDecimal(s);}catch(Exception e){return null;}}

    private Long extractUserIdFromSession(HttpSession session){
        if (session == null) return null;

        // 1) thử trực tiếp userId rời rạc
        Object direct = safeGet(session, "userId");
        Long id = castToLong(direct);
        if (id != null) {
            System.out.println("[DBG] userId found in session attribute 'userId' -> " + id);
            return id;
        }

        // 2) thử qua các key phổ biến (user object)
        String[] keys = new String[]{"currentUser","loginUser","user","account","customer","authUser"};
        for (String k : keys) {
            Object v = safeGet(session, k);
            id = reflectId(v);
            if (id != null) {
                System.out.println("[DBG] userId found via key '" + k + "' -> " + id);
                return id;
            }
        }

        // 3) quét toàn bộ để tìm
        try {
            Enumeration<String> names = session.getAttributeNames();
            while (names.hasMoreElements()) {
                String name = names.nextElement();
                Object v = session.getAttribute(name);
                id = castToLong(v);
                if (id != null) {
                    System.out.println("[DBG] userId found by scanning key '" + name + "' (primitive) -> " + id);
                    return id;
                }
                id = reflectId(v);
                if (id != null) {
                    System.out.println("[DBG] userId found by scanning key '" + name + "' (object.getUserId()/getId) -> " + id);
                    return id;
                }
            }
        } catch (Exception ignore) {}
        return null;
    }

    private String dumpSessionForDebug(HttpSession s){
        StringBuilder sb = new StringBuilder();
        try {
            if (s == null) { sb.append("NO SESSION"); return sb.toString(); }
            sb.append("SESSION DUMP:\n");
            Enumeration<String> names = s.getAttributeNames();
            while (names.hasMoreElements()){
                String name = names.nextElement();
                Object v = s.getAttribute(name);
                String cls = (v==null) ? "null" : v.getClass().getName();
                Long idByGetter = reflectId(v);
                Long idAsNumber = castToLong(v);
                sb.append(" - ").append(name)
                  .append(" : ").append(cls)
                  .append(" ; toLong=").append(idAsNumber)
                  .append(" ; viaGetter=").append(idByGetter)
                  .append("\n");
            }
        } catch (Exception e){
            sb.append("ERROR dump: ").append(e.getMessage());
        }
        System.out.println("[DBG] " + sb.toString());
        return sb.toString();
    }

    private Object safeGet(HttpSession s, String key){
        try { return s.getAttribute(key); } catch (Exception e){ return null; }
    }
    private Long castToLong(Object v){
        try {
            if (v instanceof Long) return (Long) v;
            if (v instanceof Integer) return ((Integer) v).longValue();
        } catch (Exception ignore){}
        return null;
    }
    private Long reflectId(Object obj){
        if (obj == null) return null;
        try {
            Method m = obj.getClass().getMethod("getUserId");
            Object r = m.invoke(obj);
            Long id = castToLong(r);
            if (id != null) return id;
        } catch (NoSuchMethodException ignore) {
            try {
                Method m2 = obj.getClass().getMethod("getId");
                Object r2 = m2.invoke(obj);
                Long id2 = castToLong(r2);
                if (id2 != null) return id2;
            } catch (Exception ignore2) {}
        } catch (Exception ignore) {}
        return null;
    }

    // tiny reflect helpers
    private static boolean toBool(Object o){
        if (o == null) return false;
        if (o instanceof Boolean) return (Boolean)o;
        if (o instanceof Number) return ((Number)o).intValue()!=0;
        return "true".equalsIgnoreCase(String.valueOf(o));
    }
    private static double readDouble(Object obj, String... getters){
        for (String g : getters){
            try {
                Method m = obj.getClass().getMethod(g);
                Object v = m.invoke(obj);
                if (v instanceof Number) return ((Number)v).doubleValue();
                return Double.parseDouble(String.valueOf(v));
            } catch(Exception ignore){}
        }
        return 0.0;
    }
    private static long readLong(Object obj, String... getters){
        for (String g : getters){
            try {
                Method m = obj.getClass().getMethod(g);
                Object v = m.invoke(obj);
                if (v instanceof Number) return ((Number)v).longValue();
                return Long.parseLong(String.valueOf(v));
            } catch(Exception ignore){}
        }
        return 0L;
    }
}
