package vn.iotstar.configs;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;
import io.github.cdimascio.dotenv.Dotenv; // Import thư viện dotenv

public class JPAConfig {

    private static final EntityManagerFactory emf;
    
    // Khối static để khởi tạo EMF một cách an toàn
    static {
        try {
            // 1. Load biến môi trường từ file .env (hoặc từ môi trường hệ thống)
            // Đảm bảo file .env của bạn được cấu hình đúng để thư viện tìm thấy
        	Dotenv dotenv = Dotenv.configure()
                    .directory("/") // Tìm kiếm từ gốc Classpath (nơi .env đang ở)
                    .filename(".env") // Tên file
                    .load();

            // 2. Tạo Map chứa các thuộc tính kết nối database
            Map<String, Object> properties = new HashMap<>();
            
            // Lấy giá trị thực tế từ biến môi trường
            properties.put("jakarta.persistence.jdbc.url", dotenv.get("DB_URL"));
            properties.put("jakarta.persistence.jdbc.user", dotenv.get("DB_USER"));
            properties.put("jakarta.persistence.jdbc.password", dotenv.get("DB_PASS"));
            
            // Các thuộc tính khác (Hibernate sẽ đọc chúng từ persistence.xml)
            // Bạn có thể thêm vào đây nếu muốn ghi đè các thuộc tính khác.
            
            // 3. Tạo EntityManagerFactory và truyền các properties đã load
            // Hibernate sẽ ưu tiên các thuộc tính được truyền qua Map này.
            emf = Persistence.createEntityManagerFactory("dataSource", properties);
            
        } catch (Exception e) {
            // In ra lỗi chi tiết để debug
            System.err.println("❌ Lỗi khi khởi tạo EntityManagerFactory: " + e.getMessage());
            e.printStackTrace();
            // Ném lại lỗi để chương trình dừng lại, cho biết lỗi là ở đây
            throw new ExceptionInInitializerError(e);
        }
    }

    public static EntityManager getEntityManager() {
        return emf.createEntityManager();
    }
}