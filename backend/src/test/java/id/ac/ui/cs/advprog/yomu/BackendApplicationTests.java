package id.ac.ui.cs.advprog.yomu;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BackendApplicationTests {

    @Autowired
    @Qualifier("achievementsDataSource")
    private DataSource achievementsDataSource;

    @Autowired
    @Qualifier("authDataSource")
    private DataSource authDataSource;

    @Autowired
    @Qualifier("clanDataSource")
    private DataSource clanDataSource;

    @Autowired
    @Qualifier("forumDataSource")
    private DataSource forumDataSource;

    @Autowired
    @Qualifier("learningDataSource")
    private DataSource learningDataSource;

    @Test
    void contextLoads() {
    }

    @Test
    void eachModuleUsesItsOwnDatasource() {
        assertThat(jdbcUrl(achievementsDataSource)).contains("achievements-test");
        assertThat(jdbcUrl(authDataSource)).contains("auth-test");
        assertThat(jdbcUrl(clanDataSource)).contains("clan-test");
        assertThat(jdbcUrl(forumDataSource)).contains("forum-test");
        assertThat(jdbcUrl(learningDataSource)).contains("learning-test");
    }

    private String jdbcUrl(DataSource dataSource) {
        return ((HikariDataSource) dataSource).getJdbcUrl();
    }
}
