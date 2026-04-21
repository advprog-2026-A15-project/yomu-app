package id.ac.ui.cs.advprog.yomu;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

@Configuration(proxyBeanMethods = false)
public class ModuleDatabaseConfiguration {

    @Bean
    @ConfigurationProperties("yomu.modules.achievements.datasource")
    DataSourceProperties achievementsDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "achievementsDataSource")
    HikariDataSource achievementsDataSource(
        @Qualifier("achievementsDataSourceProperties") DataSourceProperties properties
    ) {
        return buildDataSource(properties);
    }

    @Bean(name = "achievementsTransactionManager")
    DataSourceTransactionManager achievementsTransactionManager(
        @Qualifier("achievementsDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "achievementsJdbcTemplate")
    JdbcTemplate achievementsJdbcTemplate(
        @Qualifier("achievementsDataSource") DataSource dataSource
    ) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("yomu.modules.auth.datasource")
    DataSourceProperties authDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "authDataSource")
    HikariDataSource authDataSource(
        @Qualifier("authDataSourceProperties") DataSourceProperties properties
    ) {
        return buildDataSource(properties);
    }

    @Bean(name = "authTransactionManager")
    DataSourceTransactionManager authTransactionManager(
        @Qualifier("authDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "authJdbcTemplate")
    JdbcTemplate authJdbcTemplate(@Qualifier("authDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("yomu.modules.clan.datasource")
    DataSourceProperties clanDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "clanDataSource")
    HikariDataSource clanDataSource(
        @Qualifier("clanDataSourceProperties") DataSourceProperties properties
    ) {
        return buildDataSource(properties);
    }

    @Bean(name = "clanTransactionManager")
    DataSourceTransactionManager clanTransactionManager(
        @Qualifier("clanDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "clanJdbcTemplate")
    JdbcTemplate clanJdbcTemplate(@Qualifier("clanDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("yomu.modules.forum.datasource")
    DataSourceProperties forumDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "forumDataSource")
    HikariDataSource forumDataSource(
        @Qualifier("forumDataSourceProperties") DataSourceProperties properties
    ) {
        return buildDataSource(properties);
    }

    @Bean(name = "forumTransactionManager")
    DataSourceTransactionManager forumTransactionManager(
        @Qualifier("forumDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "forumJdbcTemplate")
    JdbcTemplate forumJdbcTemplate(@Qualifier("forumDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    @ConfigurationProperties("yomu.modules.learning.datasource")
    DataSourceProperties learningDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "learningDataSource")
    HikariDataSource learningDataSource(
        @Qualifier("learningDataSourceProperties") DataSourceProperties properties
    ) {
        return buildDataSource(properties);
    }

    @Bean(name = "learningTransactionManager")
    DataSourceTransactionManager learningTransactionManager(
        @Qualifier("learningDataSource") DataSource dataSource
    ) {
        return new DataSourceTransactionManager(dataSource);
    }

    @Bean(name = "learningJdbcTemplate")
    JdbcTemplate learningJdbcTemplate(@Qualifier("learningDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private HikariDataSource buildDataSource(DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder()
            .type(HikariDataSource.class)
            .build();
    }
}
