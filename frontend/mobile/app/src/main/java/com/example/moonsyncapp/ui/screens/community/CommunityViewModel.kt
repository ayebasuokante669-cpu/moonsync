package com.example.moonsyncapp.ui.screens.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moonsyncapp.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.update

class CommunityViewModel : ViewModel() {

    // ==========================================
    // UI STATE
    // ==========================================

    private val _selectedTab = MutableStateFlow(CommunityTab.FEED)
    val selectedTab: StateFlow<CommunityTab> = _selectedTab.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _showCreatePostSheet = MutableStateFlow(false)
    val showCreatePostSheet: StateFlow<Boolean> = _showCreatePostSheet.asStateFlow()

    // ==========================================
    // USER STATE
    // ==========================================

    private val _currentUser = MutableStateFlow(getMockCurrentUser())
    val currentUser: StateFlow<CommunityUser> = _currentUser.asStateFlow()

    private val _userStreaks = MutableStateFlow(getMockStreaks())
    val userStreaks: StateFlow<List<UserStreak>> = _userStreaks.asStateFlow()

    private val _userAchievements = MutableStateFlow(getMockAchievements())
    val userAchievements: StateFlow<List<UserAchievement>> = _userAchievements.asStateFlow()

    private val _userPoints = MutableStateFlow(750)
    val userPoints: StateFlow<Int> = _userPoints.asStateFlow()

    // ==========================================
    // STREAK STATE
    // ==========================================

    private val _showMilestoneCelebration = MutableStateFlow<MilestoneCelebration?>(null)
    val showMilestoneCelebration: StateFlow<MilestoneCelebration?> = _showMilestoneCelebration.asStateFlow()

    private val _showStreakAtRiskBanner = MutableStateFlow(false)
    val showStreakAtRiskBanner: StateFlow<Boolean> = _showStreakAtRiskBanner.asStateFlow()

    // ==========================================
    // FEED STATE
    // ==========================================

    private val _posts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val posts: StateFlow<List<CommunityPost>> = _posts.asStateFlow()

    private val _filteredPosts = MutableStateFlow<List<CommunityPost>>(emptyList())
    val filteredPosts: StateFlow<List<CommunityPost>> = _filteredPosts.asStateFlow()

    private val _selectedPostCategory = MutableStateFlow<PostCategory?>(null)
    val selectedPostCategory: StateFlow<PostCategory?> = _selectedPostCategory.asStateFlow()

    private val _selectedPhaseFilter = MutableStateFlow<CyclePhase?>(null)
    val selectedPhaseFilter: StateFlow<CyclePhase?> = _selectedPhaseFilter.asStateFlow()

