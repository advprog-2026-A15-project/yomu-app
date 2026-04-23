package id.ac.ui.cs.advprog.yomu.forum.internal.controller;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.CommentDeletedEvent;
import id.ac.ui.cs.advprog.yomu.forum.CommentUpdatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentResponse;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentService;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentTreeResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CommentController(commentService)).build();
    }

    @Test
    void createCommentReturnsJsonPayloadWithExpectedFields() throws Exception {
        when(commentService.createComment("user-1", "bacaan-1", "Komentar pertama", "root"))
            .thenReturn(new CommentCreatedEvent(
                "user-1",
                "bacaan-1",
                "root",
                "comment-123",
                "Komentar pertama",
                Instant.parse("2026-04-23T10:00:00Z")
            ));

        mockMvc.perform(post("/api/forum/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": "user-1",
                      "bacaanId": "bacaan-1",
                      "commentContent": "Komentar pertama",
                      "parentComment": "root"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("user-1"))
            .andExpect(jsonPath("$.bacaanId").value("bacaan-1"))
            .andExpect(jsonPath("$.parentComment").value("root"))
            .andExpect(jsonPath("$.commentId").value("comment-123"))
            .andExpect(jsonPath("$.commentContent").value("Komentar pertama"))
            .andExpect(jsonPath("$.timestamp").value("2026-04-23T10:00:00Z"));

        verify(commentService).createComment("user-1", "bacaan-1", "Komentar pertama", "root");
    }

    @Test
    void getCommentsReturnsJsonArrayWithExpectedFields() throws Exception {
        when(commentService.listComments(null)).thenReturn(List.of(
            new CommentResponse(
                "comment-123",
                "user-1",
                "bacaan-1",
                "root",
                "Komentar pertama",
                Instant.parse("2026-04-23T10:00:00Z")
            )
        ));

        mockMvc.perform(get("/api/forum/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value("comment-123"))
            .andExpect(jsonPath("$[0].userId").value("user-1"))
            .andExpect(jsonPath("$[0].bacaanId").value("bacaan-1"))
            .andExpect(jsonPath("$[0].parentComment").value("root"))
            .andExpect(jsonPath("$[0].commentContent").value("Komentar pertama"))
            .andExpect(jsonPath("$[0].timestamp").value("2026-04-23T10:00:00Z"));

        verify(commentService).listComments(null);
    }

    @Test
    void getCommentsWithBacaanIdUsesFilter() throws Exception {
        when(commentService.listComments("bacaan-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/forum/comments").queryParam("bacaanId", "bacaan-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(commentService).listComments("bacaan-1");
    }

    @Test
    void getCommentsTreeReturnsNestedJson() throws Exception {
        when(commentService.listCommentsTree(null)).thenReturn(List.of(
            new CommentTreeResponse(
                "comment-root",
                "user-1",
                "bacaan-1",
                "root",
                "Komentar induk",
                Instant.parse("2026-04-23T10:00:00Z"),
                List.of(new CommentTreeResponse(
                    "comment-reply",
                    "user-2",
                    "bacaan-1",
                    "comment-root",
                    "Komentar balasan",
                    Instant.parse("2026-04-23T10:00:01Z"),
                    List.of()
                ))
            )
        ));

        mockMvc.perform(get("/api/forum/comments/tree"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value("comment-root"))
            .andExpect(jsonPath("$[0].children[0].commentId").value("comment-reply"))
            .andExpect(jsonPath("$[0].children[0].parentComment").value("comment-root"));

        verify(commentService).listCommentsTree(null);
    }

    @Test
    void getCommentsTreeWithBacaanIdUsesFilter() throws Exception {
        when(commentService.listCommentsTree("bacaan-1")).thenReturn(List.of());

        mockMvc.perform(get("/api/forum/comments/tree").queryParam("bacaanId", "bacaan-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        verify(commentService).listCommentsTree("bacaan-1");
    }

    @Test
    void createCommentWithParentCommentReturnsReplyPayload() throws Exception {
        when(commentService.createComment("user-2", "bacaan-1", "Komentar balasan", "comment-parent-1"))
            .thenReturn(new CommentCreatedEvent(
                "user-2",
                "bacaan-1",
                "comment-parent-1",
                "comment-456",
                "Komentar balasan",
                Instant.parse("2026-04-23T10:00:01Z")
            ));

        mockMvc.perform(post("/api/forum/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "userId": "user-2",
                      "bacaanId": "bacaan-1",
                      "commentContent": "Komentar balasan",
                      "parentComment": "comment-parent-1"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.parentComment").value("comment-parent-1"))
            .andExpect(jsonPath("$.commentId").value("comment-456"));

        verify(commentService).createComment("user-2", "bacaan-1", "Komentar balasan", "comment-parent-1");
    }

    @Test
    void updateCommentReturnsUpdatedEventPayload() throws Exception {
        when(commentService.updateComment("comment-123", "Konten yang sudah diedit"))
            .thenReturn(new CommentUpdatedEvent(
                "user-1",
                "bacaan-1",
                "root",
                "comment-123",
                "Konten yang sudah diedit",
                Instant.parse("2026-04-23T10:02:00Z")
            ));

        mockMvc.perform(put("/api/forum/comments/comment-123")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "commentContent": "Konten yang sudah diedit"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value("comment-123"))
            .andExpect(jsonPath("$.commentContent").value("Konten yang sudah diedit"))
            .andExpect(jsonPath("$.timestamp").value("2026-04-23T10:02:00Z"));

        verify(commentService).updateComment("comment-123", "Konten yang sudah diedit");
    }

    @Test
    void deleteCommentReturnsDeletedEventPayload() throws Exception {
        when(commentService.deleteComment("comment-123"))
            .thenReturn(new CommentDeletedEvent(
                "user-1",
                "bacaan-1",
                "root",
                "comment-123",
                "Komentar pertama",
                Instant.parse("2026-04-23T10:03:00Z")
            ));

        mockMvc.perform(delete("/api/forum/comments/comment-123"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.commentId").value("comment-123"))
            .andExpect(jsonPath("$.commentContent").value("Komentar pertama"))
            .andExpect(jsonPath("$.timestamp").value("2026-04-23T10:03:00Z"));

        verify(commentService).deleteComment("comment-123");
    }

    @Test
    void updateCommentReturnsNotFoundWhenCommentMissing() throws Exception {
        when(commentService.updateComment("missing", "Konten baru"))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        mockMvc.perform(put("/api/forum/comments/missing")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "commentContent": "Konten baru"
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteCommentReturnsNotFoundWhenCommentMissing() throws Exception {
        when(commentService.deleteComment("missing"))
            .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        mockMvc.perform(delete("/api/forum/comments/missing"))
            .andExpect(status().isNotFound());
    }
}



