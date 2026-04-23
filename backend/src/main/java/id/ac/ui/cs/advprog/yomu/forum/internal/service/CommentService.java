package id.ac.ui.cs.advprog.yomu.forum.internal.service;

import id.ac.ui.cs.advprog.yomu.forum.CommentCreatedEvent;

public interface CommentService {

	CommentCreatedEvent createComment(String userId, String bacaanId, String commentContent);
}
