package id.ac.ui.cs.advprog.yomu.forum;

import java.time.Instant;

/**
 * Event publik saat komentar dihapus.
 */
public record CommentDeletedEvent(
    String userId,
    String bacaanId,
    String parentComment,
    String commentId,
    String commentContent,
    Instant timestamp
) {
}

