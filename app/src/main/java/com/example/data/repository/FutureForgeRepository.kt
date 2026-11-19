package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.util.UUID

class FutureForgeRepository(
    private val db: FutureForgeDatabase,
    private val sessionPrefs: SessionPreferences
) {
    private val userDao = db.userDao()
    private val learningDao = db.learningDao()
    private val careerDao = db.careerDao()
    private val networkDao = db.networkDao()

    val activeUserIdFlow: Flow<String?> = sessionPrefs.activeUserIdFlow

    fun observeUser(userId: String): Flow<UserEntity?> = userDao.observeUserById(userId)
    fun observeAllUsers(): Flow<List<UserEntity>> = userDao.observeAllUsers()
    fun observeVerifiedMentors(): Flow<List<UserEntity>> = userDao.observeVerifiedMentors()

    // Learning & Career Compass
    fun observeSavedItems(userId: String) = learningDao.observeSavedItems(userId)
    fun observeActiveRoadmap(userId: String) = learningDao.observeActiveRoadmap(userId)
    fun observeMilestones(roadmapId: String) = learningDao.observeMilestones(roadmapId)
    fun observeExamAttempts(userId: String) = learningDao.observeExamAttempts(userId)
    fun observeDoubts(userId: String) = learningDao.observeDoubts(userId)
    fun observeCourseProgress(userId: String) = learningDao.observeCourseProgress(userId)
    fun observeCommPractice(userId: String) = learningDao.observeCommPractice(userId)
    fun observeInterviews(userId: String) = learningDao.observeInterviews(userId)
    fun observeStudyPlan(userId: String) = learningDao.observeStudyPlan(userId)

    // Career, Companies & Vacancies
    fun observeResumes(userId: String) = careerDao.observeResumes(userId)
    fun observeOpportunities() = careerDao.observeOpportunities()
    fun observeUserApplications(userId: String) = careerDao.observeUserApplications(userId)
    fun observeAllApplications() = careerDao.observeAllApplications()
    fun observeCompanies() = careerDao.observeCompanies()
    fun observeReviews() = careerDao.observeReviews()
    fun observeIndustryWatch() = careerDao.observeIndustryWatch()
    fun observeCustomerVoice() = careerDao.observeCustomerVoice()

    // Network, Videos, Community, Rewards, Admin
    fun observeMentorshipsForUser(userId: String) = networkDao.observeMentorshipsForUser(userId)
    fun observeAllMentorships() = networkDao.observeAllMentorships()
    fun observeQna() = networkDao.observeQna()
    fun observeVideos() = networkDao.observeVideos()
    fun observeWatchHistory(userId: String) = networkDao.observeWatchHistory(userId)
    fun observeLiveSessions() = networkDao.observeLiveSessions()
    fun observeSessionRegistrations(userId: String) = networkDao.observeSessionRegistrations(userId)
    fun observeCommunityPosts() = networkDao.observeCommunityPosts()
    fun observeAchievements(userId: String) = networkDao.observeAchievements(userId)
    fun observeNotifications(userId: String) = networkDao.observeNotifications(userId)
    fun observeReports() = networkDao.observeReports()

    suspend fun login(email: String, rawPassword: String): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        val user = userDao.getUserByEmail(cleanEmail)
            ?: return Result.failure(IllegalArgumentException("No account found for $cleanEmail"))
        if (user.suspended) {
            return Result.failure(IllegalStateException("This account is suspended by Platform Governance."))
        }
        val expectedHash = FutureForgeDatabase.hashPassword(rawPassword)
        if (user.passwordHash != expectedHash) {
            return Result.failure(IllegalArgumentException("Incorrect password. Please verify and try again."))
        }
        sessionPrefs.setActiveUserId(user.id)
        return Result.success(user)
    }

    suspend fun register(
        name: String,
        email: String,
        rawPassword: String,
        role: UserRole,
        organizationOrHeadline: String,
        adminInviteCode: String = ""
    ): Result<UserEntity> {
        val cleanEmail = email.trim().lowercase()
        if (name.isBlank() || cleanEmail.isBlank() || rawPassword.length < 6) {
            return Result.failure(IllegalArgumentException("Provide a name, valid email, and password (6+ chars)."))
        }
        if (userDao.getUserByEmail(cleanEmail) != null) {
            return Result.failure(IllegalArgumentException("An account with this email already exists."))
        }
        if (role == UserRole.ADMIN && adminInviteCode.trim() != "FF-ADMIN-2026") {
            return Result.failure(IllegalArgumentException("Administrator registration requires invite code FF-ADMIN-2026."))
        }
        val verification = when (role) {
            UserRole.STUDENT, UserRole.ADMIN -> "VERIFIED"
            UserRole.PROFESSIONAL, UserRole.RECRUITER -> "PENDING"
        }
        val user = UserEntity(
            id = "usr_${UUID.randomUUID().toString().take(8)}",
            name = name.trim(),
            email = cleanEmail,
            passwordHash = FutureForgeDatabase.hashPassword(rawPassword),
            role = role.name,
            verificationStatus = verification,
            headline = organizationOrHeadline.trim().ifBlank { role.label },
            companyName = if (role == UserRole.RECRUITER) organizationOrHeadline.trim() else ""
        )
        userDao.upsertUser(user)
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Welcome to Future Forge",
                message = if (verification == "PENDING") {
                    "Your ${role.label} account is created and queued for Admin verification."
                } else {
                    "Your ${role.label} workspace is ready. Forge your own destiny."
                },
                category = "ACCOUNT"
            )
        )
        sessionPrefs.setActiveUserId(user.id)
        return Result.success(user)
    }

    suspend fun logout() {
        sessionPrefs.setActiveUserId(null)
    }

    suspend fun updateUserProfile(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun toggleSaveItem(userId: String, itemType: String, itemId: String, title: String, subtitle: String) {
        val existing = learningDao.observeSavedItems(userId).first().any { it.itemId == itemId }
        if (existing) {
            learningDao.deleteSavedItem(userId, itemId)
        } else {
            learningDao.insertSavedItem(
                SavedItemEntity(
                    id = "sav_${userId}_$itemId",
                    userId = userId,
                    itemType = itemType,
                    itemId = itemId,
                    title = title,
                    subtitle = subtitle
                )
            )
        }
    }

    suspend fun saveGeneratedRoadmap(
        userId: String,
        targetCareer: String,
        summary: String,
        educationSnapshot: String,
        skillsSnapshot: String,
        milestones: List<RoadmapMilestoneEntity>
    ) {
        val roadmapId = "rm_${userId}_active"
        learningDao.upsertRoadmap(
            RoadmapEntity(
                id = roadmapId,
                userId = userId,
                targetCareer = targetCareer,
                summary = summary,
                educationSnapshot = educationSnapshot,
                skillsSnapshot = skillsSnapshot
            )
        )
        learningDao.clearMilestonesForRoadmap(roadmapId)
        learningDao.insertMilestones(milestones.map { it.copy(roadmapId = roadmapId, userId = userId) })
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = userId,
                title = "Destiny Roadmap Updated",
                message = "New ${milestones.size}-stage pathway forged for $targetCareer.",
                category = "ROADMAP"
            )
        )
    }

    suspend fun toggleMilestone(milestone: RoadmapMilestoneEntity) {
        val updated = milestone.copy(completed = !milestone.completed)
        learningDao.updateMilestone(updated)
        if (updated.completed) {
            unlockAchievement(
                userId = milestone.userId,
                badgeKey = "FIRST_MILESTONE",
                title = "Destiny Architect",
                description = "Completed your first Destiny Roadmap milestone.",
                points = 50,
                category = "Roadmap"
            )
        }
    }

    suspend fun toggleMilestoneReminder(milestone: RoadmapMilestoneEntity) {
        learningDao.updateMilestone(milestone.copy(reminderEnabled = !milestone.reminderEnabled))
    }

    suspend fun recordExamAttempt(attempt: ExamAttemptEntity) {
        learningDao.insertExamAttempt(attempt)
        if (attempt.scorePercent >= 70) {
            unlockAchievement(
                userId = attempt.userId,
                badgeKey = "EXAM_HONORS",
                title = "Scholarly Precision",
                description = "Scored 70%+ on a timed Future Forge examination.",
                points = 100,
                category = "Exam Hub"
            )
        }
    }

    suspend fun saveDoubt(doubt: DoubtEntity) {
        learningDao.upsertDoubt(doubt)
    }

    suspend fun saveCourseProgress(progress: CourseProgressEntity) {
        learningDao.upsertCourseProgress(progress)
        if (progress.completed) {
            unlockAchievement(
                userId = progress.userId,
                badgeKey = "COURSE_FINISHER",
                title = "Mastery Forged",
                description = "Completed a full Skill Forge learning path with project & quiz.",
                points = 150,
                category = "Skill Forge"
            )
        }
    }

    suspend fun recordCommPractice(practice: CommunicationPracticeEntity) {
        learningDao.insertCommPractice(practice)
        if (practice.score >= 75) {
            unlockAchievement(
                userId = practice.userId,
                badgeKey = "ORATOR_SPARK",
                title = "Eloquent Voice",
                description = "Achieved 75+ score in Communication Lab.",
                points = 60,
                category = "Communication"
            )
        }
    }

    suspend fun saveInterviewSession(session: InterviewSessionEntity) {
        learningDao.insertInterview(session)
        unlockAchievement(
            userId = session.userId,
            badgeKey = "ARENA_CHALLENGER",
            title = "Arena Contender",
            description = "Completed a full interactive AI Interview Arena session.",
            points = 80,
            category = "Interview"
        )
    }

    suspend fun upsertStudyTask(task: StudyPlanTaskEntity) {
        learningDao.upsertStudyTask(task)
    }

    suspend fun deleteStudyTask(taskId: String) {
        learningDao.deleteStudyTask(taskId)
    }

    suspend fun upsertResume(resume: ResumeEntity) {
        careerDao.upsertResume(resume)
        unlockAchievement(
            userId = resume.userId,
            badgeKey = "PORTFOLIO_CRAFTER",
            title = "Portfolio Builder",
            description = "Created or updated a structured career resume & portfolio.",
            points = 50,
            category = "Resume Studio"
        )
    }

    suspend fun deleteResume(resumeId: String) {
        careerDao.deleteResume(resumeId)
    }

    suspend fun submitApplication(application: ApplicationEntity) {
        careerDao.upsertApplication(application)
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = application.userId,
                title = "Application Submitted",
                message = "Applied to ${application.opportunityTitle} at ${application.companyName}.",
                category = "APPLICATION"
            )
        )
        unlockAchievement(
            userId = application.userId,
            badgeKey = "FIRST_APPLICATION",
            title = "Gateway Pioneer",
            description = "Submitted your first private-sector application through Opportunity Gateway.",
            points = 70,
            category = "Careers"
        )
    }

    suspend fun updateApplicationRecruiterAction(
        application: ApplicationEntity,
        newStatus: String,
        assessmentScore: Int?,
        interviewDate: String,
        feedback: String
    ) {
        val updated = application.copy(
            status = newStatus,
            assessmentScore = assessmentScore ?: application.assessmentScore,
            interviewDate = interviewDate.ifBlank { application.interviewDate },
            recruiterFeedback = feedback.ifBlank { application.recruiterFeedback }
        )
        careerDao.upsertApplication(updated)
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = application.userId,
                title = "Application Update: ${application.companyName}",
                message = "Status changed to $newStatus for ${application.opportunityTitle}.",
                category = "APPLICATION"
            )
        )
    }

    suspend fun publishOpportunity(opportunity: JobOpportunityEntity) {
        careerDao.upsertOpportunity(opportunity)
    }

    suspend fun upsertCompanyProfile(company: CompanyProfileEntity) {
        careerDao.upsertCompany(company)
    }

    suspend fun requestMentorship(mentorship: MentorshipEntity) {
        networkDao.upsertMentorship(mentorship)
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = mentorship.mentorId,
                title = if (mentorship.isMockInterview) "New Mock Interview Request" else "New Mentorship Request",
                message = "${mentorship.studentName} requested a session on ${mentorship.topic}.",
                category = "MENTORSHIP"
            )
        )
    }

    suspend fun updateMentorshipStatus(mentorship: MentorshipEntity, status: String, feedback: String) {
        networkDao.upsertMentorship(mentorship.copy(status = status, feedback = feedback))
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = mentorship.studentId,
                title = "Mentor Session $status",
                message = "${mentorship.mentorName} updated your ${mentorship.topic} session to $status.",
                category = "MENTORSHIP"
            )
        )
    }

    suspend fun postQuestion(qna: ProfessionalQnaEntity) {
        networkDao.upsertQna(qna)
    }

    suspend fun answerQuestion(qna: ProfessionalQnaEntity, answer: String, mentor: UserEntity) {
        networkDao.upsertQna(
            qna.copy(
                answerText = answer,
                answeredByName = mentor.name,
                answeredByRole = mentor.headline.ifBlank { "Verified Professional" }
            )
        )
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = qna.authorId,
                title = "Professional Answered Your Question",
                message = "${mentor.name} answered: ${qna.question.take(50)}",
                category = "QNA"
            )
        )
    }

    suspend fun publishVideo(video: VideoItemEntity) {
        networkDao.upsertVideo(video)
    }

    suspend fun updateVideoWatchProgress(userId: String, videoId: String, positionMs: Long, durationMs: Long) {
        val completed = durationMs > 0 && positionMs >= (durationMs * 0.85).toLong()
        networkDao.upsertWatchHistory(
            VideoWatchEntity(
                userId = userId,
                videoId = videoId,
                positionMs = positionMs,
                durationMs = durationMs,
                completed = completed
            )
        )
    }

    suspend fun hostLiveSession(session: LiveSessionEntity) {
        networkDao.upsertLiveSession(session)
    }

    suspend fun toggleSessionRegistration(userId: String, session: LiveSessionEntity) {
        val existing = networkDao.observeSessionRegistrations(userId).first().any { it.sessionId == session.id }
        if (existing) {
            networkDao.deleteSessionRegistration(userId, session.id)
        } else {
            networkDao.insertSessionRegistration(SessionRegistrationEntity(userId, session.id, true))
            networkDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    title = "Session Reminder Armed",
                    message = "Registered for ${session.title} (${session.scheduleLabel}).",
                    category = "WEBINAR"
                )
            )
        }
    }

    suspend fun addEmployeeReview(review: EmployeeReviewEntity) {
        careerDao.upsertReview(review)
    }

    suspend fun respondToCustomerVoice(item: CustomerVoiceEntity, response: String) {
        careerDao.upsertCustomerVoice(item.copy(companyResponse = response))
    }

    suspend fun addCommunityPost(post: CommunityPostEntity) {
        networkDao.upsertCommunityPost(post)
    }

    suspend fun upvoteCommunityPost(post: CommunityPostEntity) {
        networkDao.upsertCommunityPost(post.copy(likesCount = post.likesCount + 1))
    }

    suspend fun submitContentReport(
        reporterId: String,
        targetType: String,
        targetId: String,
        targetTitle: String,
        reason: String
    ) {
        networkDao.upsertReport(
            ContentReportEntity(
                id = "rep_${UUID.randomUUID().toString().take(8)}",
                reporterId = reporterId,
                targetType = targetType,
                targetId = targetId,
                targetTitle = targetTitle,
                reason = reason
            )
        )
        if (targetType == "REVIEW") {
            val rev = careerDao.observeReviews().first().firstOrNull { it.id == targetId }
            if (rev != null) {
                careerDao.upsertReview(rev.copy(disputeStatus = "REPORTED", disputeNotes = reason))
            }
        } else if (targetType == "COMMUNITY_POST") {
            val post = networkDao.observeCommunityPosts().first().firstOrNull { it.id == targetId }
            if (post != null) {
                networkDao.upsertCommunityPost(post.copy(reportCount = post.reportCount + 1, moderationStatus = "FLAGGED"))
            }
        }
    }

    suspend fun adminVerifyUser(user: UserEntity, newStatus: String) {
        userDao.updateUser(user.copy(verificationStatus = newStatus))
        networkDao.insertNotification(
            NotificationEntity(
                id = "notif_${UUID.randomUUID().toString().take(8)}",
                userId = user.id,
                title = "Account Verification Updated",
                message = "Administrator marked your verification status as $newStatus.",
                category = "GOVERNANCE"
            )
        )
    }

    suspend fun adminToggleSuspendUser(user: UserEntity) {
        userDao.updateUser(user.copy(suspended = !user.suspended))
    }

    suspend fun adminVerifyCompany(company: CompanyProfileEntity, verified: Boolean) {
        careerDao.upsertCompany(company.copy(verified = verified))
    }

    suspend fun adminModerateOpportunity(opportunity: JobOpportunityEntity, verified: Boolean, status: String) {
        careerDao.upsertOpportunity(opportunity.copy(verified = verified, hiringStatus = status))
    }

    suspend fun adminToggleVideoApproval(video: VideoItemEntity) {
        networkDao.upsertVideo(video.copy(isApproved = !video.isApproved))
    }

    suspend fun adminResolveReport(report: ContentReportEntity, removeTarget: Boolean) {
        networkDao.upsertReport(report.copy(status = if (removeTarget) "ACTION_TAKEN" else "DISMISSED"))
        when (report.targetType) {
            "REVIEW" -> {
                val rev = careerDao.observeReviews().first().firstOrNull { it.id == report.targetId }
                if (rev != null) {
                    careerDao.upsertReview(rev.copy(disputeStatus = if (removeTarget) "REMOVED" else "RESOLVED"))
                }
            }
            "COMMUNITY_POST" -> {
                val post = networkDao.observeCommunityPosts().first().firstOrNull { it.id == report.targetId }
                if (post != null) {
                    networkDao.upsertCommunityPost(post.copy(moderationStatus = if (removeTarget) "REMOVED" else "ACTIVE"))
                }
            }
            "QNA" -> {
                val qna = networkDao.observeQna().first().firstOrNull { it.id == report.targetId }
                if (qna != null) {
                    networkDao.upsertQna(qna.copy(moderationStatus = if (removeTarget) "REMOVED" else "APPROVED"))
                }
            }
        }
    }

    suspend fun markAllNotificationsRead(userId: String) {
        networkDao.markAllNotificationsRead(userId)
    }

    private suspend fun unlockAchievement(
        userId: String,
        badgeKey: String,
        title: String,
        description: String,
        points: Int,
        category: String
    ) {
        val id = "ach_${userId}_$badgeKey"
        val existing = networkDao.observeAchievements(userId).first().any { it.id == id }
        if (!existing) {
            networkDao.insertAchievement(
                AchievementEntity(
                    id = id,
                    userId = userId,
                    badgeKey = badgeKey,
                    title = title,
                    description = description,
                    points = points,
                    category = category
                )
            )
            networkDao.insertNotification(
                NotificationEntity(
                    id = "notif_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    title = "Achievement Unlocked: $title",
                    message = "$description (+$points pts)",
                    category = "REWARD"
                )
            )
        }
    }
}
