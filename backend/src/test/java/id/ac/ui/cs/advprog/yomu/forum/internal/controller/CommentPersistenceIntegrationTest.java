package id.ac.ui.cs.advprog.yomu.forum.internal.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentService;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@SuppressWarnings({"SqlResolve", "SqlNoDataSourceInspection", "SqlStatementInspection"})
class CommentPersistenceIntegrationTest {

    @Autowired
    @Qualifier("forumJdbcTemplate")
    private JdbcTemplate forumJdbcTemplate;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentController commentController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(commentController).build();
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
        assertThat(event.parentComment()).isEqualTo("root");
        assertThat(event.commentContent()).isEqualTo("Komentar tersimpan ke database");
        assertThat(event.commentId()).isNotBlank();

        Integer rowCount = forumJdbcTemplate.queryForObject("SELECT COUNT(*) FROM comments", Integer.class);
        assertThat(rowCount).isEqualTo(1);

        Map<String, Object> savedComment = forumJdbcTemplate.queryForMap(
            "SELECT id, user_id, bacaan_id, parent_comment, content, created_at FROM comments WHERE id = ?",
            event.commentId()
        );

        assertThat(savedComment.get("ID")).isEqualTo(event.commentId());
        assertThat(savedComment.get("USER_ID")).isEqualTo("user-1");
        assertThat(savedComment.get("BACAAN_ID")).isEqualTo("bacaan-1");
        assertThat(savedComment.get("PARENT_COMMENT")).isEqualTo("root");
        assertThat(savedComment.get("CONTENT")).isEqualTo("Komentar tersimpan ke database");
        assertThat(savedComment.get("CREATED_AT")).isNotNull();
    }

    @Test
    void createReplyStoresParentCommentInDatabase() {
        CommentCreatedEvent parentEvent = commentService.createComment(
            "user-parent",
            "bacaan-2",
            "Komentar induk"
        );

        CommentCreatedEvent replyEvent = commentService.createComment(
            "user-reply",
            "bacaan-2",
            "Komentar balasan",
            parentEvent.commentId()
        );

        assertThat(replyEvent.parentComment()).isEqualTo(parentEvent.commentId());

        Map<String, Object> savedReply = forumJdbcTemplate.queryForMap(
            "SELECT id, user_id, bacaan_id, parent_comment, content, created_at FROM comments WHERE id = ?",
            replyEvent.commentId()
        );

        assertThat(savedReply.get("PARENT_COMMENT")).isEqualTo(parentEvent.commentId());
        assertThat(savedReply.get("CONTENT")).isEqualTo("Komentar balasan");
    }

    @Test
    void getCommentsReturnsPersistedRows() throws Exception {
        CommentCreatedEvent event = commentService.createComment(
            "user-2",
            "bacaan-2",
            "Komentar untuk endpoint get"
        );

        mockMvc.perform(get("/api/forum/comments").queryParam("bacaanId", "bacaan-2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value(event.commentId()))
            .andExpect(jsonPath("$[0].userId").value("user-2"))
            .andExpect(jsonPath("$[0].bacaanId").value("bacaan-2"))
            .andExpect(jsonPath("$[0].parentComment").value("root"))
            .andExpect(jsonPath("$[0].commentContent").value("Komentar untuk endpoint get"))
            .andExpect(jsonPath("$[0].timestamp").exists());
    }

    @Test
    void getCommentsTreeReturnsNestedPersistedRows() throws Exception {
        CommentCreatedEvent parentEvent = commentService.createComment(
            "user-parent",
            "bacaan-tree",
            "Komentar induk tree"
        );
        CommentCreatedEvent replyEvent = commentService.createComment(
            "user-reply",
            "bacaan-tree",
            "Komentar balasan tree",
            parentEvent.commentId()
        );

        mockMvc.perform(get("/api/forum/comments/tree").queryParam("bacaanId", "bacaan-tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value(parentEvent.commentId()))
            .andExpect(jsonPath("$[0].children[0].commentId").value(replyEvent.commentId()))
            .andExpect(jsonPath("$[0].children[0].parentComment").value(parentEvent.commentId()));
    }

    @Test
    void updateCommentUpdatesStoredContent() {
        CommentCreatedEvent created = commentService.createComment(
            "user-1",
            "bacaan-edit",
            "Konten awal"
        );

        commentService.updateComment(created.commentId(), "Konten setelah edit");

        String updatedContent = forumJdbcTemplate.queryForObject(
            "SELECT content FROM comments WHERE id = ?",
            String.class,
            created.commentId()
        );
        assertThat(updatedContent).isEqualTo("Konten setelah edit");
    }

    @Test
    void deleteCommentRemovesStoredRow() {
        CommentCreatedEvent created = commentService.createComment(
            "user-1",
            "bacaan-delete",
            "Komentar untuk dihapus"
        );

        commentService.deleteComment(created.commentId());

        Integer count = forumJdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM comments WHERE id = ?",
            Integer.class,
            created.commentId()
        );
        assertThat(count).isEqualTo(0);
    }

    @Test
    void updateAndDeleteEndpointsReturnEventPayloads() throws Exception {
        CommentCreatedEvent created = commentService.createComment(
            "user-endpoint",
            "bacaan-endpoint",
            "Komentar endpoint"
        );

        mockMvc.perform(put("/api/forum/comments/{commentId}", created.commentId())
                .contentType("application/json")
                .content("""
                    {
                      "commentContent": "Komentar endpoint edit"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(created.commentId()))
            .andExpect(jsonPath("$.commentContent").value("Komentar endpoint edit"));

        mockMvc.perform(delete("/api/forum/comments/{commentId}", created.commentId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value(created.commentId()))
            .andExpect(jsonPath("$.commentContent").value("Komentar endpoint edit"));
    }
}








