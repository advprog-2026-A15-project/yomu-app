package id.ac.ui.cs.advprog.yomu.forum;

import java.time.Instant;

/**
 * Event publik yang dilempar saat komentar baru dibuat.
 * Modul lain (seperti gamification) bisa mendengarkan event ini.
 */
public record CommentCreatedEvent(
        String userId,
        String bacaanId,
        String commentId,
        String commentContent,
        Instant timestamp
) {
}