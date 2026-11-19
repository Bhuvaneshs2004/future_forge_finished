package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole(val label: String) {
    STUDENT("Student / Job Seeker"),
    PROFESSIONAL("Professional / Mentor"),
    RECRUITER("Company / Recruiter"),
    ADMIN("Administrator")
}

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val passwordHash: String,
    val role: String,
    val verificationStatus: String = "VERIFIED", // PENDING, VERIFIED, REJECTED
    val suspended: Boolean = false,
    val educationLevel: String = "",
    val qualifications: String = "",
    val interests: String = "",
    val strengths: String = "",
    val skills: String = "",
    val careerAmbitions: String = "",
    val headline: String = "",
    val employmentHistory: String = "",
    val expertise: String = "",
    val availability: String = "",
    val companyName: String = "",
    val companyIndustry: String = "",
    val privacyPublicProfile: Boolean = true,
    val privacyShareResume: Boolean = true,
    val privacyYouthSafeMode: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val itemType: String, // CAREER, COURSE, COMPANY, OPPORTUNITY, VIDEO
    val itemId: String,
    val title: String,
    val subtitle: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "roadmaps")
data class RoadmapEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val targetCareer: String,
    val summary: String,
    val educationSnapshot: String,
    val skillsSnapshot: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "roadmap_milestones")
data class RoadmapMilestoneEntity(
    @PrimaryKey val id: String,
    val roadmapId: String,
    val userId: String,
    val stage: String,
    val title: String,
    val description: String,
    val linkedModule: String,
    val targetWeek: String,
    val reminderEnabled: Boolean = false,
    val completed: Boolean = false,
    val orderIndex: Int = 0
)

@Entity(tableName = "exam_attempts")
data class ExamAttemptEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val examId: String,
    val examTitle: String,
    val category: String,
    val scorePercent: Int,
    val correctCount: Int,
    val incorrectCount: Int,
    val totalQuestions: Int,
    val durationSeconds: Int,
    val reviewSummary: String,
    val takenAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "doubts")
data class DoubtEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val questionText: String,
    val imageUri: String? = null,
    val subject: String,
    val topic: String,
    val explanation: String,
    val followUpHistory: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "course_progress")
data class CourseProgressEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val courseId: String,
    val courseTitle: String,
    val completedLessons: String = "", // comma-separated lesson IDs
    val projectSubmitted: Boolean = false,
    val projectNotes: String = "",
    val quizScore: Int = 0,
    val completed: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "communication_practice")
data class CommunicationPracticeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mode: String, // SPEAKING, VOCABULARY, GRAMMAR, SENTENCE, PUBLIC_SPEAKING
    val prompt: String,
    val userResponse: String,
    val aiFeedback: String,
    val score: Int,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "interview_sessions")
data class InterviewSessionEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val mode: String, // HR, TECHNICAL, APTITUDE, ROLE_SPECIFIC
    val roleTitle: String,
    val companyName: String,
    val transcript: String,
    val clarityScore: Int,
    val relevanceScore: Int,
    val feedbackSummary: String,
    val recommendedTopics: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "resumes")
data class ResumeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val versionTitle: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val summary: String,
    val education: String,
    val experience: String,
    val skills: String,
    val projects: String,
    val portfolioLinks: String,
    val coverLetter: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "opportunities")
data class JobOpportunityEntity(
    @PrimaryKey val id: String,
    val title: String,
    val companyId: String,
    val companyName: String,
    val type: String, // INTERNSHIP, FRESHER, EXPERIENCED, SCHOLARSHIP, COMPETITION, APPRENTICESHIP
    val department: String,
    val location: String,
    val eligibility: String,
    val requiredSkills: String,
    val salaryOrStipend: String,
    val deadline: String,
    val instructions: String,
    val sourceType: String, // OFFICIAL_EMPLOYER, TRUSTED_FEED, THIRD_PARTY
    val sourceName: String,
    val lastUpdated: String,
    val verified: Boolean = true,
    val hiringStatus: String = "OPEN", // OPEN, CAMPUS_DRIVE, CLOSING_SOON, CLOSED
    val postedByUserId: String = "seed_recruiter"
)

@Entity(tableName = "applications")
data class ApplicationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val applicantName: String,
    val applicantSkills: String,
    val opportunityId: String,
    val opportunityTitle: String,
    val companyName: String,
    val resumeVersionTitle: String,
    val coverNote: String,
    val status: String = "SUBMITTED", // SUBMITTED, ASSESSMENT_SENT, SHORTLISTED, INTERVIEW_SCHEDULED, FEEDBACK_SHARED, ACCEPTED, REJECTED
    val assessmentScore: Int? = null,
    val interviewDate: String = "",
    val recruiterFeedback: String = "",
    val appliedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "mentorships")
