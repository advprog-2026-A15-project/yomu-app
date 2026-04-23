package id.ac.ui.cs.advprog.yomu.forum.internal.service;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.model.Comment;
import id.ac.ui.cs.advprog.yomu.forum.internal.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private CommentServiceImpl commentService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-04-23T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        commentService = new CommentServiceImpl(commentRepository, eventPublisher, clock);
    }

    @Test
    void createCommentReturnsAndPublishesCommentCreatedEvent() {
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId("comment-123");
            return comment;
        });

        CommentCreatedEvent result = commentService.createComment("user-1", "bacaan-1", "Komentar pertama");

        assertThat(result).isEqualTo(new CommentCreatedEvent(
            "user-1",
            "bacaan-1",
            "root",
            "comment-123",
            "Komentar pertama",
            Instant.parse("2026-04-23T10:00:00Z")
        ));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        assertThat(commentCaptor.getValue().getParentComment()).isEqualTo("root");
        verify(eventPublisher).publishEvent(result);
    }

    @Test
    void createCommentWithParentCommentPersistsReplyRelationship() {
        when(commentRepository.findById("comment-parent-1")).thenReturn(java.util.Optional.of(
            new Comment("user-parent", "bacaan-1", "root", "Komentar induk")
        ));
        when(commentRepository.save(any(Comment.class))).thenAnswer(invocation -> {
            Comment comment = invocation.getArgument(0);
            comment.setId("comment-456");
            return comment;
        });

        CommentCreatedEvent result = commentService.createComment(
            "user-2",
            "bacaan-1",
            "Komentar balasan",
            "comment-parent-1"
        );

        assertThat(result).isEqualTo(new CommentCreatedEvent(
            "user-2",
            "bacaan-1",
            "comment-parent-1",
            "comment-456",
            "Komentar balasan",
            Instant.parse("2026-04-23T10:00:00Z")
        ));
        verify(commentRepository).findById("comment-parent-1");
    }

    @Test
    void createCommentRejectsParentCommentFromDifferentBacaan() {
        when(commentRepository.findById("comment-parent-1")).thenReturn(java.util.Optional.of(
            new Comment("user-parent", "bacaan-other", "root", "Komentar induk")
        ));

        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
            ResponseStatusException.class,
            () -> commentService.createComment("user-2", "bacaan-1", "Komentar balasan", "comment-parent-1")
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void listCommentsWithoutFilterReturnsMappedResponses() {
        Comment storedComment = new Comment("user-1", "bacaan-1", "Komentar pertama");
        storedComment.setId("comment-123");
        storedComment.setCreatedAt(LocalDateTime.of(2026, 4, 23, 10, 0));
        storedComment.setParentComment("root");

        when(commentRepository.findAll()).thenReturn(List.of(storedComment));

        List<CommentResponse> result = commentService.listComments(null);

        assertThat(result).containsExactly(new CommentResponse(
            "comment-123",
            "user-1",
            "bacaan-1",
            "root",
            "Komentar pertama",
            Instant.parse("2026-04-23T10:00:00Z")
        ));
        verify(commentRepository).findAll();
    }

    @Test
    void listCommentsWithBacaanIdUsesRepositoryFilter() {
        when(commentRepository.findByBacaanId("bacaan-1")).thenReturn(List.of());

        List<CommentResponse> result = commentService.listComments("bacaan-1");

        assertThat(result).isEmpty();
        verify(commentRepository).findByBacaanId("bacaan-1");
    }

    @Test
    void listCommentsIncludesReplyParentComment() {
        Comment reply = new Comment("user-2", "bacaan-1", "comment-parent-1", "Komentar balasan");
        reply.setId("comment-456");
        reply.setCreatedAt(LocalDateTime.of(2026, 4, 23, 10, 0));

        when(commentRepository.findAll()).thenReturn(List.of(reply));

        List<CommentResponse> result = commentService.listComments(null);

        assertThat(result).containsExactly(new CommentResponse(
            "comment-456",
            "user-2",
            "bacaan-1",
            "comment-parent-1",
            "Komentar balasan",
            Instant.parse("2026-04-23T10:00:00Z")
        ));
    }
}

