// filepath: src/main/java/vn/iotstar/configs/JPAConfig.java
// purpose: Safe JPA bootstrap for Render: ENV-first, .env-fallback, never crash just because .env is missing.
// notes:
// - Reads DB_URL, DB_USER, DB_PASS from System ENV first (Render), then from .env if present (local), then from System properties.
// - Dialect defaults to PostgreSQLDialect but can be overridden by ENV HIBERNATE_DIALECT.
// - Optional ENV: HBM2DDL (default "update"), HIBERNATE_SHOW_SQL ("false"), HIBERNATE_FORMAT_SQL ("true").
// - persistence.xml uses PU name "dataSource" and these properties will override its placeholders if present.

package vn.iotstar.configs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class JPAConfig {

    private static final EntityManagerFactory emf;

    static {
        try {
            // 1) Load .env only if available; do not fail if missing (Render sẽ dùng ENV)
            Dotenv dotenv = Dotenv.configure()
                    .ignoreIfMalformed()
                    .ignoreIfMissing()
                    .load();

            // 2) Helper to resolve config: ENV → .env → System properties
            String url  = firstNonEmpty(
                    System.getenv("DB_URL"),
                    getenv(dotenv, "DB_URL"),
                    System.getProperty("DB_URL")
            );
            String user = firstNonEmpty(
                    System.getenv("DB_USER"),
                    getenv(dotenv, "DB_USER"),
                    System.getProperty("DB_USER")
            );
            String pass = firstNonEmpty(
                    System.getenv("DB_PASS"),
                    getenv(dotenv, "DB_PASS"),
                    System.getProperty("DB_PASS")
            );

            // Hibernate tunables
            String dialect = firstNonEmpty(
                    System.getenv("HIBERNATE_DIALECT"),
                    getenv(dotenv, "HIBERNATE_DIALECT"),
                    "org.hibernate.dialect.PostgreSQLDialect" // default cho Render Postgres
            );
            String hbm2ddl = firstNonEmpty(
                    System.getenv("HBM2DDL"),
                    getenv(dotenv, "HBM2DDL"),
                    "update"
            );
            String showSql = firstNonEmpty(
                    System.getenv("HIBERNATE_SHOW_SQL"),
                    getenv(dotenv, "HIBERNATE_SHOW_SQL"),
                    "false"
            );
            String fmtSql = firstNonEmpty(
                    System.getenv("HIBERNATE_FORMAT_SQL"),
                    getenv(dotenv, "HIBERNATE_FORMAT_SQL"),
                    "true"
            );

            // 3) Validate bắt buộc (chỉ khi thực sự thiếu thông tin kết nối)
            if (isBlank(url) || isBlank(user) || isBlank(pass)) {
                StringBuilder sb = new StringBuilder();
                sb.append("Thiếu thông tin DB. Cần thiết: DB_URL, DB_USER, DB_PASS.\n")
                  .append("Ví dụ Render ENV:\n")
                  .append("  DB_URL=jdbc:postgresql://<host>:5432/<db>?sslmode=require\n")
                  .append("  DB_USER=<username>\n")
                  .append("  DB_PASS=<password>\n");
                throw new IllegalStateException(sb.toString());
            }

            // 4) Override properties cho PU "dataSource"
            Map<String, Object> properties = new HashMap<>();
            properties.put("jakarta.persistence.jdbc.url", url);
            properties.put("jakarta.persistence.jdbc.user", user);
            properties.put("jakarta.persistence.jdbc.password", pass);
            properties.put("hibernate.dialect", dialect);
            properties.put("hibernate.hbm2ddl.auto", hbm2ddl);
            properties.put("hibernate.show_sql", showSql);
            properties.put("hibernate.format_sql", fmtSql);

            // 5) Log ngắn gọn (không lộ password)
            System.out.println("[JPAConfig] Using URL=" + url);
            System.out.println("[JPAConfig] User=" + user);
            System.out.println("[JPAConfig] Dialect=" + dialect + ", hbm2ddl=" + hbm2ddl +
                               ", show_sql=" + showSql + ", format_sql=" + fmtSql);

            // 6) Khởi tạo EMF
            emf = Persistence.createEntityManagerFactory("dataSource", properties);

        } catch (Exception e) {
            System.err.println("❌ Lỗi khi khởi tạo EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    // ===== Helpers =====
    private static String getenv(Dotenv dotenv, String key) {
        try {
            if (dotenv == null) return null;
            String v = dotenv.get(key);
            return isBlank(v) ? null : v;
        } catch (Throwable ignored) {
            return null;
        }
    }

    private static String firstNonEmpty(String... candidates) {
        if (candidates == null) return null;
        for (String c : candidates) {
            if (!isBlank(c)) return c.trim();
        }
        return null;
        }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
