package acs.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.io.InputStream;

@SpringBootTest
public class AccessControlServiceTest {

    @Autowired
    private DataSource dataSource;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // 1. 禁用外键检查
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0");
            
            // 2. 执行初始化脚本
            InputStream initScript = getClass().getResourceAsStream("/db/init-test-data.sql");
            String script = new String(initScript.readAllBytes());
            stmt.execute(script);
            
            // 3. 重新启用外键检查（实际在脚本中已处理）
        }
    }

    @Test
    void testAccessControl() {
        // 测试逻辑
    }
}