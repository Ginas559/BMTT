package vn.iotstar.services;

import vn.iotstar.configs.VnPayConfig;

import jakarta.servlet.http.HttpServletRequest;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

public class VnPayService {

    public String createPaymentUrl(long amountVnd, String orderInfo, String baseUrl, String bankCode, HttpServletRequest req) {
        String vnpTxnRef = VnPayConfig.getRandomNumber(8);
        String vnpIpAddr = "127.0.0.1"; // Optional: lấy IP client nếu cần
        String orderType = "other";

        Map<String, String> params = new HashMap<>();
        params.put("vnp_Version", VnPayConfig.VNP_VERSION);
        params.put("vnp_Command", VnPayConfig.VNP_COMMAND);
        params.put("vnp_TmnCode", VnPayConfig.VNP_TMNCODE);
        params.put("vnp_Amount", String.valueOf(amountVnd * 100)); // VND x 100
        params.put("vnp_CurrCode", VnPayConfig.VNP_CURR_CODE);
        params.put("vnp_TxnRef", vnpTxnRef);
        params.put("vnp_OrderInfo", orderInfo);
        params.put("vnp_OrderType", orderType);
        params.put("vnp_Locale", VnPayConfig.VNP_LOCALE);

        // ✅ CHỈNH CHUẨN 1 DÒNG: thêm context path để tránh 404
        String contextPath = req.getContextPath(); // vd: /shoes-shop
        params.put("vnp_ReturnUrl", baseUrl + contextPath + "/api/payment/callback");

        params.put("vnp_IpAddr", vnpIpAddr);

        Calendar cld = Calendar.getInstance(VnPayConfig.VNP_TZ);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String createDate = formatter.format(cld.getTime());
        params.put("vnp_CreateDate", createDate);
        cld.add(Calendar.MINUTE, 15);
        String expireDate = formatter.format(cld.getTime());
        params.put("vnp_ExpireDate", expireDate);

        if (bankCode != null && !bankCode.isBlank()) {
            params.put("vnp_BankCode", bankCode);
        }

        // sort by key and build query + hash data
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();

        for (int i = 0; i < fieldNames.size(); i++) {
            String k = fieldNames.get(i);
            String v = params.get(k);
            try {
                hashData.append(k).append("=").append(URLEncoder.encode(v, "UTF-8"));
                query.append(URLEncoder.encode(k, "UTF-8")).append("=")
                     .append(URLEncoder.encode(v, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not supported", e);
            }
            if (i < fieldNames.size() - 1) {
                hashData.append("&");
                query.append("&");
            }
        }

        String secureHash = VnPayConfig.hmacSHA512(VnPayConfig.VNP_HASH_SECRET, hashData.toString());
        query.append("&vnp_SecureHash=").append(secureHash);

        return VnPayConfig.VNP_PAY_URL + "?" + query.toString();
    }

    /**
     * Xử lý callback từ VNPAY, verify chữ ký.
     * @return 1: success, 0: failed/canceled, -1: invalid signature, -2: missing hash
     */
    public int handleReturn(HttpServletRequest request) {
        Map<String, String> fields = new HashMap<>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            if (value != null && !value.isEmpty()) {
                fields.put(name, value);
            }
        }

        String vnpSecureHash         = request.getParameter("vnp_SecureHash");
        String vnpResponseCode       = request.getParameter("vnp_ResponseCode");       // RETURN URL ưu tiên
        String vnpTransactionStatus  = request.getParameter("vnp_TransactionStatus");  // Có thì xét thêm

        fields.remove("vnp_SecureHashType");
        fields.remove("vnp_SecureHash");

        // sort and rebuild for signature
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (int i = 0; i < fieldNames.size(); i++) {
            String k = fieldNames.get(i);
            String v = fields.get(k);
            try {
                hashData.append(k).append("=").append(URLEncoder.encode(v, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("UTF-8 not supported", e);
            }
            if (i < fieldNames.size() - 1) hashData.append("&");
        }

        if (vnpSecureHash == null || vnpSecureHash.isEmpty()) return -2;

        String calc = VnPayConfig.hmacSHA512(VnPayConfig.VNP_HASH_SECRET, hashData.toString());
        if (!calc.equalsIgnoreCase(vnpSecureHash)) return -1;

        // ✅ Thành công nếu vnp_ResponseCode == "00" (trên RETURN URL),
        // hoặc (nếu có) vnp_TransactionStatus == "00".
        if ("00".equals(vnpResponseCode) || "00".equals(vnpTransactionStatus)) {
            return 1;
        }
        return 0;
    }
}
