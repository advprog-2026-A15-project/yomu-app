package id.ac.ui.cs.advprog.yomu;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class ModuleDatabaseConfiguration {

    private static final String ACHIEVEMENTS_SCHEMA = "ACHIEVEMENTS";
    private static final String AUTH_SCHEMA = "AUTH";
    private static final String CLAN_SCHEMA = "CLAN";
    private static final String FORUM_SCHEMA = "FORUM";
    private static final String LEARNING_SCHEMA = "LEARNING";

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    private final Object schemaInitializationLock = new Object();

    private volatile boolean schemasInitialized;

    @Bean(name = "achievementsDataSource")
    public DataSource achievementsDataSource() {
        return moduleDataSource(ACHIEVEMENTS_SCHEMA);
    }

    @Bean(name = "authDataSource")
    public DataSource authDataSource() {
        return moduleDataSource(AUTH_SCHEMA);
    }

    @Bean(name = "clanDataSource")
    public DataSource clanDataSource() {
        return moduleDataSource(CLAN_SCHEMA);
    }

    @Bean(name = "forumDataSource")
    public DataSource forumDataSource() {
        return moduleDataSource(FORUM_SCHEMA);
    }

    @Bean(name = "learningDataSource")
    public DataSource learningDataSource() {
        return moduleDataSource(LEARNING_SCHEMA);
    }

    @Bean(name = "achievementsJdbcTemplate")
    public JdbcTemplate achievementsJdbcTemplate(@Qualifier("achievementsDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "authJdbcTemplate")
    public JdbcTemplate authJdbcTemplate(@Qualifier("authDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "clanJdbcTemplate")
    public JdbcTemplate clanJdbcTemplate(@Qualifier("clanDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "forumJdbcTemplate")
    public JdbcTemplate forumJdbcTemplate(@Qualifier("forumDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = "learningJdbcTemplate")
    public JdbcTemplate learningJdbcTemplate(@Qualifier("learningDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean(name = {"transactionManager", "forumTransactionManager"})
    public PlatformTransactionManager transactionManager(@Qualifier("forumDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "achievementsTransactionManager")
    public PlatformTransactionManager achievementsTransactionManager(@Qualifier("achievementsDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "authTransactionManager")
    public PlatformTransactionManager authTransactionManager(@Qualifier("authDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "clanTransactionManager")
    public PlatformTransactionManager clanTransactionManager(@Qualifier("clanDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "learningTransactionManager")
    public PlatformTransactionManager learningTransactionManager(@Qualifier("learningDataSource") DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private DataSource moduleDataSource(String schema) {
        ensureSchemasCreated();

        HikariDataSource dataSource = DataSourceBuilder.create()
            .type(HikariDataSource.class)
            .url(jdbcUrl)
            .username(username)
            .password(password)
            .driverClassName(driverClassName)
            .build();
        dataSource.setPoolName(schema + "Pool");
        dataSource.setSchema(schema);
        return dataSource;
    }

    private void ensureSchemasCreated() {
        if (schemasInitialized) {
            return;
        }

        synchronized (schemaInitializationLock) {
            if (schemasInitialized) {
                return;
            }

            try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
                 Statement statement = connection.createStatement()) {
                statement.execute("CREATE SCHEMA IF NOT EXISTS ACHIEVEMENTS");
                statement.execute("CREATE SCHEMA IF NOT EXISTS AUTH");
                statement.execute("CREATE SCHEMA IF NOT EXISTS CLAN");
                statement.execute("CREATE SCHEMA IF NOT EXISTS FORUM");
                statement.execute("CREATE SCHEMA IF NOT EXISTS LEARNING");
                schemasInitialized = true;
            } catch (SQLException exception) {
                throw new IllegalStateException("Failed to initialize module schemas", exception);
            }
        }
    }
}



