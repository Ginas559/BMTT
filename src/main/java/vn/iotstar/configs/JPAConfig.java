// filepath: src/main/java/vn/iotstar/configs/JPAConfig.java
package vn.iotstar.configs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import io.github.cdimascio.dotenv.Dotenv;

public class JPAConfig {

  private static final EntityManagerFactory emf;

  static {
    try {
      Dotenv dotenv = Dotenv.configure()
          .ignoreIfMalformed()
          .ignoreIfMissing()
          .load();

      // ... (toàn bộ code trong khối static giữ nguyên)
      // 1) Resolve URL/USER/PASS...
      String url  = firstNonEmpty(
          env("DB_URL"), dot(dotenv, "DB_URL"),
          env("SPRING_DATASOURCE_URL"), dot(dotenv, "SPRING_DATASOURCE_URL"),
          env("JDBC_DATABASE_URL"), dot(dotenv, "JDBC_DATABASE_URL"),
          env("DATABASE_URL"), dot(dotenv, "DATABASE_URL"),
          System.getProperty("DB_URL")
      );
      String user = firstNonEmpty(
          env("DB_USER"), dot(dotenv, "DB_USER"),
          env("SPRING_DATASOURCE_USERNAME"), dot(dotenv, "SPRING_DATASOURCE_USERNAME"),
          env("JDBC_DATABASE_USERNAME"), dot(dotenv, "JDBC_DATABASE_USERNAME"),
          System.getProperty("DB_USER")
      );
      String pass = firstNonEmpty(
          env("DB_PASS"), dot(dotenv, "DB_PASS"),
          env("SPRING_DATASOURCE_PASSWORD"), dot(dotenv, "SPRING_DATASOURCE_PASSWORD"),
          env("JDBC_DATABASE_PASSWORD"), dot(dotenv, "JDBC_DATABASE_PASSWORD"),
          System.getProperty("DB_PASS")
      );

      // 2) Các option Hibernate...
      String dialect = firstNonEmpty(
          env("HIBERNATE_DIALECT"), dot(dotenv, "HIBERNATE_DIALECT"),
          "org.hibernate.dialect.PostgreSQLDialect"
      );
      String hbm2ddl = firstNonEmpty(
          env("HBM2DDL"), dot(dotenv, "HBM2DDL"),
          env("SPRING_JPA_HIBERNATE_DDL_AUTO"), dot(dotenv, "SPRING_JPA_HIBERNATE_DDL_AUTO"),
          "update"
      );
      String showSql = firstNonEmpty(
          env("HIBERNATE_SHOW_SQL"), dot(dotenv, "HIBERNATE_SHOW_SQL"),
          env("SPRING_JPA_SHOW_SQL"), dot(dotenv, "SPRING_JPA_SHOW_SQL"),
          "false"
      );
      String fmtSql = firstNonEmpty(
          env("HIBERNATE_FORMAT_SQL"), dot(dotenv, "HIBERNATE_FORMAT_SQL"),
          "true"
      );

      // 3) Log kiểm tra key...
      presence("DB_URL","SPRING_DATASOURCE_URL","JDBC_DATABASE_URL","DATABASE_URL");
      presence("DB_USER","SPRING_DATASOURCE_USERNAME","JDBC_DATABASE_USERNAME");
      presence("DB_PASS","SPRING_DATASOURCE_PASSWORD","JDBC_DATABASE_PASSWORD");

      if (isBlank(url) || isBlank(user) || isBlank(pass)) {
        throw new IllegalStateException(
            "Thiếu thông tin DB. Cần 1 trong các bộ key sau:\n" +
            " - (DB_URL, DB_USER, DB_PASS)\n" +
            " - (SPRING_DATASOURCE_URL, SPRING_DATASOURCE_USERNAME, SPRING_DATASOURCE_PASSWORD)\n" +
            " - (JDBC_DATABASE_URL, JDBC_DATABASE_USERNAME, JDBC_DATABASE_PASSWORD)\n" +
            "Ví dụ Postgres: jdbc:postgresql://<host>:5432/<db>?sslmode=require"
        );
      }

      // 4) Load driver & test kết nối...
      try {
        // Postgres driver; nếu bạn dùng DB khác, đổi tên class tương ứng
        Class.forName("org.postgresql.Driver");
      } catch (ClassNotFoundException e) {
        System.err.println("❌ Không tìm thấy driver org.postgresql.Driver. Kiểm tra pom.xml có postgresql:42.x chưa?");
        throw e;
      }

      // Test connection 3s để bắt lỗi URL/SSL/quyền sớm
      try (Connection c = tryConnect(url, user, pass, 3)) {
        if (c == null) {
          throw new RuntimeException("Không mở được kết nối test (null). Kiểm tra URL/USER/PASS/sslmode.");
        }
      }

      // 5) Tạo EMF
      Map<String,Object> props = new HashMap<>();
      props.put("jakarta.persistence.jdbc.url", url);
      props.put("jakarta.persistence.jdbc.user", user);
      props.put("jakarta.persistence.jdbc.password", pass);
      props.put("hibernate.dialect", dialect);
      props.put("hibernate.hbm2ddl.auto", hbm2ddl);
      props.put("hibernate.show_sql", showSql);
      props.put("hibernate.format_sql", fmtSql);

      System.out.println("[JPAConfig] Using URL=" + url);
      System.out.println("[JPAConfig] User=" + user);
      System.out.println("[JPAConfig] Dialect=" + dialect + ", hbm2ddl=" + hbm2ddl +
                         ", show_sql=" + showSql + ", format_sql=" + fmtSql);

      emf = Persistence.createEntityManagerFactory("dataSource", props);

    } catch (Exception ex) {
      System.err.println("❌ Lỗi khi khởi tạo EntityManagerFactory (JPAConfig): " + ex.getClass().getName() + " - " + ex.getMessage());
      ex.printStackTrace();
      throw new ExceptionInInitializerError(ex);
    }
  }

  public static EntityManager getEntityManager() {
    return emf.createEntityManager();
  }

  /**
   * // <-- THÊM VÀO
   * Lấy EntityManagerFactory (cần cho B2)
   */ // <-- THÊM VÀO
  public static EntityManagerFactory getEntityManagerFactory() { // <-- THÊM VÀO
    return emf; // <-- THÊM VÀO
  }

  // ===== Helpers =====
  // ... (toàn bộ helper functions giữ nguyên)
  private static String env(String k){ try{ String v=System.getenv(k); return isBlank(v)?null:v.trim(); }catch(Throwable t){return null;}}
  private static String dot(Dotenv d,String k){ try{ if(d==null)return null; String v=d.get(k); return isBlank(v)?null:v.trim(); }catch(Throwable t){return null;}}
  private static String firstNonEmpty(String... xs){ if(xs==null)return null; for(String s:xs){ if(!isBlank(s)) return s; } return null; }
  private static boolean isBlank(String s){ return s==null || s.trim().isEmpty(); }

  private static void presence(String... keys){
    StringBuilder sb=new StringBuilder("[JPAConfig] ENV presence: ");
    for(String k:keys){ sb.append(k).append("=").append(System.getenv(k)!=null?"✓":"×").append("  "); }
    System.out.println(sb.toString());
  }

  private static Connection tryConnect(String url, String user, String pass, int timeoutSeconds) throws Exception {
    // Postgres tôn trọng connectTimeout param; nếu không có, driver có timeout mặc định
    // Có thể thêm ?connectTimeout=3&sslmode=require vào URL nếu cần.
    return DriverManager.getConnection(url, user, pass);
  }
}