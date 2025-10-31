// filepath: src/main/java/vn/iotstar/utils/JwtUtil.java

package vn.iotstar.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

/**
 * Tiện ích JWT dùng cho Servlet/JSP.
 * Thuật toán: HS256 với secret đặt trong biến môi trường JWT_SECRET (có fallback dev).
 * Chỉ nhúng thông tin cần thiết: userId, role, shopId (nếu là vendor).
 * Không đưa dữ liệu nhạy cảm vào token.
 */
public final class JwtUtil {

    private static final String DEFAULT_SECRET = "dev-secret-change-this";

    private static final String ISSUER = "uteshop";

    private static final String CLAIM_UID = "uid";

    private static final String CLAIM_ROLE = "role";

    private static final String CLAIM_SHOP = "shopId";

    private static Algorithm algorithm() {
        String secret = System.getenv("JWT_SECRET");
        if (secret == null || secret.isBlank()) secret = DEFAULT_SECRET;
        return Algorithm.HMAC256(secret);
    }

    /** Tạo access token có thời hạn ttlMillis kể từ hiện tại. */
    public static String generateAccessToken(long userId, String role, Long shopId, long ttlMillis) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + ttlMillis);

        var builder = JWT.create()
                .withIssuer(ISSUER)
                .withIssuedAt(now)
                .withExpiresAt(exp)
                .withClaim(CLAIM_UID, userId)
                .withClaim(CLAIM_ROLE, role == null ? "" : role);

        if (shopId != null) {
            builder.withClaim(CLAIM_SHOP, shopId);
        }

        return builder.sign(algorithm());
    }

    /** Xác minh token, ném JWTVerificationException nếu không hợp lệ. */
    public static DecodedJWT verify(String token) throws JWTVerificationException {
        return JWT.require(algorithm())
                .withIssuer(ISSUER)
                .build()
                .verify(token);
    }

    /** Lấy userId từ token đã xác minh. */
    public static Long getUserId(DecodedJWT jwt) {
        try {
            return jwt.getClaim(CLAIM_UID).asLong();
        } catch (Exception e) {
            return null;
        }
    }

    /** Lấy role từ token đã xác minh. */
    public static String getRole(DecodedJWT jwt) {
        try {
            String r = jwt.getClaim(CLAIM_ROLE).asString();
            return r == null ? "" : r;
        } catch (Exception e) {
            return "";
        }
    }

    /** Lấy shopId (nếu có) từ token đã xác minh. */
    public static Long getShopId(DecodedJWT jwt) {
        try {
            return jwt.getClaim(CLAIM_SHOP).asLong();
        } catch (Exception e) {
            return null;
        }
    }
}