    // Hidden content tracking (for personal hide after reporting)
    private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())
    private val _hiddenCommentIds = MutableStateFlow<Set<String>>(emptySet())
    private val _hiddenMessageIds = MutableStateFlow<Set<String>>(emptySet())

    // ==========================================
    // PHASE ROOM STATE
    // ==========================================

    private val _phaseRooms = MutableStateFlow(getMockPhaseRooms())
    val phaseRooms: StateFlow<List<PhaseRoom>> = _phaseRooms.asStateFlow()

    private val _currentPhaseRoom = MutableStateFlow<PhaseRoom?>(null)
    val currentPhaseRoom: StateFlow<PhaseRoom?> = _currentPhaseRoom.asStateFlow()

    // ==========================================
    // GROUPS STATE (Replaces Explore)
    // ==========================================

    private val _groups = MutableStateFlow<List<CommunityGroup>>(emptyList())
    val groups: StateFlow<List<CommunityGroup>> = _groups.asStateFlow()

    private val _localCircles = MutableStateFlow<List<CommunityGroup>>(emptyList())
    val localCircles: StateFlow<List<CommunityGroup>> = _localCircles.asStateFlow()

    private val _verifiedProfessionals = MutableStateFlow<List<CommunityUser>>(emptyList())
    val verifiedProfessionals: StateFlow<List<CommunityUser>> = _verifiedProfessionals.asStateFlow()

    // ==========================================
    // SEARCH STATE
    // ==========================================

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchFilter = MutableStateFlow(SearchFilter.ALL)
    val searchFilter: StateFlow<SearchFilter> = _searchFilter.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults: StateFlow<List<SearchResult>> = _searchResults.asStateFlow()

    // ==========================================
    // CHALLENGES STATE
    // ==========================================

    private val _activeChallenges = MutableStateFlow<List<Challenge>>(emptyList())
    val activeChallenges: StateFlow<List<Challenge>> = _activeChallenges.asStateFlow()

    private val _joinedChallenges = MutableStateFlow<Set<String>>(emptySet())
    val joinedChallenges: StateFlow<Set<String>> = _joinedChallenges.asStateFlow()

    // ==========================================
    // INITIALIZATION
    // ==========================================

    init {
        loadInitialData()
        validateAllStreaks()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true

            _posts.value = fetchPostsFromApi()
            _groups.value = getMockGroups()
            _localCircles.value = getMockLocalCircles()
            _verifiedProfessionals.value = getMockProfessionals()
            _activeChallenges.value = getMockChallenges()

            val userPhase = _currentUser.value.currentPhase
            _currentPhaseRoom.value = _phaseRooms.value.find { it.phase == userPhase }

            refreshFilteredPosts()

            _isLoading.value = false
        }
    }

    // ==========================================
    // TAB ACTIONS
    // ==========================================

    fun selectTab(tab: CommunityTab) {
        _selectedTab.value = tab
    }

    // ==========================================
    // FEED ACTIONS
    // ==========================================

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            _posts.value = fetchPostsFromApi()
            refreshFilteredPosts()
            _isRefreshing.value = false
        }
    }

    fun filterByCategory(category: PostCategory?) {
        _selectedPostCategory.value = category
        refreshFilteredPosts()
    }

    fun filterByPhase(phase: CyclePhase?) {
        _selectedPhaseFilter.value = phase
        refreshFilteredPosts()
    }

    private fun refreshFilteredPosts() {
        _filteredPosts.value = getFilteredPosts()
    }

    fun getFilteredPosts(): List<CommunityPost> {
        var filtered = _posts.value

        // Filter out auto-hidden posts (hidden for everyone)
        filtered = filtered.filter { !it.isAutoHidden }

        // Filter out posts hidden by current user (personal hide after reporting)
        filtered = filtered.filter { it.id !in _hiddenPostIds.value }

        _selectedPostCategory.value?.let { category ->
            filtered = filtered.filter { it.category == category }
        }

        _selectedPhaseFilter.value?.let { phase ->
            filtered = filtered.filter { it.phaseTag == phase }
        }

        return filtered.sortedWith(
            compareByDescending<CommunityPost> { it.isPromotedToArticle }
                .thenByDescending { it.createdAt }
        )
    }

    // ==========================================
    // POST ACTIONS
    // ==========================================

    fun showCreatePost() {
        _showCreatePostSheet.value = true
    }

    fun hideCreatePost() {
        _showCreatePostSheet.value = false
    }

    fun createPost(
        content: String,
        category: PostCategory,
        ageRestriction: AgeRestriction,
        phaseTag: CyclePhase?,
        isAnonymous: Boolean
    ) {
        viewModelScope.launch {
            val newPost = CommunityPost(
                id = "post_${System.currentTimeMillis()}",
                author = if (isAnonymous) {
                    _currentUser.value.copy(
                        displayName = "Anonymous Sister",
                        identityMode = IdentityMode.ANONYMOUS
                    )
                } else {
                    _currentUser.value
                },
                content = content,
                imageUrl = null,  // No image support yet
                createdAt = LocalDateTime.now(),
                ageRestriction = ageRestriction,
                category = category,
                phaseTag = phaseTag,
                reactions = ReactionType.values().map {
                    PostReaction(it, 0, false)
                },
                commentCount = 0
            )

            _posts.value = listOf(newPost) + _posts.value
            _showCreatePostSheet.value = false

            refreshFilteredPosts()

            // Increment community streak for posting
            incrementStreak(StreakType.COMMUNITY)

            awardPoints(10, "Created a post")
        }
    }

    fun reactToPost(postId: String, reactionType: ReactionType) {
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val existingUserReaction = post.reactions.find { it.hasUserReacted }

                val updatedReactions = when {
                    existingUserReaction == null -> {
                        post.reactions.map { reaction ->
                            if (reaction.type == reactionType) {
                                reaction.copy(
                                    count = reaction.count + 1,
                                    hasUserReacted = true
                                )
                            } else {
                                reaction
                            }
                        }
                    }

                    existingUserReaction.type == reactionType -> {
                        post.reactions.map { reaction ->
                            if (reaction.type == reactionType) {
                                reaction.copy(
                                    count = (reaction.count - 1).coerceAtLeast(0),
                                    hasUserReacted = false
                                )
                            } else {
                                reaction
                            }
                        }
                    }

                    else -> {
                        post.reactions.map { reaction ->
                            when (reaction.type) {
                                existingUserReaction.type -> reaction.copy(
                                    count = (reaction.count - 1).coerceAtLeast(0),
                                    hasUserReacted = false
                                )
                                reactionType -> reaction.copy(
                                    count = reaction.count + 1,
                                    hasUserReacted = true
                                )
                                else -> reaction
                            }
                        }
                    }
                }

                post.copy(reactions = updatedReactions)
            } else {
                post
            }
        }

        // First reaction of the day counts for community streak
        checkAndIncrementCommunityStreak()

        refreshFilteredPosts()
    }

    fun reportPost(postId: String, reason: ReportReason, notes: String?) {
        viewModelScope.launch {
            _posts.value = _posts.value.map { post ->
                if (post.id == postId) {
                    // Check if already reported by current user
                    if (post.hasCurrentUserReported) {
                        return@map post  // Already reported, no changes
                    }

                    val newCount = post.reportCount + 1
                    post.copy(
                        reportCount = newCount,
                        isFlagged = newCount >= 2,
                        isAutoHidden = newCount >= 5,  // Auto-hide threshold for posts
                        hasCurrentUserReported = true
                    )
                } else {
                    post
                }
            }

            // Hide post for this user (personal hide)
            _hiddenPostIds.update { it + postId }

            refreshFilteredPosts()

            // TODO: Backend integration
            // api.reportPost(postId, reason, notes)
        }
    }

    // ==========================================
    // HIDE/UNHIDE ACTIONS (Optional Undo)
    // ==========================================

    fun unhidePost(postId: String) {
        _hiddenPostIds.update { it - postId }
        refreshFilteredPosts()
    }

    fun unhideComment(commentId: String) {
        _hiddenCommentIds.update { it - commentId }
    }

    fun unhideMessage(messageId: String) {
        _hiddenMessageIds.update { it - messageId }
    }

    fun reportComment(commentId: String, reason: ReportReason, notes: String?) {
        viewModelScope.launch {
            _posts.value = _posts.value.map { post ->
                val updatedComments = post.comments.map { comment ->
                    if (comment.id == commentId) {
                        // Check if already reported by current user
                        if (comment.hasCurrentUserReported) {
                            return@map comment  // Already reported, no changes
                        }

                        val newCount = comment.reportCount + 1
                        comment.copy(
                            reportCount = newCount,
                            isAutoHidden = newCount >= 3,  // Auto-hide threshold for comments
                            hasCurrentUserReported = true
                        )
                    } else {
                        comment
                    }
                }

                // Only update post if it has comments
                if (post.comments.isNotEmpty()) {
                    post.copy(comments = updatedComments)
                } else {
                    post
                }
            }

            // Hide comment for this user (personal hide)
            _hiddenCommentIds.update { it + commentId }

            refreshFilteredPosts()

            // TODO: Backend integration
            // api.reportComment(commentId, reason, notes)
        }
    }

    // ==========================================
    // PHASE ROOM ACTIONS
    // ==========================================

    fun joinPhaseRoom(phase: CyclePhase) {
        _currentPhaseRoom.value = _phaseRooms.value.find { it.phase == phase }
    }

    fun sendPhaseRoomMessage(content: String, isAnonymous: Boolean = true) {
        viewModelScope.launch {
            val currentRoom = _currentPhaseRoom.value ?: return@launch

            val newMessage = PhaseRoomMessage(
                id = "msg_${System.currentTimeMillis()}",
                author = _currentUser.value,
                content = content,
                timestamp = LocalDateTime.now(),
                isAnonymous = isAnonymous
            )

            val updatedRoom = currentRoom.copy(
                recentMessages = listOf(newMessage) + currentRoom.recentMessages.take(49)
            )

            _currentPhaseRoom.value = updatedRoom
            _phaseRooms.value = _phaseRooms.value.map {
                if (it.phase == currentRoom.phase) updatedRoom else it
            }

            // Increment community streak for phase room participation
            incrementStreak(StreakType.COMMUNITY)
        }
    }

    fun reportPhaseRoomMessage(messageId: String, reason: ReportReason, notes: String?) {
        viewModelScope.launch {
            val currentRoom = _currentPhaseRoom.value ?: return@launch

            val updatedMessages = currentRoom.recentMessages.map { message ->
                if (message.id == messageId) {
                    // Check if already reported by current user
                    if (message.hasCurrentUserReported) {
                        return@map message  // Already reported, no changes
                    }

                    val newCount = message.reportCount + 1
                    message.copy(
                        reportCount = newCount,
                        isHidden = newCount >= 3,  // Keep existing behavior
                        isAutoHidden = newCount >= 3,  // Auto-hide threshold for messages
                        hasCurrentUserReported = true
                    )
                } else {
                    message
                }
            }

            val updatedRoom = currentRoom.copy(recentMessages = updatedMessages)
            _currentPhaseRoom.value = updatedRoom
            _phaseRooms.value = _phaseRooms.value.map {
                if (it.phase == currentRoom.phase) updatedRoom else it
            }

            // Hide message for this user (personal hide)
            _hiddenMessageIds.update { it + messageId }

            // TODO: Backend integration
            // api.reportPhaseRoomMessage(messageId, reason, notes)
        }
    }

    // ==========================================
    // SEARCH ACTIONS
    // ==========================================

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        performSearch()
    }

    fun updateSearchFilter(filter: SearchFilter) {
        _searchFilter.value = filter
        performSearch()
    }

    private fun performSearch() {
        val query = _searchQuery.value.lowercase().trim()
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }

        val results = mutableListOf<SearchResult>()

        // Search posts
        if (_searchFilter.value == SearchFilter.ALL || _searchFilter.value == SearchFilter.POSTS) {
            _posts.value
                .filter { it.content.lowercase().contains(query) }
                .take(10)
                .forEach { results.add(SearchResult(type = SearchFilter.POSTS, post = it)) }
        }

        // Search groups
        if (_searchFilter.value == SearchFilter.ALL || _searchFilter.value == SearchFilter.GROUPS) {
            (_groups.value + _localCircles.value)
                .filter {
                    it.name.lowercase().contains(query) ||
                            it.description.lowercase().contains(query)
                }
                .take(10)
                .forEach { results.add(SearchResult(type = SearchFilter.GROUPS, group = it)) }
        }

        // Search users (by display name)
        if (_searchFilter.value == SearchFilter.ALL || _searchFilter.value == SearchFilter.USERS) {
            val allUsers = _posts.value.map { it.author }.distinctBy { it.id }
            allUsers
                .filter { it.displayName.lowercase().contains(query) }
                .take(10)
                .forEach { results.add(SearchResult(type = SearchFilter.USERS, user = it)) }
        }

        _searchResults.value = results
    }

    // ==========================================
    // GROUP ACTIONS
    // ==========================================

    fun createGroup(
        name: String,
        description: String,
        emoji: String,
        category: GroupCategory,
        privacy: GroupPrivacy,
        passcode: String?
    ) {
        viewModelScope.launch {
            // Check if user has required wisdom level (Blooming = 500 pts)
            if (_currentUser.value.wisdomLevel.minPoints < WisdomLevel.BLOOMING.minPoints) {
                return@launch
            }

            val newGroup = CommunityGroup(
                id = "grp_${System.currentTimeMillis()}",
                name = name,
                description = description,
                emoji = emoji,
                memberCount = 1, // Creator is first member
                category = category,
                privacy = privacy,
                passcode = passcode,
                createdBy = _currentUser.value,
                coverColor = getRandomGroupColor(),
                isJoined = true
            )

            _groups.value = listOf(newGroup) + _groups.value

            awardPoints(25, "Created a group")
        }
    }

    fun joinGroup(groupId: String, passcode: String? = null) {
        viewModelScope.launch {
            _groups.value = _groups.value.map { group ->
                if (group.id == groupId) {
                    // Check passcode if required
                    if (group.privacy == GroupPrivacy.PRIVATE_PASSCODE && group.passcode != passcode) {
                        return@launch // Wrong passcode
                    }
                    group.copy(
                        memberCount = group.memberCount + 1,
                        isJoined = true
                    )
                } else {
                    group
                }
            }

            _localCircles.value = _localCircles.value.map { circle ->
                if (circle.id == groupId) {
                    circle.copy(
                        memberCount = circle.memberCount + 1,
                        isJoined = true
                    )
                } else {
                    circle
                }
            }

            awardPoints(5, "Joined a group")
        }
    }

    fun leaveGroup(groupId: String) {
        viewModelScope.launch {
            _groups.value = _groups.value.map { group ->
                if (group.id == groupId) {
                    group.copy(
                        memberCount = (group.memberCount - 1).coerceAtLeast(0),
                        isJoined = false
                    )
                } else {
                    group
                }
            }

            _localCircles.value = _localCircles.value.map { circle ->
                if (circle.id == groupId) {
                    circle.copy(
                        memberCount = (circle.memberCount - 1).coerceAtLeast(0),
                        isJoined = false
                    )
                } else {
                    circle
                }
            }
        }
    }

    private fun getRandomGroupColor(): Color {
        val colors = listOf(
            Color(0xFFAB47BC),
            Color(0xFFEC407A),
            Color(0xFF66BB6A),
            Color(0xFF42A5F5),
            Color(0xFFFFA726),
            Color(0xFF7B5EA7),
            Color(0xFF26A69A)
        )
        return colors.random()
    }

    // ==========================================
    // CHALLENGE ACTIONS
    // ==========================================

    fun joinChallenge(challengeId: String) {
        _joinedChallenges.value = _joinedChallenges.value + challengeId

        _activeChallenges.value = _activeChallenges.value.map { challenge ->
            if (challenge.id == challengeId) {
                challenge.copy(participantCount = challenge.participantCount + 1)
            } else {
                challenge
            }
        }

        awardPoints(5, "Joined a challenge")
    }

    fun leaveChallenge(challengeId: String) {
        _joinedChallenges.value = _joinedChallenges.value - challengeId

        _activeChallenges.value = _activeChallenges.value.map { challenge ->
            if (challenge.id == challengeId) {
                challenge.copy(participantCount = (challenge.participantCount - 1).coerceAtLeast(0))
            } else {
                challenge
            }
        }
    }

    fun updateChallengeProgress(challengeId: String, progress: Float) {
        _activeChallenges.value = _activeChallenges.value.map { challenge ->
            if (challenge.id == challengeId) {
                val newProgress = progress.coerceIn(0f, 1f)
                if (newProgress >= 1f && challenge.currentProgress < 1f) {
                    // Increment challenge streak when completing a challenge
                    incrementStreak(StreakType.CHALLENGE)
                    awardPoints(challenge.pointsReward, "Completed: ${challenge.title}")
                }
                challenge.copy(currentProgress = newProgress)
            } else {
                challenge
            }
        }
    }

    // ==========================================
    // STREAK MANAGEMENT (ENHANCED)
    // ==========================================

    fun incrementStreak(type: StreakType) {
        viewModelScope.launch {
            val streak = _userStreaks.value.find { it.type == type } ?: return@launch
            val today = LocalDate.now()

            // Already logged today? Skip
            if (streak.lastActivityDate == today) return@launch

            // Calculate gap
            val daysSinceLastActivity = ChronoUnit.DAYS.between(streak.lastActivityDate, today)

            val updatedStreak = when {
                // Consecutive day
                daysSinceLastActivity == 1L -> {
                    streak.copy(
                        currentCount = streak.currentCount + 1,
                        longestCount = maxOf(streak.longestCount, streak.currentCount + 1),
                        lastActivityDate = today,
                        lastCheckinTime = LocalDateTime.now(),
                        isBroken = false,
                        activityHistory = (streak.activityHistory + today).toList().takeLast(365).toSet()
                    )
                }

                // Same day (edge case)
                daysSinceLastActivity == 0L -> return@launch

                // Gap detected - check freeze
                daysSinceLastActivity > 1L && streak.freezeTokensRemaining > 0 -> {
                    streak.copy(
                        currentCount = streak.currentCount + 1,
                        longestCount = maxOf(streak.longestCount, streak.currentCount + 1),
                        lastActivityDate = today,
                        lastCheckinTime = LocalDateTime.now(),
                        freezeTokensRemaining = streak.freezeTokensRemaining - 1,
                        activeFreezeUsedOn = today.minusDays(1),
                        activityHistory = (streak.activityHistory + today).toList().takeLast(365).toSet()
                    )
                }

                // Streak broken - restart
                else -> {
                    streak.copy(
                        currentCount = 1,
                        lastActivityDate = today,
                        lastCheckinTime = LocalDateTime.now(),
                        isBroken = true,
                        activityHistory = (streak.activityHistory + today).toList().takeLast(365).toSet()
                    )
                }
            }

            // Update state
            _userStreaks.value = _userStreaks.value.map {
                if (it.type == type) updatedStreak else it
            }

            // Check for milestones
            checkStreakMilestones(updatedStreak)

            // Award base points
            val streakPoints = when {
                updatedStreak.currentCount >= 30 -> 5
                updatedStreak.currentCount >= 7 -> 4
                else -> 3
            }
            awardPoints(streakPoints, "${type.label} streak: ${updatedStreak.currentCount} days")

            // Update at-risk banner state
            checkStreakAtRiskStatus()
        }
    }

    fun validateAllStreaks() {
        viewModelScope.launch {
            val today = LocalDate.now()

            _userStreaks.value = _userStreaks.value.map { streak ->
                val daysSinceActivity = ChronoUnit.DAYS.between(streak.lastActivityDate, today)

                when {
                    daysSinceActivity == 0L -> streak
                    daysSinceActivity == 1L -> streak.copy(isBroken = false)
                    daysSinceActivity == 2L && streak.freezeTokensRemaining > 0 -> streak
                    daysSinceActivity > 1L -> {
                        streak.copy(
                            currentCount = 0,
                            isBroken = true
                        )
                    }
                    else -> streak
                }
            }

            checkStreakAtRiskStatus()
        }
    }

    private fun checkStreakAtRiskStatus() {
        _showStreakAtRiskBanner.value = _userStreaks.value.any { it.isAtRisk }
    }

    private fun checkStreakMilestones(streak: UserStreak) {
        StreakMilestone.values().forEach { milestone ->
            if (streak.currentCount == milestone.requiredDays &&
                milestone !in streak.milestonesUnlocked) {

                awardPoints(milestone.pointsReward, "🎉 ${milestone.title}!")

                _userStreaks.value = _userStreaks.value.map {
                    if (it.type == streak.type) {
                        it.copy(
                            freezeTokensRemaining = if (milestone.earnsFreezeToken) {
                                it.freezeTokensRemaining + 1
                            } else {
                                it.freezeTokensRemaining
                            },
                            milestonesUnlocked = it.milestonesUnlocked + milestone
                        )
                    } else it
                }

                _showMilestoneCelebration.value = MilestoneCelebration(
                    milestone = milestone,
                    streakType = streak.type,
                    newCount = streak.currentCount
                )
            }
        }
    }

    fun useStreakFreeze(type: StreakType) {
        _userStreaks.value = _userStreaks.value.map { streak ->
            if (streak.type == type && streak.freezeTokensRemaining > 0) {
                streak.copy(
                    freezeTokensRemaining = streak.freezeTokensRemaining - 1,
                    activeFreezeUsedOn = LocalDate.now()
                )
            } else streak
        }
    }

    fun getStreakCalendarData(type: StreakType, yearMonth: YearMonth): Map<LocalDate, Boolean> {
        val streak = _userStreaks.value.find { it.type == type } ?: return emptyMap()
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        return (0..ChronoUnit.DAYS.between(startDate, endDate))
            .map { startDate.plusDays(it) }
            .associateWith { it in streak.activityHistory }
    }

    fun dismissMilestoneCelebration() {
        _showMilestoneCelebration.value = null
    }

    private fun checkAndIncrementCommunityStreak() {
        val communityStreak = _userStreaks.value.find { it.type == StreakType.COMMUNITY }
        if (communityStreak?.lastActivityDate != LocalDate.now()) {
            incrementStreak(StreakType.COMMUNITY)
        }
    }

    // ==========================================
    // APP LIFECYCLE ACTIONS
    // ==========================================

    fun onAppResume() {
        validateAllStreaks()
        incrementStreak(StreakType.DAILY_LOGIN)
    }

    // ==========================================
    // GAMIFICATION ACTIONS
    // ==========================================

    private fun awardPoints(amount: Int, reason: String) {
        _userPoints.value += amount
        updateWisdomLevel()
    }

    private fun updateWisdomLevel() {
        val points = _userPoints.value
        val newLevel = WisdomLevel.values()
            .filter { points >= it.minPoints }
            .maxByOrNull { it.minPoints }
            ?: WisdomLevel.SEEDLING

        if (newLevel != _currentUser.value.wisdomLevel) {
            _currentUser.value = _currentUser.value.copy(wisdomLevel = newLevel)
        }
    }

    // ==========================================
    // USER ACTIONS
    // ==========================================

    fun updateIdentityMode(mode: IdentityMode) {
        _currentUser.value = _currentUser.value.copy(identityMode = mode)
    }

    fun updateMood(mood: UserMood) {
        _currentUser.value = _currentUser.value.copy(currentMood = mood)
        awardPoints(2, "Shared mood")
    }

    // ==========================================
    // HELPER FUNCTIONS
    // ==========================================

    fun getTotalReactions(post: CommunityPost): Int {
        return post.reactions.sumOf { it.count }
    }

    fun getTopReactions(post: CommunityPost, limit: Int = 3): List<PostReaction> {
        return post.reactions
            .filter { it.count > 0 }
            .sortedByDescending { it.count }
            .take(limit)
    }

    fun getUserReaction(post: CommunityPost): ReactionType? {
        return post.reactions.find { it.hasUserReacted }?.type
    }

    fun getPhaseRoomUserCount(phase: CyclePhase): Int {
        return _phaseRooms.value.find { it.phase == phase }?.activeUsers ?: 0
    }

    // ==========================================
    // MOCK DATA
    // ==========================================

    private fun getMockCurrentUser(): CommunityUser {
        return CommunityUser(
            id = "user_current",
            displayName = "Luna Flower",
            identityMode = IdentityMode.PSEUDONYM,
            currentPhase = CyclePhase.FOLLICULAR,
            currentMood = UserMood.ENERGETIC,
            wisdomLevel = WisdomLevel.BLOOMING,
            isVerifiedProfessional = false,
            professionalTitle = null,
            joinedDate = LocalDateTime.now().minusMonths(3),
            localCircle = "Lagos"
        )
    }

    private fun getMockStreaks(): List<UserStreak> {
        val today = LocalDate.now()
        return listOf(
            UserStreak(
                type = StreakType.DAILY_LOGIN,
                currentCount = 12,
                longestCount = 23,
                lastActivityDate = today.minusDays(1),
                lastCheckinTime = LocalDateTime.now().minusHours(20),
                freezeTokensRemaining = 1,
                activityHistory = (0..11).map { today.minusDays(it.toLong()) }.toSet(),
                milestonesUnlocked = setOf(
                    StreakMilestone.FIRST_STEP,
                    StreakMilestone.THREE_DAY,
                    StreakMilestone.WEEK_WARRIOR
                )
            ),
            UserStreak(
                type = StreakType.TRACKING,
                currentCount = 8,
                longestCount = 15,
                lastActivityDate = today,
                lastCheckinTime = LocalDateTime.now(),
                freezeTokensRemaining = 0,
                activityHistory = (0..7).map { today.minusDays(it.toLong()) }.toSet(),
                milestonesUnlocked = setOf(
                    StreakMilestone.FIRST_STEP,
                    StreakMilestone.THREE_DAY,
                    StreakMilestone.WEEK_WARRIOR
                )
            ),
            UserStreak(
                type = StreakType.COMMUNITY,
                currentCount = 0,
                longestCount = 10,
                lastActivityDate = today.minusDays(2),
                lastCheckinTime = LocalDateTime.now().minusHours(50),
                isBroken = true,
                freezeTokensRemaining = 0,
                activityHistory = emptySet(),
                milestonesUnlocked = setOf(
                    StreakMilestone.FIRST_STEP,
                    StreakMilestone.THREE_DAY
                )
            ),
            UserStreak(
                type = StreakType.CHALLENGE,
                currentCount = 0,
                longestCount = 3,
                lastActivityDate = today.minusWeeks(2),
                lastCheckinTime = LocalDateTime.now().minusWeeks(2),
                freezeTokensRemaining = 0,
                activityHistory = emptySet(),
                milestonesUnlocked = emptySet()
            )
        )
    }

    private fun getMockAchievements(): List<UserAchievement> {
        return listOf(
            UserAchievement(
                id = "ach_1",
                title = "First Post",
                description = "Share your first post with the community",
                emoji = "✨",
                earnedAt = LocalDateTime.now().minusWeeks(2),
                progress = 1f,
                isUnlocked = true
            ),
            UserAchievement(
                id = "ach_2",
                title = "Helping Hand",
                description = "Give 50 reactions to other posts",
                emoji = "🤗",
                earnedAt = null,
                progress = 0.68f,
                isUnlocked = false
            ),
            UserAchievement(
                id = "ach_3",
                title = "Phase Explorer",
                description = "Join all 4 phase rooms",
                emoji = "🌙",
                earnedAt = null,
                progress = 0.75f,
                isUnlocked = false
            ),
            UserAchievement(
                id = "ach_4",
                title = "Cycle Master",
                description = "Track 3 complete cycles",
                emoji = "🏆",
                earnedAt = null,
                progress = 0.33f,
                isUnlocked = false
            )
        )
    }

    private suspend fun fetchPostsFromApi(): List<CommunityPost> {
        return withContext(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://moonsync-production.up.railway.app/community/posts")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                conn.connectTimeout = 15000
                conn.readTimeout = 15000

                if (conn.responseCode == 200) {
                    val response = conn.inputStream.bufferedReader(Charsets.UTF_8).readText()
                    parsePostsJson(response)
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    private fun parsePostsJson(json: String): List<CommunityPost> {
        return try {
            val array = org.json.JSONArray(json)
            val posts = mutableListOf<CommunityPost>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                posts.add(parsePost(obj))
            }
            posts
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun parsePost(obj: org.json.JSONObject): CommunityPost {
        val authorObj = obj.optJSONObject("author")
        val author = if (authorObj != null) parseUser(authorObj) else defaultAnonymousUser()

        val createdAtStr = obj.optString("created_at", "")
        val createdAt = try {
            LocalDateTime.parse(createdAtStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }

        val categoryStr = obj.optString("category", "DISCUSSION")
        val category = try { PostCategory.valueOf(categoryStr) } catch (e: Exception) { PostCategory.DISCUSSION }

        val ageStr = obj.optString("age_restriction", "ALL_AGES")
        val ageRestriction = try { AgeRestriction.valueOf(ageStr) } catch (e: Exception) { AgeRestriction.ALL_AGES }

        val phaseStr = obj.optString("phase_tag", "")
        val phaseTag = if (phaseStr.isNotBlank()) {
            try { CyclePhase.valueOf(phaseStr) } catch (e: Exception) { null }
        } else null

        val reactionsArray = obj.optJSONArray("reactions")
        val reactions = if (reactionsArray != null) {
            parseReactions(reactionsArray)
        } else {
            ReactionType.values().map { PostReaction(it, 0, false) }
        }

        return CommunityPost(
            id = obj.optString("id", "post_${System.currentTimeMillis()}"),
            author = author,
            content = obj.optString("content", ""),
            imageUrl = obj.optString("image_url", "").takeIf { it.isNotBlank() },
            createdAt = createdAt,
            ageRestriction = ageRestriction,
            category = category,
            phaseTag = phaseTag,
            reactions = reactions,
            commentCount = obj.optInt("comment_count", 0),
            isPromotedToArticle = obj.optBoolean("is_promoted_to_article", false),
            isAutoHidden = obj.optBoolean("is_auto_hidden", false)
        )
    }

    private fun parseUser(obj: org.json.JSONObject): CommunityUser {
        val identityStr = obj.optString("identity_mode", "ANONYMOUS")
        val identityMode = try { IdentityMode.valueOf(identityStr) } catch (e: Exception) { IdentityMode.ANONYMOUS }

        val phaseStr = obj.optString("current_phase", "")
        val currentPhase = if (phaseStr.isNotBlank()) {
            try { CyclePhase.valueOf(phaseStr) } catch (e: Exception) { null }
        } else null

        val moodStr = obj.optString("current_mood", "")
        val currentMood = if (moodStr.isNotBlank()) {
            try { UserMood.valueOf(moodStr) } catch (e: Exception) { null }
        } else null

        val wisdomStr = obj.optString("wisdom_level", "SEEDLING")
        val wisdomLevel = try { WisdomLevel.valueOf(wisdomStr) } catch (e: Exception) { WisdomLevel.SEEDLING }

        val joinedStr = obj.optString("joined_date", "")
        val joinedDate = try {
            LocalDateTime.parse(joinedStr, DateTimeFormatter.ISO_DATE_TIME)
        } catch (e: Exception) {
            LocalDateTime.now()
        }

        return CommunityUser(
            id = obj.optString("id", "user_unknown"),
            displayName = obj.optString("display_name", "Anonymous"),
            identityMode = identityMode,
            currentPhase = currentPhase,
            currentMood = currentMood,
            wisdomLevel = wisdomLevel,
            isVerifiedProfessional = obj.optBoolean("is_verified_professional", false),
            professionalTitle = obj.optString("professional_title", "").takeIf { it.isNotBlank() },
            joinedDate = joinedDate,
            localCircle = obj.optString("local_circle", "").takeIf { it.isNotBlank() }
        )
    }

    private fun parseReactions(array: org.json.JSONArray): List<PostReaction> {
        val parsed = mutableListOf<PostReaction>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val typeStr = obj.optString("type", "")
            val type = try { ReactionType.valueOf(typeStr) } catch (e: Exception) { null } ?: continue
            parsed.add(PostReaction(type, obj.optInt("count", 0), obj.optBoolean("has_user_reacted", false)))
        }
        // Fill in any missing reaction types with zero count
        val presentTypes = parsed.map { it.type }.toSet()
        ReactionType.values().filter { it !in presentTypes }.forEach {
            parsed.add(PostReaction(it, 0, false))
        }
        return parsed
    }

    private fun defaultAnonymousUser() = CommunityUser(
        id = "user_anon",
        displayName = "Anonymous",
        identityMode = IdentityMode.ANONYMOUS,
        currentPhase = null,
        currentMood = null,
        wisdomLevel = WisdomLevel.SEEDLING,
        isVerifiedProfessional = false,
        professionalTitle = null,
        joinedDate = LocalDateTime.now(),
        localCircle = null
    )

    private fun getMockPhaseRooms(): List<PhaseRoom> {
        return listOf(
            PhaseRoom(
                phase = CyclePhase.MENSTRUAL,
                activeUsers = 234,
                recentMessages = listOf(
                    PhaseRoomMessage(
                        id = "msg_1",
                        author = CommunityUser(
                            id = "u1", displayName = "Luna #1234",
                            identityMode = IdentityMode.ANONYMOUS,
                            currentPhase = CyclePhase.MENSTRUAL,
                            currentMood = UserMood.TIRED,
                            wisdomLevel = WisdomLevel.SPROUT,
                            isVerifiedProfessional = false, professionalTitle = null,
                            joinedDate = LocalDateTime.now(), localCircle = null
                        ),
                        content = "Day 2 here, heating pad is my best friend 🥲",
                        timestamp = LocalDateTime.now().minusMinutes(5),
                        isAnonymous = true
                    ),
                    PhaseRoomMessage(
                        id = "msg_2",
                        author = CommunityUser(
                            id = "u2", displayName = "CycleSister",
                            identityMode = IdentityMode.PSEUDONYM,
                            currentPhase = CyclePhase.MENSTRUAL,
                            currentMood = UserMood.PEACEFUL,
                            wisdomLevel = WisdomLevel.BLOOMING,
                            isVerifiedProfessional = false, professionalTitle = null,
                            joinedDate = LocalDateTime.now(), localCircle = "Lagos"
                        ),
                        content = "Ginger tea has been a lifesaver! Anyone else tried it?",
                        timestamp = LocalDateTime.now().minusMinutes(12),
                        isAnonymous = false
                    )
                )
            ),
            PhaseRoom(
                phase = CyclePhase.FOLLICULAR,
                activeUsers = 456,
                recentMessages = listOf(
                    PhaseRoomMessage(
                        id = "msg_3",
                        author = CommunityUser(
                            id = "u3", displayName = "MoonRiser",
                            identityMode = IdentityMode.PSEUDONYM,
                            currentPhase = CyclePhase.FOLLICULAR,
                            currentMood = UserMood.ENERGETIC,
                            wisdomLevel = WisdomLevel.BLOOMING,
                            isVerifiedProfessional = false, professionalTitle = null,
                            joinedDate = LocalDateTime.now(), localCircle = "Lagos"
                        ),
                        content = "Energy is UP today! Who else is feeling it? ⚡",
                        timestamp = LocalDateTime.now().minusMinutes(2),
                        isAnonymous = false
                    )
                )
            ),
            PhaseRoom(
                phase = CyclePhase.OVULATION,
                activeUsers = 312,
                recentMessages = emptyList()
            ),
            PhaseRoom(
                phase = CyclePhase.LUTEAL,
                activeUsers = 389,
                recentMessages = emptyList()
            )
        )
    }

    private fun getMockGroups(): List<CommunityGroup> {
        return listOf(
            CommunityGroup(
                id = "grp_1",
                name = "PCOS Warriors",
                description = "Support and tips for managing PCOS symptoms together",
                emoji = "💪",
                memberCount = 2345,
                category = GroupCategory.HEALTH_CONDITION,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFFAB47BC)
            ),
            CommunityGroup(
                id = "grp_2",
                name = "First Period Club",
                description = "Safe space for those new to menstruation",
                emoji = "🌸",
                memberCount = 1567,
                category = GroupCategory.LIFE_STAGE,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFFEC407A)
            ),
            CommunityGroup(
                id = "grp_3",
                name = "TTC Journey",
                description = "Trying to conceive - share experiences and support",
                emoji = "🍼",
                memberCount = 890,
                category = GroupCategory.LIFE_STAGE,
                privacy = GroupPrivacy.PRIVATE_REQUEST,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFF66BB6A)
            ),
            CommunityGroup(
                id = "grp_4",
                name = "Cycle Syncing Lifestyle",
                description = "Living in harmony with your cycle phases",
                emoji = "🌙",
                memberCount = 3456,
                category = GroupCategory.LIFESTYLE,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFF7B5EA7)
            ),
            CommunityGroup(
                id = "grp_5",
                name = "Endometriosis Support",
                description = "Community for endo warriors to connect and share",
                emoji = "🎗️",
                memberCount = 1234,
                category = GroupCategory.HEALTH_CONDITION,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFFFFA726)
            )
        )
    }

    private fun getMockLocalCircles(): List<CommunityGroup> {
        return listOf(
            CommunityGroup(
                id = "loc_1",
                name = "Lagos Lunas",
                description = "Connect with cycle sisters in Lagos, Nigeria",
                emoji = "🇳🇬",
                memberCount = 567,
                category = GroupCategory.LOCAL_CIRCLE,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFF66BB6A)
            ),
            CommunityGroup(
                id = "loc_2",
                name = "London Cycles",
                description = "UK-based community for cycle support",
                emoji = "🇬🇧",
                memberCount = 1234,
                category = GroupCategory.LOCAL_CIRCLE,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFF42A5F5)
            ),
            CommunityGroup(
                id = "loc_3",
                name = "NYC Moon Sisters",
                description = "New York community for menstrual health",
                emoji = "🗽",
                memberCount = 2345,
                category = GroupCategory.LOCAL_CIRCLE,
                privacy = GroupPrivacy.PUBLIC,
                createdBy = getMockCurrentUser(),
                coverColor = Color(0xFFAB47BC)
            )
        )
    }

    private fun getMockProfessionals(): List<CommunityUser> {
        return listOf(
            CommunityUser(
                id = "pro_1",
                displayName = "Dr. Amara O.",
                identityMode = IdentityMode.REAL_NAME,
                currentPhase = null,
                currentMood = null,
                wisdomLevel = WisdomLevel.WISE_TREE,
                isVerifiedProfessional = true,
                professionalTitle = "Gynecologist",
                joinedDate = LocalDateTime.now().minusYears(1),
                localCircle = "Lagos"
            ),
            CommunityUser(
                id = "pro_2",
                displayName = "Nurse Sarah K.",
                identityMode = IdentityMode.REAL_NAME,
                currentPhase = null,
                currentMood = null,
                wisdomLevel = WisdomLevel.WISE_TREE,
                isVerifiedProfessional = true,
                professionalTitle = "Women's Health Nurse",
                joinedDate = LocalDateTime.now().minusMonths(8),
                localCircle = "London"
            ),
            CommunityUser(
                id = "pro_3",
                displayName = "Dr. Chen Wei",
                identityMode = IdentityMode.REAL_NAME,
                currentPhase = null,
                currentMood = null,
                wisdomLevel = WisdomLevel.WISE_TREE,
                isVerifiedProfessional = true,
                professionalTitle = "Reproductive Endocrinologist",
                joinedDate = LocalDateTime.now().minusMonths(6),
                localCircle = "Singapore"
            )
        )
    }

    private fun getMockChallenges(): List<Challenge> {
        return listOf(
            Challenge(
                id = "chal_1",
                title = "7-Day Mood Tracker",
                description = "Log your mood every day for a week",
                emoji = "😊",
                type = ChallengeType.DAILY_CHECK_IN,
                durationDays = 7,
                pointsReward = 50,
                participantCount = 1234,
                currentProgress = 0.4f,
                startDate = LocalDateTime.now().minusDays(3),
                endDate = LocalDateTime.now().plusDays(4)
            ),
            Challenge(
                id = "chal_2",
                title = "Phase Ritual: Menstrual Rest",
                description = "Complete 3 self-care activities during your period",
                emoji = "🛁",
                type = ChallengeType.PHASE_RITUAL,
                durationDays = 5,
                pointsReward = 75,
                participantCount = 567,
                currentProgress = 0f,
                startDate = LocalDateTime.now(),
                endDate = LocalDateTime.now().plusDays(5)
            ),
            Challenge(
                id = "chal_3",
                title = "Hydration Hero",
                description = "Drink 8 glasses of water daily for 5 days",
                emoji = "💧",
                type = ChallengeType.WELLNESS,
                durationDays = 5,
                pointsReward = 40,
                participantCount = 2345,
                currentProgress = 0.6f,
                startDate = LocalDateTime.now().minusDays(3),
                endDate = LocalDateTime.now().plusDays(2)
            ),
            Challenge(
                id = "chal_4",
                title = "Community Connector",
                description = "Give 10 reactions to support other members",
                emoji = "🤗",
                type = ChallengeType.COMMUNITY,
                durationDays = 3,
                pointsReward = 30,
                participantCount = 890,
                currentProgress = 0.8f,
                startDate = LocalDateTime.now().minusDays(2),
                endDate = LocalDateTime.now().plusDays(1)
            ),
            Challenge(
                id = "chal_5",
                title = "Full Cycle Tracker",
                description = "Log symptoms every day for one complete cycle",
                emoji = "📊",
                type = ChallengeType.TRACKING,
                durationDays = 28,
                pointsReward = 200,
                participantCount = 456,
                currentProgress = 0.25f,
                startDate = LocalDateTime.now().minusDays(7),
                endDate = LocalDateTime.now().plusDays(21)
            )
        )
    }
}