data class MentorshipEntity(
    @PrimaryKey val id: String,
    val studentId: String,
    val studentName: String,
    val mentorId: String,
    val mentorName: String,
    val topic: String,
    val preferredSlot: String,
    val isMockInterview: Boolean = false,
    val status: String = "REQUESTED", // REQUESTED, SCHEDULED, COMPLETED, DECLINED
    val feedback: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "professional_qna")
data class ProfessionalQnaEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val category: String,
    val question: String,
    val answerText: String = "",
    val answeredByName: String = "",
    val answeredByRole: String = "",
    val moderationStatus: String = "APPROVED", // APPROVED, FLAGGED, REMOVED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class VideoItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val creatorId: String,
    val creatorName: String,
    val creatorRole: String,
    val category: String, // EDUCATIONAL, INDUSTRY_EXPERT, CAREER_GUIDANCE, DAY_IN_CAREER, INTERVIEW, RECORDED_WEBINAR
    val industry: String,
    val videoUrl: String,
    val durationLabel: String,
    val dayInCareerSummary: String = "",
    val isApproved: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "video_watch_history", primaryKeys = ["userId", "videoId"])
data class VideoWatchEntity(
    val userId: String,
    val videoId: String,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val completed: Boolean = false,
    val lastWatchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_sessions")
data class LiveSessionEntity(
    @PrimaryKey val id: String,
    val title: String,
    val hostId: String,
    val hostName: String,
    val hostTitle: String,
    val topic: String,
    val scheduleLabel: String,
    val description: String,
    val recordingUrl: String = "",
    val liveNotes: String = ""
)

@Entity(tableName = "session_registrations", primaryKeys = ["userId", "sessionId"])
data class SessionRegistrationEntity(
    val userId: String,
    val sessionId: String,
    val reminderEnabled: Boolean = true,
    val registeredAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "companies")
data class CompanyProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val industry: String,
    val productsServices: String,
    val locations: String,
    val roleQualifications: String,
    val requiredSkills: String,
    val toolsTech: String,
    val certifications: String,
    val recruitmentStages: String,
    val fresherInfo: String,
    val internshipInfo: String,
    val prepChecklist: String,
    val salaryBandSummary: String,
    val sourceType: String,
    val lastUpdated: String,
    val verified: Boolean = true
)

@Entity(tableName = "employee_reviews")
data class EmployeeReviewEntity(
    @PrimaryKey val id: String,
    val companyId: String,
    val companyName: String,
    val authorName: String,
    val roleTitle: String,
    val verificationType: String, // VERIFIED_EMPLOYEE, UNVERIFIED, ANONYMOUS
    val workCultureRating: Int,
    val workLifeBalanceRating: Int,
    val managementRating: Int,
    val learningRating: Int,
    val hybridRemotePolicy: String,
    val interviewExperience: String,
    val reviewText: String,
    val disputeStatus: String = "NONE", // NONE, REPORTED, UNDER_REVIEW, RESOLVED, REMOVED
    val disputeNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "industry_watch")
data class IndustryWatchEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val sector: String,
    val category: String, // EXPANSION, NEW_PRODUCT, HIRING_TREND, LAYOFF_REPORT, PARTNERSHIP_MA
    val headline: String,
    val summary: String,
    val inDemandSkills: String,
    val sourceName: String,
    val publishedDate: String
)

@Entity(tableName = "customer_voice")
data class CustomerVoiceEntity(
    @PrimaryKey val id: String,
    val companyName: String,
    val category: String, // PRODUCT_REVIEW, SERVICE_REVIEW, COMPLAINT_THEME, CASE_STUDY
    val rating: Int,
    val summary: String,
    val companyResponse: String,
    val businessCaseTakeaway: String,
    val sourceName: String,
    val publishedDate: String
)

@Entity(tableName = "study_plan_tasks")
data class StudyPlanTaskEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val subject: String,
    val dueDate: String,
    val durationMinutes: Int,
    val priority: String,
    val reminderEnabled: Boolean = true,
    val completed: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_posts")
data class CommunityPostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val category: String, // EXAM_PREP, CODING, CAREER_ADVICE, CHALLENGE, STUDY_NOTES
    val title: String,
    val content: String,
    val likesCount: Int = 0,
    val reportCount: Int = 0,
    val moderationStatus: String = "ACTIVE", // ACTIVE, FLAGGED, REMOVED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "achievements")
data class AchievementEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val badgeKey: String,
    val title: String,
    val description: String,
    val points: Int,
    val category: String,
    val earnedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val message: String,
    val category: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_reports")
data class ContentReportEntity(
    @PrimaryKey val id: String,
    val reporterId: String,
    val targetType: String, // REVIEW, COMMUNITY_POST, QNA, VIDEO, VACANCY
    val targetId: String,
    val targetTitle: String,
    val reason: String,
    val status: String = "OPEN", // OPEN, DISMISSED, ACTION_TAKEN
    val createdAt: Long = System.currentTimeMillis()
)
