package id.ac.ui.cs.advprog.yomu.forum.internal.controller;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentResponse;
import id.ac.ui.cs.advprog.yomu.forum.internal.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        when(commentService.createComment("user-1", "bacaan-1", "Komentar pertama"))
            .thenReturn(new CommentCreatedEvent(
                "user-1",
                "bacaan-1",
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
                      "commentContent": "Komentar pertama"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.userId").value("user-1"))
            .andExpect(jsonPath("$.bacaanId").value("bacaan-1"))
            .andExpect(jsonPath("$.commentId").value("comment-123"))
            .andExpect(jsonPath("$.commentContent").value("Komentar pertama"))
            .andExpect(jsonPath("$.timestamp").value("2026-04-23T10:00:00Z"));
    }

    @Test
    void getCommentsReturnsJsonArrayWithExpectedFields() throws Exception {
        when(commentService.listComments(null)).thenReturn(List.of(
            new CommentResponse(
                "comment-123",
                "user-1",
                "bacaan-1",
                "Komentar pertama",
                Instant.parse("2026-04-23T10:00:00Z")
            )
        ));

        mockMvc.perform(get("/api/forum/comments"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].commentId").value("comment-123"))
            .andExpect(jsonPath("$[0].userId").value("user-1"))
            .andExpect(jsonPath("$[0].bacaanId").value("bacaan-1"))
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
}



