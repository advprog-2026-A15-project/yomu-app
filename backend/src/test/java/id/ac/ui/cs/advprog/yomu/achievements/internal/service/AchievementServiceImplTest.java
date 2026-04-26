package id.ac.ui.cs.advprog.yomu.achievements.internal.service;

import id.ac.ui.cs.advprog.yomu.achievements.AchievementUnlockedEvent;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.ClaimRewardResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.CreateDailyMissionRequest;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.Achievement;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementMetric;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementProgress;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementProgressState;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.DailyMission;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.DailyMissionProgress;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.DailyMissionProgressState;
import id.ac.ui.cs.advprog.yomu.achievements.internal.repository.AchievementRepository;
import id.ac.ui.cs.advprog.yomu.clan.LeagueActivityEvent;
import id.ac.ui.cs.advprog.yomu.learning.LearningCompletedEvent;
import id.ac.ui.cs.advprog.yomu.learning.QuizCompletedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AchievementServiceImplTest {

    private static final Instant NOW = Instant.parse("2026-04-26T10:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 4, 26);

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private InMemoryAchievementRepository achievementRepository;
    private AchievementServiceImpl achievementService;

    @BeforeEach
    void setUp() {
        achievementRepository = new InMemoryAchievementRepository();
        achievementService = new AchievementServiceImpl(
            achievementRepository,
            eventPublisher,
            Clock.fixed(NOW, ZoneOffset.UTC)
        );
        seedAchievement("FIRST_READ", "First Read", AchievementMetric.QUIZ_COMPLETED, 1);
        seedAchievement("TEN_READS", "10 Bacaan Selesai", AchievementMetric.READING_COMPLETED, 10);
        seedAchievement("LEAGUE_STARTER", "League Starter", AchievementMetric.LEAGUE_ACTIVITY, 1);
    }

    @Test
    void quizCompletedUnlocksFirstReadAchievement() {
        UUID userId = UUID.randomUUID();
        UUID quizId = UUID.randomUUID();

        achievementService.recordQuizCompleted(new QuizCompletedEvent(
            userId,
            quizId,
            "intro-quiz",
            85,
            NOW
        ));

        assertThat(achievementService.listAchievementProgress(userId))
            .filteredOn(response -> response.code().equals("FIRST_READ"))
            .singleElement()
            .satisfies(response -> {
                assertThat(response.progress()).isEqualTo(1);
                assertThat(response.unlocked()).isTrue();
                assertThat(response.unlockedAt()).isEqualTo(NOW);
            });
        verify(eventPublisher).publishEvent(new AchievementUnlockedEvent(
            userId,
            "FIRST_READ",
            "First Read",
            NOW
        ));
    }

    @Test
    void leagueActivityUnlocksLeagueAchievement() {
        UUID userId = UUID.randomUUID();
        UUID activityId = UUID.randomUUID();

        achievementService.recordLeagueActivity(new LeagueActivityEvent(
            userId,
            UUID.randomUUID(),
            activityId,
            "WEEKLY_POINT_GAINED",
            NOW
        ));

        assertThat(achievementService.listAchievementProgress(userId))
            .filteredOn(response -> response.code().equals("LEAGUE_STARTER"))
            .singleElement()
            .satisfies(response -> {
                assertThat(response.progress()).isEqualTo(1);
                assertThat(response.unlocked()).isTrue();
            });
        verify(eventPublisher).publishEvent(new AchievementUnlockedEvent(
            userId,
            "LEAGUE_STARTER",
            "League Starter",
            NOW
        ));
    }

    @Test
    void repeatedReadingEventForSameBacaanDoesNotDoubleCountProgress() {
        UUID userId = UUID.randomUUID();
        UUID bacaanId = UUID.randomUUID();
        LearningCompletedEvent event = new LearningCompletedEvent(userId, bacaanId, "intro", NOW);

        achievementService.recordReadingCompleted(event);
        achievementService.recordReadingCompleted(event);

        assertThat(achievementService.listAchievementProgress(userId))
            .filteredOn(response -> response.code().equals("TEN_READS"))
            .singleElement()
            .satisfies(response -> {
                assertThat(response.progress()).isEqualTo(1);
                assertThat(response.unlocked()).isFalse();
            });
    }

    @Test
    void readingCompletedProgressesActiveDailyMissionAndAllowsRewardClaim() {
        UUID userId = UUID.randomUUID();
        DailyMissionResponse mission = achievementService.createDailyMission(new CreateDailyMissionRequest(
            "DAILY_READ",
            "Baca Hari Ini",
            "Selesaikan satu bacaan",
            AchievementMetric.READING_COMPLETED,
            1,
            20,
            TODAY,
            TODAY.plusDays(1)
        ));

        achievementService.recordReadingCompleted(new LearningCompletedEvent(
            userId,
            UUID.randomUUID(),
            "daily-read",
            NOW
        ));

        List<DailyMissionProgressResponse> activeMissions = achievementService.listActiveDailyMissions(userId);
        assertThat(activeMissions).singleElement().satisfies(response -> {
            assertThat(response.missionId()).isEqualTo(mission.missionId());
            assertThat(response.progress()).isEqualTo(1);
            assertThat(response.completed()).isTrue();
            assertThat(response.claimed()).isFalse();
        });

        ClaimRewardResponse claim = achievementService.claimDailyMissionReward(mission.missionId(), userId);
        assertThat(claim.rewardPoints()).isEqualTo(20);
        assertThat(claim.claimed()).isTrue();
        assertThat(claim.claimedAt()).isEqualTo(NOW);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> achievementService.claimDailyMissionReward(mission.missionId(), userId)
        );
        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void claimDailyMissionRejectsIncompleteProgress() {
        UUID userId = UUID.randomUUID();
        DailyMissionResponse mission = achievementService.createDailyMission(new CreateDailyMissionRequest(
            "DAILY_TWO_READS",
            "Baca Dua Kali",
            "",
            AchievementMetric.READING_COMPLETED,
            2,
            30,
            TODAY,
            TODAY.plusDays(1)
        ));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> achievementService.claimDailyMissionReward(mission.missionId(), userId)
        );

        assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void rotateDailyMissionsCreatesOneDefaultMissionForCurrentDay() {
        UUID userId = UUID.randomUUID();

        achievementService.rotateDailyMissions();
        achievementService.rotateDailyMissions();

        assertThat(achievementService.listActiveDailyMissions(userId))
            .singleElement()
            .satisfies(response -> {
                assertThat(response.code()).isEqualTo("DAILY_READ_20260426");
                assertThat(response.targetCount()).isEqualTo(1);
                assertThat(response.rewardPoints()).isEqualTo(10);
            });
    }

    private void seedAchievement(String code, String name, AchievementMetric metric, int milestone) {
        achievementRepository.saveAchievement(new Achievement(
            UUID.randomUUID(),
            code,
            name,
            "",
            metric,
            milestone,
            true,
            NOW
        ));
    }

    private record UserAchievementKey(UUID userId, UUID achievementId) {
    }

    private record UserMissionKey(UUID userId, UUID missionId) {
    }

    private record ActivityKey(UUID userId, AchievementMetric metric, String sourceId) {
    }

    private static class InMemoryAchievementRepository implements AchievementRepository {
        private final Map<UUID, Achievement> achievements = new HashMap<>();
        private final Map<UUID, DailyMission> missions = new HashMap<>();
        private final Map<UserAchievementKey, AchievementProgressState> achievementProgress = new HashMap<>();
        private final Map<UserMissionKey, DailyMissionProgressState> missionProgress = new HashMap<>();
        private final Set<ActivityKey> activityEvents = new HashSet<>();

        @Override
        public Achievement saveAchievement(Achievement achievement) {
            achievements.put(achievement.id(), achievement);
            return achievement;
        }

        @Override
        public boolean existsByAchievementCode(String code) {
            return achievements.values().stream().anyMatch(achievement -> achievement.code().equals(code));
        }

        @Override
        public List<Achievement> findActiveAchievementsByMetric(AchievementMetric metric) {
            return achievements.values().stream()
                .filter(Achievement::active)
                .filter(achievement -> achievement.metric() == metric)
                .sorted(Comparator.comparing(Achievement::createdAt).thenComparing(Achievement::name))
                .toList();
        }

        @Override
        public List<AchievementProgress> findAchievementProgressForUser(UUID userId) {
            return achievements.values().stream()
                .filter(Achievement::active)
                .sorted(Comparator.comparing(Achievement::createdAt).thenComparing(Achievement::name))
                .map(achievement -> {
                    AchievementProgressState state = achievementProgress
                        .getOrDefault(new UserAchievementKey(userId, achievement.id()), new AchievementProgressState(0, null));
                    return new AchievementProgress(achievement, state.progressCount(), state.unlockedAt());
                })
                .toList();
        }

        @Override
        public Optional<AchievementProgressState> findAchievementProgressState(UUID userId, UUID achievementId) {
            return Optional.ofNullable(achievementProgress.get(new UserAchievementKey(userId, achievementId)));
        }

        @Override
        public void saveAchievementProgress(UUID userId, UUID achievementId, int progressCount, Instant unlockedAt) {
            achievementProgress.put(
                new UserAchievementKey(userId, achievementId),
                new AchievementProgressState(progressCount, unlockedAt)
            );
        }

        @Override
        public DailyMission saveDailyMission(DailyMission mission) {
            missions.put(mission.id(), mission);
            return mission;
        }

        @Override
        public boolean existsByDailyMissionCode(String code) {
            return missions.values().stream().anyMatch(mission -> mission.code().equals(code));
        }

        @Override
        public Optional<DailyMission> findDailyMissionById(UUID missionId) {
            return Optional.ofNullable(missions.get(missionId));
        }

        @Override
        public List<DailyMission> findActiveDailyMissionsByMetric(AchievementMetric metric, LocalDate activeOn) {
            return activeMissions(activeOn).stream()
                .filter(mission -> mission.metric() == metric)
                .toList();
        }

        @Override
        public List<DailyMissionProgress> findActiveDailyMissionProgressForUser(UUID userId, LocalDate activeOn) {
            List<DailyMissionProgress> result = new ArrayList<>();
            for (DailyMission mission : activeMissions(activeOn)) {
                DailyMissionProgressState state = missionProgress
                    .getOrDefault(new UserMissionKey(userId, mission.id()), new DailyMissionProgressState(0, null));
                result.add(new DailyMissionProgress(mission, state.progressCount(), state.claimedAt()));
            }
            return result;
        }

        @Override
        public boolean hasActiveDailyMissionOn(LocalDate activeOn) {
            return !activeMissions(activeOn).isEmpty();
        }

        @Override
        public Optional<DailyMissionProgressState> findDailyMissionProgressState(UUID userId, UUID missionId) {
            return Optional.ofNullable(missionProgress.get(new UserMissionKey(userId, missionId)));
        }

        @Override
        public void saveDailyMissionProgress(UUID userId, UUID missionId, int progressCount, Instant claimedAt) {
            missionProgress.put(new UserMissionKey(userId, missionId), new DailyMissionProgressState(progressCount, claimedAt));
        }

        @Override
        public boolean saveActivityEvent(UUID userId, AchievementMetric metric, String sourceId, Instant occurredAt) {
            if (sourceId == null || sourceId.isBlank()) {
                return true;
            }
            return activityEvents.add(new ActivityKey(userId, metric, sourceId));
        }

        private List<DailyMission> activeMissions(LocalDate activeOn) {
            return missions.values().stream()
                .filter(mission -> !mission.activeFrom().isAfter(activeOn))
                .filter(mission -> mission.activeUntil().isAfter(activeOn))
                .sorted(Comparator.comparing(DailyMission::createdAt).thenComparing(DailyMission::name))
                .toList();
        }
    }
}
