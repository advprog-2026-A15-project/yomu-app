package id.ac.ui.cs.advprog.yomu.forum.internal.service;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;
import id.ac.ui.cs.advprog.yomu.forum.internal.model.Comment;
import id.ac.ui.cs.advprog.yomu.forum.internal.repository.CommentRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {

	private final CommentRepository commentRepository;
	private final ApplicationEventPublisher eventPublisher;
	private final Clock clock;

	public CommentServiceImpl(
		CommentRepository commentRepository,
		ApplicationEventPublisher eventPublisher,
		Clock clock
	) {
		this.commentRepository = commentRepository;
		this.eventPublisher = eventPublisher;
		this.clock = clock;
	}

	@Override
	@Transactional(transactionManager = "forumTransactionManager")
	public CommentCreatedEvent createComment(String userId, String bacaanId, String commentContent) {
		Instant timestamp = clock.instant();
		Comment comment = new Comment(userId, bacaanId, commentContent);
		comment.setCreatedAt(LocalDateTime.ofInstant(timestamp, clock.getZone()));

		Comment savedComment = commentRepository.save(comment);
		CommentCreatedEvent event = new CommentCreatedEvent(
			savedComment.getUserId(),
			savedComment.getBacaanId(),
			savedComment.getId(),
			savedComment.getContent(),
			timestamp
		);
		eventPublisher.publishEvent(event);
		return event;
	}
}
