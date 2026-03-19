package com.example.moonsyncapp.data.model

import androidx.compose.ui.graphics.Color
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

// ==========================================
// USER IDENTITY
// ==========================================

enum class IdentityMode {
    ANONYMOUS,    // "Luna User" + random number
    PSEUDONYM,    // Custom username
    REAL_NAME     // Verified real name
}

data class CommunityUser(
    val id: String,
    val displayName: String,
    val identityMode: IdentityMode,
    val currentPhase: CyclePhase?,
    val currentMood: UserMood?,
    val wisdomLevel: WisdomLevel,
    val isVerifiedProfessional: Boolean = false,
    val professionalTitle: String? = null,
    val joinedDate: LocalDateTime,
    val localCircle: String? = null
)

enum class UserMood(val emoji: String, val label: String) {
    GREAT("😊", "Feeling great"),
    OKAY("😐", "Just okay"),
    LOW("😔", "Feeling low"),
    FRUSTRATED("😤", "Frustrated"),
    TIRED("🥱", "Tired"),
    ANXIOUS("😰", "Anxious"),
    PEACEFUL("😌", "At peace"),
    ENERGETIC("⚡", "Energetic")
}

enum class WisdomLevel(
    val title: String,
    val emoji: String,
    val minPoints: Int,
    val color: Color
) {
    SEEDLING("Seedling", "🌱", 0, Color(0xFF81C784)),
    SPROUT("Sprout", "🌿", 100, Color(0xFF66BB6A)),
    BLOOMING("Blooming", "🌸", 500, Color(0xFFEC407A)),       // Can create groups at this level
    FLOURISHING("Flourishing", "🌺", 1500, Color(0xFFAB47BC)),
    WISE_TREE("Wise Tree", "🌳", 5000, Color(0xFF7B5EA7))     // Can be moderator at this level
}

// ==========================================
// POSTS & CONTENT
// ==========================================

data class CommunityPost(
    val id: String,
    val author: CommunityUser,
    val content: String,
    val imageUrl: String? = null,
    val createdAt: LocalDateTime,
    val ageRestriction: AgeRestriction,
    val category: PostCategory,
    val phaseTag: CyclePhase? = null,
    val reactions: List<PostReaction>,
    val commentCount: Int,
    val comments: List<PostComment> = emptyList(),
    val isPromotedToArticle: Boolean = false,
    val reportCount: Int = 0,
    val isFlagged: Boolean = false,
    val groupId: String? = null,
    val localCircle: String? = null
)

enum class AgeRestriction(val label: String, val minAge: Int) {
    ALL_AGES("All Ages", 13),
    MATURE("Mature", 18),
    ADULTS_ONLY("Adults Only", 21)
}

enum class PostCategory(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    QUESTION("Question", "❓", Color(0xFF42A5F5)),
    EXPERIENCE("Experience", "💭", Color(0xFFAB47BC)),
    TIP("Tip", "💡", Color(0xFFFFA726)),
    SUPPORT("Support Needed", "🤗", Color(0xFFEC407A)),
    CELEBRATION("Celebration", "🎉", Color(0xFF66BB6A)),
    DISCUSSION("Discussion", "💬", Color(0xFF7B5EA7)),
    PROFESSIONAL("Professional Advice", "🩺", Color(0xFF26A69A))
}

// ==========================================
// REACTIONS (Positive Only!)
// ==========================================

data class PostReaction(
    val type: ReactionType,
    val count: Int,
    val hasUserReacted: Boolean = false
)

enum class ReactionType(
    val emoji: String,
    val label: String
) {
    HUG("🤗", "Sending hugs"),
    RELATE("💜", "I relate"),
    STRENGTH("💪", "You're strong"),
    MOONBEAM("🌙", "Moon beam"),
    WISDOM("✨", "Wise words"),
    HELPFUL("🌸", "This helped"),
    LOVE("❤️", "Love this")
}

// ==========================================
// COMMENTS
// ==========================================

data class PostComment(
    val id: String,
    val postId: String,
    val author: CommunityUser,
    val content: String,
    val createdAt: LocalDateTime,
    val reactions: List<PostReaction> = emptyList(),
    val isVerifiedProfessional: Boolean = false,
    val isAnonymous: Boolean = false,
    val reportCount: Int = 0
)

// ==========================================
// GROUPS & CIRCLES
// ==========================================

data class CommunityGroup(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val memberCount: Int,
    val category: GroupCategory,
    val privacy: GroupPrivacy = GroupPrivacy.PUBLIC,
    val passcode: String? = null,  // For passcode-protected groups
    val createdBy: CommunityUser,
    val coverColor: Color,
    val isJoined: Boolean = false
)

enum class GroupCategory(
    val displayName: String,
    val emoji: String
) {
    PHASE_ROOM("Phase Room", "🌙"),
    LOCAL_CIRCLE("Local Circle", "📍"),
    HEALTH_CONDITION("Health Condition", "🩺"),
    LIFE_STAGE("Life Stage", "🌸"),
    LIFESTYLE("Lifestyle", "✨"),
    CUSTOM("Custom", "💜")
}

