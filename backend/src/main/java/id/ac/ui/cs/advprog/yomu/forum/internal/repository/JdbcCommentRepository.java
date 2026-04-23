package id.ac.ui.cs.advprog.yomu.forum.internal.repository;

import id.ac.ui.cs.advprog.yomu.forum.internal.model.Comment;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Repository
public class JdbcCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcCommentRepository(@Qualifier("forumJdbcTemplate") JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    void createTableIfNeeded() {
        jdbcTemplate.execute("""
            CREATE TABLE IF NOT EXISTS comments (
                id VARCHAR(36) PRIMARY KEY,
                user_id VARCHAR(255) NOT NULL,
                bacaan_id VARCHAR(255) NOT NULL,
                content TEXT NOT NULL,
                created_at TIMESTAMP NOT NULL
            )
            """);
    }

    @Override
    public Comment save(Comment comment) {
        if (comment.getId() == null || comment.getId().isBlank()) {
            comment.setId(UUID.randomUUID().toString());
        }
        if (comment.getCreatedAt() == null) {
            comment.setCreatedAt(LocalDateTime.now());
        }

        jdbcTemplate.update(
            "INSERT INTO comments (id, user_id, bacaan_id, content, created_at) VALUES (?, ?, ?, ?, ?)",
            comment.getId(),
            comment.getUserId(),
            comment.getBacaanId(),
            comment.getContent(),
            Timestamp.valueOf(comment.getCreatedAt())
        );
        return comment;
    }
}



