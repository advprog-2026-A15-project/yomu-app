package id.ac.ui.cs.advprog.yomu.achievements.internal.service;

import id.ac.ui.cs.advprog.yomu.achievements.AchievementUnlockedEvent;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.ClaimRewardResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.CreateAchievementRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class AchievementServiceImpl implements AchievementService {

    private static final AchievementMetric DEFAULT_METRIC = AchievementMetric.READING_COMPLETED;

    private final AchievementRepository achievementRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Autowired
    public AchievementServiceImpl(
        AchievementRepository achievementRepository,
        ApplicationEventPublisher eventPublisher
    ) {
        this(achievementRepository, eventPublisher, Clock.systemUTC());
    }

    AchievementServiceImpl(
        AchievementRepository achievementRepository,
        ApplicationEventPublisher eventPublisher,
        Clock clock
    ) {
        this.achievementRepository = achievementRepository;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public AchievementResponse createAchievement(CreateAchievementRequest request) {
        String code = normalizeCode(request.code(), request.name());
        if (achievementRepository.existsByAchievementCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Achievement code sudah digunakan");
        }

        Achievement achievement = new Achievement(
            UUID.randomUUID(),
            code,
            request.name().trim(),
            normalizeDescription(request.description()),
            request.metric() == null ? DEFAULT_METRIC : request.metric(),
            request.milestone(),
            true,
            clock.instant()
        );

        return toAchievementResponse(achievementRepository.saveAchievement(achievement));
    }

    @Override
    public List<AchievementProgressResponse> listAchievementProgress(UUID userId) {
        return achievementRepository.findAchievementProgressForUser(userId).stream()
            .map(this::toAchievementProgressResponse)
            .toList();
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public DailyMissionResponse createDailyMission(CreateDailyMissionRequest request) {
        LocalDate activeFrom = request.activeFrom() == null ? LocalDate.now(clock) : request.activeFrom();
        LocalDate activeUntil = request.activeUntil() == null ? activeFrom.plusDays(1) : request.activeUntil();
        validateDailyMissionWindow(activeFrom, activeUntil);

        String code = normalizeCode(request.code(), request.name());
        if (achievementRepository.existsByDailyMissionCode(code)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Daily mission code sudah digunakan");
        }

        DailyMission mission = new DailyMission(
            UUID.randomUUID(),
            code,
            request.name().trim(),
            normalizeDescription(request.description()),
            request.metric() == null ? DEFAULT_METRIC : request.metric(),
            request.targetCount(),
            request.rewardPoints() == null ? 0 : request.rewardPoints(),
            activeFrom,
            activeUntil,
            clock.instant()
        );

        return toDailyMissionResponse(achievementRepository.saveDailyMission(mission));
    }

    @Override
    public List<DailyMissionProgressResponse> listActiveDailyMissions(UUID userId) {
        return achievementRepository
            .findActiveDailyMissionProgressForUser(userId, LocalDate.now(clock))
            .stream()
            .map(this::toDailyMissionProgressResponse)
            .toList();
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public ClaimRewardResponse claimDailyMissionReward(UUID missionId, UUID userId) {
        DailyMission mission = achievementRepository.findDailyMissionById(missionId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Daily mission tidak ditemukan"));
        DailyMissionProgressState state = achievementRepository
            .findDailyMissionProgressState(userId, missionId)
            .orElse(new DailyMissionProgressState(0, null));

        if (state.progressCount() < mission.targetCount()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Daily mission belum selesai");
        }
        if (state.claimedAt() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Reward sudah diklaim");
        }

        Instant claimedAt = clock.instant();
        achievementRepository.saveDailyMissionProgress(userId, missionId, state.progressCount(), claimedAt);
        return new ClaimRewardResponse(missionId, userId, mission.rewardPoints(), true, claimedAt);
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public void recordReadingCompleted(LearningCompletedEvent event) {
        if (event == null) {
            return;
        }
        String sourceId = event.bacaanId() == null ? null : event.bacaanId().toString();
        recordActivity(event.userId(), AchievementMetric.READING_COMPLETED, sourceId, event.occurredAt());
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public void recordQuizCompleted(QuizCompletedEvent event) {
        if (event == null) {
            return;
        }
        String sourceId = event.quizId() == null ? null : event.quizId().toString();
        recordActivity(event.userId(), AchievementMetric.QUIZ_COMPLETED, sourceId, event.occurredAt());
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public void recordLeagueActivity(LeagueActivityEvent event) {
        if (event == null) {
            return;
        }
        String sourceId = event.activityId() != null
            ? event.activityId().toString()
            : buildLeagueFallbackSourceId(event);
        recordActivity(event.userId(), AchievementMetric.LEAGUE_ACTIVITY, sourceId, event.occurredAt());
    }

    @Override
    @Transactional(transactionManager = "achievementsTransactionManager")
    public void rotateDailyMissions() {
        LocalDate today = LocalDate.now(clock);
        if (achievementRepository.hasActiveDailyMissionOn(today)) {
            return;
        }

        String dateSuffix = DateTimeFormatter.BASIC_ISO_DATE.format(today);
        DailyMission mission = new DailyMission(
            UUID.randomUUID(),
            "DAILY_READ_" + dateSuffix,
            "Daily Reading Mission",
            "Menyelesaikan satu bacaan hari ini.",
            AchievementMetric.READING_COMPLETED,
            1,
            10,
            today,
            today.plusDays(1),
            clock.instant()
        );

        if (!achievementRepository.existsByDailyMissionCode(mission.code())) {
            achievementRepository.saveDailyMission(mission);
        }
    }

    private void recordActivity(UUID userId, AchievementMetric metric, String sourceId, Instant occurredAt) {
        if (userId == null || metric == null) {
            return;
        }

        Instant timestamp = occurredAt == null ? clock.instant() : occurredAt;
        if (!achievementRepository.saveActivityEvent(userId, metric, sourceId, timestamp)) {
            return;
        }

        unlockMatchingAchievements(userId, metric, timestamp);
        updateMatchingDailyMissions(userId, metric, timestamp);
    }

    private String buildLeagueFallbackSourceId(LeagueActivityEvent event) {
        if (event.leagueId() == null && event.activityType() == null) {
            return null;
        }
        String league = event.leagueId() == null ? "unknown-league" : event.leagueId().toString();
        String activityType = event.activityType() == null ? "activity" : event.activityType();
        String occurredAt = event.occurredAt() == null ? clock.instant().toString() : event.occurredAt().toString();
        return league + ":" + activityType + ":" + occurredAt;
    }

    private void unlockMatchingAchievements(UUID userId, AchievementMetric metric, Instant occurredAt) {
        for (Achievement achievement : achievementRepository.findActiveAchievementsByMetric(metric)) {
            AchievementProgressState current = achievementRepository
                .findAchievementProgressState(userId, achievement.id())
                .orElse(new AchievementProgressState(0, null));

            if (current.unlockedAt() != null) {
                continue;
            }

            int nextProgress = Math.min(achievement.milestone(), current.progressCount() + 1);
            Instant unlockedAt = nextProgress >= achievement.milestone() ? occurredAt : null;
            achievementRepository.saveAchievementProgress(userId, achievement.id(), nextProgress, unlockedAt);

            if (unlockedAt != null) {
                eventPublisher.publishEvent(new AchievementUnlockedEvent(
                    userId,
                    achievement.code(),
                    achievement.name(),
                    unlockedAt
                ));
            }
        }
    }

    private void updateMatchingDailyMissions(UUID userId, AchievementMetric metric, Instant occurredAt) {
        LocalDate activeOn = LocalDate.ofInstant(occurredAt, clock.getZone());
        for (DailyMission mission : achievementRepository.findActiveDailyMissionsByMetric(metric, activeOn)) {
            DailyMissionProgressState current = achievementRepository
                .findDailyMissionProgressState(userId, mission.id())
                .orElse(new DailyMissionProgressState(0, null));

            int nextProgress = Math.min(mission.targetCount(), current.progressCount() + 1);
            achievementRepository.saveDailyMissionProgress(userId, mission.id(), nextProgress, current.claimedAt());
        }
    }

    private AchievementResponse toAchievementResponse(Achievement achievement) {
        return new AchievementResponse(
            achievement.id(),
            achievement.code(),
            achievement.name(),
            achievement.description(),
            achievement.metric(),
            achievement.milestone(),
            achievement.active(),
            achievement.createdAt()
        );
    }

    private AchievementProgressResponse toAchievementProgressResponse(AchievementProgress progress) {
        Achievement achievement = progress.achievement();
        return new AchievementProgressResponse(
            achievement.id(),
            achievement.code(),
            achievement.name(),
            achievement.description(),
            achievement.metric(),
            achievement.milestone(),
            progress.progressCount(),
            progress.unlocked(),
            progress.unlockedAt()
        );
    }

    private DailyMissionResponse toDailyMissionResponse(DailyMission mission) {
        return new DailyMissionResponse(
            mission.id(),
            mission.code(),
            mission.name(),
            mission.description(),
            mission.metric(),
            mission.targetCount(),
            mission.rewardPoints(),
            mission.activeFrom(),
            mission.activeUntil(),
            mission.createdAt()
        );
    }

    private DailyMissionProgressResponse toDailyMissionProgressResponse(DailyMissionProgress progress) {
        DailyMission mission = progress.mission();
        return new DailyMissionProgressResponse(
            mission.id(),
            mission.code(),
            mission.name(),
            mission.description(),
            mission.metric(),
            mission.targetCount(),
            progress.progressCount(),
            mission.rewardPoints(),
            mission.activeFrom(),
            mission.activeUntil(),
            progress.completed(),
            progress.claimed(),
            progress.claimedAt()
        );
    }

    private void validateDailyMissionWindow(LocalDate activeFrom, LocalDate activeUntil) {
        if (!activeUntil.isAfter(activeFrom)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "activeUntil harus setelah activeFrom");
        }
    }

    private String normalizeDescription(String description) {
        return description == null ? "" : description.trim();
    }

    private String normalizeCode(String requestedCode, String fallbackName) {
        String rawCode = requestedCode == null || requestedCode.isBlank() ? fallbackName : requestedCode;
        String normalized = rawCode.trim()
            .toUpperCase(Locale.ROOT)
            .replaceAll("[^A-Z0-9]+", "_")
            .replaceAll("^_+|_+$", "");
        return normalized.isBlank() ? "ACHIEVEMENT_" + UUID.randomUUID() : normalized;
    }
}