enum class GroupPrivacy(
    val displayName: String,
    val description: String,
    val emoji: String
) {
    PUBLIC("Public", "Anyone can join", "🌍"),
    PRIVATE_REQUEST("Private (Request)", "Admin approves members", "🔒"),
    PRIVATE_PASSCODE("Private (Passcode)", "Need code to join", "🔑")
}

// ==========================================
// PHASE ROOMS (Auto-joined)
// ==========================================

data class PhaseRoom(
    val phase: CyclePhase,
    val activeUsers: Int,
    val recentMessages: List<PhaseRoomMessage>
)

data class PhaseRoomMessage(
    val id: String,
    val author: CommunityUser,
    val content: String,
    val timestamp: LocalDateTime,
    val isAnonymous: Boolean = true,  // Default to anonymous
    val reportCount: Int = 0,
    val isHidden: Boolean = false
)

// ==========================================
// GAMIFICATION & STREAKS (ENHANCED)
// ==========================================

data class UserAchievement(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val earnedAt: LocalDateTime?,
    val progress: Float,
    val isUnlocked: Boolean
)

data class Challenge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val type: ChallengeType,
    val durationDays: Int,
    val pointsReward: Int,
    val participantCount: Int,
    val currentProgress: Float,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime
)

enum class ChallengeType(
    val displayName: String,
    val emoji: String,
    val color: Color
) {
    DAILY_CHECK_IN("Daily Check-in", "📅", Color(0xFF42A5F5)),
    PHASE_RITUAL("Phase Ritual", "🌙", Color(0xFFAB47BC)),
    WELLNESS("Wellness", "🧘", Color(0xFF66BB6A)),
    COMMUNITY("Community", "💜", Color(0xFFEC407A)),
    TRACKING("Tracking", "📊", Color(0xFFFFA726))
}

/**
 * Enhanced UserStreak with full tracking capabilities
 */
data class UserStreak(
    val type: StreakType,
    val currentCount: Int,
    val longestCount: Int,
    val lastActivityDate: LocalDate,
    val lastCheckinTime: LocalDateTime,
    val isBroken: Boolean = false,
    val freezeTokensRemaining: Int = 0,
    val activeFreezeUsedOn: LocalDate? = null,
    val activityHistory: Set<LocalDate> = emptySet(),
    val milestonesUnlocked: Set<StreakMilestone> = emptySet()
) {
    val isActive: Boolean
        get() = lastActivityDate == LocalDate.now()

    val isAtRisk: Boolean
        get() {
            if (isActive || currentCount == 0) return false
            val hoursSinceActivity = ChronoUnit.HOURS.between(lastCheckinTime, LocalDateTime.now())
            return hoursSinceActivity in 18..24
        }

    val isSafe: Boolean
        get() {
            if (isActive || currentCount == 0) return false
            val hoursSinceActivity = ChronoUnit.HOURS.between(lastCheckinTime, LocalDateTime.now())
            return hoursSinceActivity < 18
        }

    val hoursUntilBreak: Long
        get() = if (isAtRisk || isSafe) {
            24 - ChronoUnit.HOURS.between(lastCheckinTime, LocalDateTime.now())
        } else 0L

    val nextMilestone: StreakMilestone?
        get() = StreakMilestone.values()
            .filter { it.requiredDays > currentCount }
            .minByOrNull { it.requiredDays }

    val daysUntilNextMilestone: Int
        get() = nextMilestone?.let { it.requiredDays - currentCount } ?: 0

    val progressToNextMilestone: Float
        get() {
            val next = nextMilestone ?: return 1f
            val prev = StreakMilestone.values()
                .filter { it.requiredDays <= currentCount }
                .maxByOrNull { it.requiredDays }
                ?: return currentCount.toFloat() / next.requiredDays

            val range = next.requiredDays - prev.requiredDays
            val progress = currentCount - prev.requiredDays
            return progress.toFloat() / range
        }

    val state: StreakState
        get() = when {
            nextMilestone?.requiredDays == currentCount -> StreakState(
                status = StreakState.Status.MILESTONE_HIT,
                message = "Milestone reached!",
                color = nextMilestone?.badgeColor ?: Color(0xFFFFD700),
                icon = nextMilestone?.emoji ?: "🎉"
            )
            activeFreezeUsedOn == LocalDate.now() -> StreakState(
                status = StreakState.Status.FROZEN,
                message = "Streak protected",
                color = Color(0xFF42A5F5),
                icon = "🛡️"
            )
            isBroken -> StreakState(
                status = StreakState.Status.BROKEN,
                message = "Start fresh today",
                color = Color(0xFF9E9E9E),
                icon = "💔"
            )
            isActive -> StreakState(
                status = StreakState.Status.ACTIVE,
                message = "$currentCount days!",
                color = Color(0xFFFF9800),
                icon = "🔥"
            )
            isAtRisk -> StreakState(
                status = StreakState.Status.AT_RISK,
                message = "${hoursUntilBreak}h left",
                color = Color(0xFFFFC107),
                icon = "⚠️"
            )
            else -> StreakState(
                status = StreakState.Status.SAFE,
                message = "Keep it up!",
                color = Color(0xFF66BB6A),
                icon = type.emoji
            )
        }
}

