// tung - filepath: src/main/java/vn/iotstar/utils/PasswordUtil.java
package vn.iotstar.utils;

import java.security.SecureRandom;
import java.util.Base64;
import org.mindrot.jbcrypt.BCrypt;

public final class PasswordUtil {

    private PasswordUtil(){}

    public static String generateSalt() {
        byte[] b = new byte[16];
        new SecureRandom().nextBytes(b);
        return Base64.getEncoder().encodeToString(b);
    }

    public static String hashWithBCrypt(String raw) {
        return BCrypt.hashpw(raw, BCrypt.gensalt(12));
    }

    public static boolean matches(String raw, String hashed) {
        return BCrypt.checkpw(raw, hashed);
    }
}