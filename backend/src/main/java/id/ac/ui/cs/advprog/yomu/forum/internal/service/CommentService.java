package id.ac.ui.cs.advprog.yomu.forum.internal.service;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;

import java.util.List;

public interface CommentService {

	default CommentCreatedEvent createComment(String userId, String bacaanId, String commentContent) {
		return createComment(userId, bacaanId, commentContent, "root");
	}

	CommentCreatedEvent createComment(String userId, String bacaanId, String commentContent, String parentComment);

	List<CommentResponse> listComments(String bacaanId);
}
