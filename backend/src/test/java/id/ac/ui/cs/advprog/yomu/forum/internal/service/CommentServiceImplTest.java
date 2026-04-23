package id.ac.ui.cs.advprog.yomu.forum.internal.service;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.model.Comment;
import id.ac.ui.cs.advprog.yomu.forum.internal.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
            "comment-123",
            "Komentar pertama",
            Instant.parse("2026-04-23T10:00:00Z")
        ));

        ArgumentCaptor<Comment> commentCaptor = ArgumentCaptor.forClass(Comment.class);
        verify(commentRepository).save(commentCaptor.capture());
        assertThat(commentCaptor.getValue().getCreatedAt()).isEqualTo(LocalDateTime.ofInstant(clock.instant(), clock.getZone()));
        verify(eventPublisher).publishEvent(result);
    }
}

