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
        assertThat(jdbcUrl(achievementsDataSource)).isEqualTo(jdbcUrl(authDataSource));
        assertThat(jdbcUrl(authDataSource)).isEqualTo(jdbcUrl(clanDataSource));
        assertThat(jdbcUrl(clanDataSource)).isEqualTo(jdbcUrl(forumDataSource));
        assertThat(jdbcUrl(forumDataSource)).isEqualTo(jdbcUrl(learningDataSource));

                                    assertThat(jdbcUrl(achievementsDataSource)).contains("yomutest");

            assertThat(schema(achievementsDataSource)).isEqualTo("ACHIEVEMENTS");
            assertThat(schema(authDataSource)).isEqualTo("AUTH");
            assertThat(schema(clanDataSource)).isEqualTo("CLAN");
            assertThat(schema(forumDataSource)).isEqualTo("FORUM");
            assertThat(schema(learningDataSource)).isEqualTo("LEARNING");
    }

    private String jdbcUrl(DataSource dataSource) {
        return ((HikariDataSource) dataSource).getJdbcUrl();
    }

    private String schema(DataSource dataSource) {
        return ((HikariDataSource) dataSource).getSchema();
    }
}
