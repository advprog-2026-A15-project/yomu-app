package id.ac.ui.cs.advprog.yomu.forum.internal.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection", "SqlStatementInspection"})
class CommentPersistenceIntegrationTest {

    @Autowired
    @Qualifier("forumJdbcTemplate")
    private JdbcTemplate forumJdbcTemplate;

    @Autowired
    private CommentService commentService;

    @BeforeEach
    void setUp() {
        forumJdbcTemplate.update("DELETE FROM comments WHERE created_at IS NOT NULL");
    }

    @Test
    void createCommentStoresCommentInDatabase() {
        CommentCreatedEvent event = commentService.createComment(
            "user-1",
            "bacaan-1",
            "Komentar tersimpan ke database"
        );

        assertThat(event.userId()).isEqualTo("user-1");
        assertThat(event.bacaanId()).isEqualTo("bacaan-1");
        assertThat(event.commentContent()).isEqualTo("Komentar tersimpan ke database");
        assertThat(event.commentId()).isNotBlank();

        Integer rowCount = forumJdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments", Integer.class);
        assertThat(rowCount).isEqualTo(1);

        Map<String, Object> savedComment = forumJdbcTemplate.queryForMap(
            "SELECT id, user_id, bacaan_id, content, created_at FROM comments WHERE id = ?",
            event.commentId()
        );

        assertThat(savedComment.get("ID")).isEqualTo(event.commentId());
        assertThat(savedComment.get("USER_ID")).isEqualTo("user-1");
        assertThat(savedComment.get("BACAAN_ID")).isEqualTo("bacaan-1");
        assertThat(savedComment.get("CONTENT")).isEqualTo("Komentar tersimpan ke database");
        assertThat(savedComment.get("CREATED_AT")).isNotNull();
    }
}