enum class StreakType(val emoji: String, val label: String) {
    DAILY_LOGIN("🔥", "Login"),
    TRACKING("📝", "Logging"),
    COMMUNITY("💬", "Community"),
    CHALLENGE("🏆", "Challenges")
}

enum class StreakMilestone(
    val requiredDays: Int,
    val title: String,
    val emoji: String,
    val pointsReward: Int,
    val badgeColor: Color,
    val earnsFreezeToken: Boolean = false
) {
    FIRST_STEP(1, "First Step", "✨", 5, Color(0xFFB0BEC5)),
    THREE_DAY(3, "Getting Started", "🌱", 10, Color(0xFF81C784)),
    WEEK_WARRIOR(7, "Week Warrior", "⚡", 25, Color(0xFF64B5F6), earnsFreezeToken = true),
    TWO_WEEK(14, "Fortnight Champion", "🔥", 50, Color(0xFFFF9800)),
    CYCLE_COMPLETE(28, "Full Cycle", "🌙", 80, Color(0xFF7B5EA7)),
    MONTH_MASTER(30, "Month Master", "💎", 100, Color(0xFFAB47BC), earnsFreezeToken = true),
    CENTURY_SISTER(100, "Century Sister", "👑", 300, Color(0xFFFFD700), earnsFreezeToken = true),
    YEAR_LEGEND(365, "Year Legend", "🏆", 1000, Color(0xFFE91E63), earnsFreezeToken = true)
}

data class StreakState(
    val status: Status,
    val message: String,
    val color: Color,
    val icon: String
) {
    enum class Status {
        ACTIVE,
        AT_RISK,
        SAFE,
        BROKEN,
        FROZEN,
        MILESTONE_HIT
    }
}

data class MilestoneCelebration(
    val milestone: StreakMilestone,
    val streakType: StreakType,
    val newCount: Int
)

// ==========================================
// PROFESSIONAL VERIFICATION
// ==========================================

data class ProfessionalApplication(
    val userId: String,
    val fullName: String,
    val licenseNumber: String,
    val issuingBody: String,
    val specialty: String,
    val linkedInUrl: String?,
    val documentUrls: List<String>,
    val status: VerificationStatus,
    val submittedAt: LocalDateTime,
    val reviewedAt: LocalDateTime?,
    val reviewNotes: String?
)

enum class VerificationStatus {
    PENDING,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    NEEDS_MORE_INFO
}

// ==========================================
// REPORTING & MODERATION
// ==========================================

data class Report(
    val id: String,
    val reporterId: String,
    val contentId: String,
    val contentType: ContentType,
    val reason: ReportReason,
    val additionalNotes: String?,
    val createdAt: LocalDateTime,
    val status: ReportStatus
)

enum class ContentType {
    POST,
    COMMENT,
    USER,
    GROUP,
    PHASE_ROOM_MESSAGE,
    AI_MESSAGE
}

enum class ReportReason(val displayName: String) {
    INAPPROPRIATE("Inappropriate content"),
    MISINFORMATION("Medical misinformation"),
    HARASSMENT("Harassment or bullying"),
    SPAM("Spam"),
    SELF_HARM("Self-harm or dangerous"),
    UNDERAGE_CONTENT("Inappropriate for age group"),
    FAKE_PROFESSIONAL("Fake professional claims"),
    OTHER("Other")
}

enum class ReportStatus {
    PENDING,
    REVIEWING,
    ACTION_TAKEN,
    DISMISSED
}

// ==========================================
// COMMUNITY TABS (UPDATED)
// ==========================================

enum class CommunityTab(
    val title: String,
    val emoji: String
) {
    FEED("Feed", "🏠"),
    PHASE_ROOM("Phase Room", "🌙"),
    SEARCH("Search", "🔍"),
    GROUPS("Groups", "💜"),
    CHALLENGES("Challenges", "🏆")
}

// ==========================================
// SEARCH
// ==========================================

enum class SearchFilter(val displayName: String, val emoji: String) {
    ALL("All", "✨"),
    POSTS("Posts", "📝"),
    GROUPS("Groups", "💜"),
    USERS("Users", "👤")
}

data class SearchResult(
    val type: SearchFilter,
    val post: CommunityPost? = null,
    val group: CommunityGroup? = null,
    val user: CommunityUser? = null
)

// ==========================================
// SHARE OPTIONS
// ==========================================

enum class ShareOption(val displayName: String, val emoji: String) {
    SHARE_TO_GROUP("Share to Group", "💜"),
    COPY_LINK("Copy Link", "🔗"),
    SHARE_EXTERNAL("Share External", "📤")
}