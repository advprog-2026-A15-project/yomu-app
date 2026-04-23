package id.ac.ui.cs.advprog.yomu.forum;

import java.time.Instant;

/**
 * Event publik saat isi komentar diperbarui.
 */
public record CommentUpdatedEvent(
    String userId,
    String bacaanId,
    String parentComment,
    String commentId,
    String commentContent,
    Instant timestamp
) {
}

