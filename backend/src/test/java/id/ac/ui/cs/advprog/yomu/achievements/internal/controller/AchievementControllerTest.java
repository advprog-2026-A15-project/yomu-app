package id.ac.ui.cs.advprog.yomu.achievements.internal.controller;

import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.AchievementProgressResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.AchievementResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.ClaimRewardResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.DailyMissionProgressResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.dto.DailyMissionResponse;
import id.ac.ui.cs.advprog.yomu.achievements.internal.model.AchievementMetric;
import id.ac.ui.cs.advprog.yomu.achievements.internal.service.AchievementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AchievementControllerTest {

    @Mock
    private AchievementService achievementService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new AchievementController(achievementService)).build();
    }

    @Test
    void createAchievementReturnsCreatedDefinition() throws Exception {
        UUID achievementId = UUID.randomUUID();
        when(achievementService.createAchievement(any())).thenReturn(new AchievementResponse(
            achievementId,
            "TEN_READS",
            "10 Bacaan Selesai",
            "",
            AchievementMetric.READING_COMPLETED,
            10,
            true,
            Instant.parse("2026-04-26T10:00:00Z")
        ));

        mockMvc.perform(post("/api/achievements/admin")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "TEN_READS",
                      "name": "10 Bacaan Selesai",
                      "metric": "READING_COMPLETED",
                      "milestone": 10
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.achievementId").value(achievementId.toString()))
            .andExpect(jsonPath("$.code").value("TEN_READS"))
            .andExpect(jsonPath("$.milestone").value(10));

        verify(achievementService).createAchievement(any());
    }

    @Test
    void listAchievementsReturnsProgressForUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID achievementId = UUID.randomUUID();
        when(achievementService.listAchievementProgress(userId)).thenReturn(List.of(new AchievementProgressResponse(
            achievementId,
            "FIRST_READ",
            "First Read",
            "",
            AchievementMetric.READING_COMPLETED,
            1,
            1,
            true,
            Instant.parse("2026-04-26T10:00:00Z")
        )));

        mockMvc.perform(get("/api/achievements").queryParam("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].achievementId").value(achievementId.toString()))
            .andExpect(jsonPath("$[0].code").value("FIRST_READ"))
            .andExpect(jsonPath("$[0].unlocked").value(true));

        verify(achievementService).listAchievementProgress(userId);
    }

    @Test
    void createDailyMissionReturnsCreatedMission() throws Exception {
        UUID missionId = UUID.randomUUID();
        when(achievementService.createDailyMission(any())).thenReturn(new DailyMissionResponse(
            missionId,
            "DAILY_READ",
            "Baca Hari Ini",
            "",
            AchievementMetric.READING_COMPLETED,
            1,
            20,
            LocalDate.of(2026, 4, 26),
            LocalDate.of(2026, 4, 27),
            Instant.parse("2026-04-26T10:00:00Z")
        ));

        mockMvc.perform(post("/api/achievements/admin/daily-missions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "code": "DAILY_READ",
                      "name": "Baca Hari Ini",
                      "metric": "READING_COMPLETED",
                      "targetCount": 1,
                      "rewardPoints": 20,
                      "activeFrom": "2026-04-26",
                      "activeUntil": "2026-04-27"
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.missionId").value(missionId.toString()))
            .andExpect(jsonPath("$.code").value("DAILY_READ"))
            .andExpect(jsonPath("$.rewardPoints").value(20));
    }

    @Test
    void listActiveDailyMissionsReturnsProgress() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        when(achievementService.listActiveDailyMissions(userId)).thenReturn(List.of(new DailyMissionProgressResponse(
            missionId,
            "DAILY_READ",
            "Baca Hari Ini",
            "",
            AchievementMetric.READING_COMPLETED,
            1,
            1,
            20,
            LocalDate.of(2026, 4, 26),
            LocalDate.of(2026, 4, 27),
            true,
            false,
            null
        )));

        mockMvc.perform(get("/api/achievements/daily-missions/active").queryParam("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].missionId").value(missionId.toString()))
            .andExpect(jsonPath("$[0].completed").value(true))
            .andExpect(jsonPath("$[0].claimed").value(false));
    }

    @Test
    void claimDailyMissionRewardReturnsClaimPayload() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID missionId = UUID.randomUUID();
        when(achievementService.claimDailyMissionReward(missionId, userId)).thenReturn(new ClaimRewardResponse(
            missionId,
            userId,
            20,
            true,
            Instant.parse("2026-04-26T10:00:00Z")
        ));

        mockMvc.perform(post("/api/achievements/daily-missions/{missionId}/claim", missionId)
                .queryParam("userId", userId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.missionId").value(missionId.toString()))
            .andExpect(jsonPath("$.userId").value(userId.toString()))
            .andExpect(jsonPath("$.rewardPoints").value(20))
            .andExpect(jsonPath("$.claimed").value(true));

        verify(achievementService).claimDailyMissionReward(missionId, userId);
    }
}
