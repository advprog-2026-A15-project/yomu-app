package id.ac.ui.cs.advprog.yomu.achievements.internal.listener;

import id.ac.ui.cs.advprog.yomu.achievements.internal.service.AchievementService;
import id.ac.ui.cs.advprog.yomu.clan.LeagueActivityEvent;
import id.ac.ui.cs.advprog.yomu.learning.LearningCompletedEvent;
import id.ac.ui.cs.advprog.yomu.learning.QuizCompletedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ReadingCompletedListenerTest {

    @Test
    void forwardsLearningCompletedEventToAchievementService() {
        AchievementService achievementService = mock(AchievementService.class);
        ReadingCompletedListener listener = new ReadingCompletedListener(achievementService);
        LearningCompletedEvent event = new LearningCompletedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "intro",
            Instant.parse("2026-04-26T10:00:00Z")
        );

        listener.onLearningCompleted(event);

        verify(achievementService).recordReadingCompleted(event);
    }

    @Test
    void forwardsQuizCompletedEventToAchievementService() {
        AchievementService achievementService = mock(AchievementService.class);
        ReadingCompletedListener listener = new ReadingCompletedListener(achievementService);
        QuizCompletedEvent event = new QuizCompletedEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            "intro-quiz",
            90,
            Instant.parse("2026-04-26T10:00:00Z")
        );

        listener.onQuizCompleted(event);

        verify(achievementService).recordQuizCompleted(event);
    }

    @Test
    void forwardsLeagueActivityEventToAchievementService() {
        AchievementService achievementService = mock(AchievementService.class);
        ReadingCompletedListener listener = new ReadingCompletedListener(achievementService);
        LeagueActivityEvent event = new LeagueActivityEvent(
            UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "WEEKLY_POINT_GAINED",
            Instant.parse("2026-04-26T10:00:00Z")
        );

        listener.onLeagueActivity(event);

        verify(achievementService).recordLeagueActivity(event);
    }
}
