package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    fun observeUserById(id: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: String): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun observeAllUsers(): Flow<List<UserEntity>>

    @Query("SELECT * FROM users WHERE role = 'PROFESSIONAL' AND verificationStatus = 'VERIFIED'")
    fun observeVerifiedMentors(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertUser(user: UserEntity)

    @Update
    suspend fun updateUser(user: UserEntity)
}

@Dao
interface LearningDao {
    @Query("SELECT * FROM saved_items WHERE userId = :userId ORDER BY savedAt DESC")
    fun observeSavedItems(userId: String): Flow<List<SavedItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedItem(item: SavedItemEntity)

    @Query("DELETE FROM saved_items WHERE userId = :userId AND itemId = :itemId")
    suspend fun deleteSavedItem(userId: String, itemId: String)

    @Query("SELECT * FROM roadmaps WHERE userId = :userId ORDER BY updatedAt DESC LIMIT 1")
    fun observeActiveRoadmap(userId: String): Flow<RoadmapEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoadmap(roadmap: RoadmapEntity)

    @Query("SELECT * FROM roadmap_milestones WHERE roadmapId = :roadmapId ORDER BY orderIndex ASC")
    fun observeMilestones(roadmapId: String): Flow<List<RoadmapMilestoneEntity>>

    @Query("DELETE FROM roadmap_milestones WHERE roadmapId = :roadmapId")
    suspend fun clearMilestonesForRoadmap(roadmapId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMilestones(milestones: List<RoadmapMilestoneEntity>)

    @Update
    suspend fun updateMilestone(milestone: RoadmapMilestoneEntity)

    @Query("SELECT * FROM exam_attempts WHERE userId = :userId ORDER BY takenAt DESC")
    fun observeExamAttempts(userId: String): Flow<List<ExamAttemptEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExamAttempt(attempt: ExamAttemptEntity)

    @Query("SELECT * FROM doubts WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeDoubts(userId: String): Flow<List<DoubtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDoubt(doubt: DoubtEntity)

    @Query("SELECT * FROM course_progress WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeCourseProgress(userId: String): Flow<List<CourseProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCourseProgress(progress: CourseProgressEntity)

    @Query("SELECT * FROM communication_practice WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeCommPractice(userId: String): Flow<List<CommunicationPracticeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommPractice(practice: CommunicationPracticeEntity)

    @Query("SELECT * FROM interview_sessions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeInterviews(userId: String): Flow<List<InterviewSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInterview(session: InterviewSessionEntity)

    @Query("SELECT * FROM study_plan_tasks WHERE userId = :userId ORDER BY completed ASC, createdAt DESC")
    fun observeStudyPlan(userId: String): Flow<List<StudyPlanTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertStudyTask(task: StudyPlanTaskEntity)

    @Query("DELETE FROM study_plan_tasks WHERE id = :taskId")
    suspend fun deleteStudyTask(taskId: String)
}

@Dao
interface CareerDao {
    @Query("SELECT * FROM resumes WHERE userId = :userId ORDER BY updatedAt DESC")
    fun observeResumes(userId: String): Flow<List<ResumeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertResume(resume: ResumeEntity)

    @Query("DELETE FROM resumes WHERE id = :resumeId")
    suspend fun deleteResume(resumeId: String)

    @Query("SELECT * FROM opportunities WHERE hiringStatus != 'REMOVED' ORDER BY lastUpdated DESC")
    fun observeOpportunities(): Flow<List<JobOpportunityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertOpportunity(opportunity: JobOpportunityEntity)

    @Query("DELETE FROM opportunities WHERE id = :id")
    suspend fun deleteOpportunity(id: String)

    @Query("SELECT * FROM applications WHERE userId = :userId ORDER BY appliedAt DESC")
    fun observeUserApplications(userId: String): Flow<List<ApplicationEntity>>

    @Query("SELECT * FROM applications ORDER BY appliedAt DESC")
    fun observeAllApplications(): Flow<List<ApplicationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertApplication(application: ApplicationEntity)

    @Query("SELECT * FROM companies ORDER BY verified DESC, name ASC")
    fun observeCompanies(): Flow<List<CompanyProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompany(company: CompanyProfileEntity)

    @Query("SELECT * FROM employee_reviews WHERE disputeStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun observeReviews(): Flow<List<EmployeeReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReview(review: EmployeeReviewEntity)

    @Query("SELECT * FROM industry_watch ORDER BY publishedDate DESC")
    fun observeIndustryWatch(): Flow<List<IndustryWatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertIndustryWatch(item: IndustryWatchEntity)

    @Query("SELECT * FROM customer_voice ORDER BY publishedDate DESC")
    fun observeCustomerVoice(): Flow<List<CustomerVoiceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomerVoice(item: CustomerVoiceEntity)
}

@Dao
interface NetworkDao {
    @Query("SELECT * FROM mentorships WHERE studentId = :userId OR mentorId = :userId ORDER BY createdAt DESC")
    fun observeMentorshipsForUser(userId: String): Flow<List<MentorshipEntity>>

    @Query("SELECT * FROM mentorships ORDER BY createdAt DESC")
    fun observeAllMentorships(): Flow<List<MentorshipEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMentorship(mentorship: MentorshipEntity)

    @Query("SELECT * FROM professional_qna WHERE moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun observeQna(): Flow<List<ProfessionalQnaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQna(qna: ProfessionalQnaEntity)

    @Query("SELECT * FROM videos ORDER BY createdAt DESC")
    fun observeVideos(): Flow<List<VideoItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: VideoItemEntity)

    @Query("SELECT * FROM video_watch_history WHERE userId = :userId ORDER BY lastWatchedAt DESC")
    fun observeWatchHistory(userId: String): Flow<List<VideoWatchEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(watch: VideoWatchEntity)

    @Query("SELECT * FROM live_sessions ORDER BY id ASC")
    fun observeLiveSessions(): Flow<List<LiveSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertLiveSession(session: LiveSessionEntity)

    @Query("SELECT * FROM session_registrations WHERE userId = :userId")
    fun observeSessionRegistrations(userId: String): Flow<List<SessionRegistrationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSessionRegistration(reg: SessionRegistrationEntity)

    @Query("DELETE FROM session_registrations WHERE userId = :userId AND sessionId = :sessionId")
    suspend fun deleteSessionRegistration(userId: String, sessionId: String)

    @Query("SELECT * FROM community_posts WHERE moderationStatus != 'REMOVED' ORDER BY createdAt DESC")
    fun observeCommunityPosts(): Flow<List<CommunityPostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCommunityPost(post: CommunityPostEntity)

    @Query("SELECT * FROM achievements WHERE userId = :userId ORDER BY earnedAt DESC")
    fun observeAchievements(userId: String): Flow<List<AchievementEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAchievement(achievement: AchievementEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeNotifications(userId: String): Flow<List<NotificationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: String)

    @Query("SELECT * FROM content_reports ORDER BY createdAt DESC")
    fun observeReports(): Flow<List<ContentReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertReport(report: ContentReportEntity)
}
