package id.ac.ui.cs.advprog.yomu.forum.internal.repository;

import id.ac.ui.cs.advprog.yomu.forum.internal.model.Comment;

import java.util.List;

public interface CommentRepository {

    Comment save(Comment comment);

    List<Comment> findAll();

    List<Comment> findByBacaanId(String bacaanId);
}

