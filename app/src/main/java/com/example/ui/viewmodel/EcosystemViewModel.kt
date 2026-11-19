package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.ai.JobReadinessResult
import com.example.data.local.*
import com.example.data.repository.FutureForgeRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class EcosystemViewModel(
    private val repository: FutureForgeRepository,
    private val aiRepository: AiRepository,
    private val currentUserId: String
) : ViewModel() {

    val allUsers: StateFlow<List<UserEntity>> = repository.observeAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val verifiedMentors: StateFlow<List<UserEntity>> = repository.observeVerifiedMentors()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val resumes: StateFlow<List<ResumeEntity>> = repository.observeResumes(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val opportunities: StateFlow<List<JobOpportunityEntity>> = repository.observeOpportunities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val myApplications: StateFlow<List<ApplicationEntity>> = repository.observeUserApplications(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allApplications: StateFlow<List<ApplicationEntity>> = repository.observeAllApplications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val companies: StateFlow<List<CompanyProfileEntity>> = repository.observeCompanies()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val reviews: StateFlow<List<EmployeeReviewEntity>> = repository.observeReviews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val industryWatch: StateFlow<List<IndustryWatchEntity>> = repository.observeIndustryWatch()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val customerVoice: StateFlow<List<CustomerVoiceEntity>> = repository.observeCustomerVoice()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val myMentorships: StateFlow<List<MentorshipEntity>> = repository.observeMentorshipsForUser(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val allMentorships: StateFlow<List<MentorshipEntity>> = repository.observeAllMentorships()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val qnaList: StateFlow<List<ProfessionalQnaEntity>> = repository.observeQna()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val videos: StateFlow<List<VideoItemEntity>> = repository.observeVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val watchHistory: StateFlow<List<VideoWatchEntity>> = repository.observeWatchHistory(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val liveSessions: StateFlow<List<LiveSessionEntity>> = repository.observeLiveSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val sessionRegistrations: StateFlow<List<SessionRegistrationEntity>> = repository.observeSessionRegistrations(currentUserId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val communityPosts: StateFlow<List<CommunityPostEntity>> = repository.observeCommunityPosts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val reports: StateFlow<List<ContentReportEntity>> = repository.observeReports()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _readinessState = MutableStateFlow<AiCallState<JobReadinessResult>>(AiCallState.Idle)
    val readinessState: StateFlow<AiCallState<JobReadinessResult>> = _readinessState.asStateFlow()

    fun analyzeReadiness(
        profile: UserEntity,
        completedCoursesCount: Int,
        completedProjectsCount: Int,
        avgExamScore: Int,
        jobTitle: String,
        jobDescription: String
    ) {
        if (jobDescription.isBlank()) return
        viewModelScope.launch {
            _readinessState.value = AiCallState.Loading
            try {
                val res = aiRepository.analyzeJobReadiness(
                    profile = profile,
                    completedCoursesCount = completedCoursesCount,
                    completedProjectsCount = completedProjectsCount,
                    avgExamScore = avgExamScore,
                    jobTitle = jobTitle.ifBlank { "Target Private-Sector Role" },
                    jobDescription = jobDescription
                )
                _readinessState.value = AiCallState.Success(res)
            } catch (e: Exception) {
                _readinessState.value = AiCallState.Error(e.localizedMessage ?: "Analysis failed")
            }
        }
    }

    fun saveResume(resume: ResumeEntity) {
        viewModelScope.launch { repository.upsertResume(resume) }
    }

    fun deleteResume(resumeId: String) {
        viewModelScope.launch { repository.deleteResume(resumeId) }
    }

    fun applyToOpportunity(
        user: UserEntity,
        opportunity: JobOpportunityEntity,
        resumeVersionTitle: String,
        coverNote: String
    ) {
        viewModelScope.launch {
            repository.submitApplication(
                ApplicationEntity(
                    id = "app_${UUID.randomUUID().toString().take(8)}",
                    userId = user.id,
                    applicantName = user.name,
                    applicantSkills = user.skills,
                    opportunityId = opportunity.id,
                    opportunityTitle = opportunity.title,
                    companyName = opportunity.companyName,
                    resumeVersionTitle = resumeVersionTitle.ifBlank { "Default Profile Resume" },
                    coverNote = coverNote
                )
            )
        }
    }

    fun updateApplicationRecruiter(
        app: ApplicationEntity,
        status: String,
        assessmentScore: Int?,
        interviewDate: String,
        feedback: String
    ) {
        viewModelScope.launch {
            repository.updateApplicationRecruiterAction(app, status, assessmentScore, interviewDate, feedback)
        }
    }

    fun publishOpportunity(
        title: String,
        companyName: String,
        type: String,
        department: String,
        location: String,
        eligibility: String,
        requiredSkills: String,
        salaryOrStipend: String,
        deadline: String,
        instructions: String,
        hiringStatus: String
    ) {
        if (title.isBlank() || companyName.isBlank()) return
        viewModelScope.launch {
            repository.publishOpportunity(
                JobOpportunityEntity(
                    id = "opp_${UUID.randomUUID().toString().take(8)}",
                    title = title.trim(),
                    companyId = "comp_custom_${companyName.lowercase().take(6)}",
                    companyName = companyName.trim(),
                    type = type,
                    department = department.ifBlank { "Engineering" },
                    location = location.ifBlank { "Hybrid" },
                    eligibility = eligibility.ifBlank { "Open to qualified applicants" },
                    requiredSkills = requiredSkills.ifBlank { "Problem Solving, Communication" },
                    salaryOrStipend = salaryOrStipend.ifBlank { "Competitive" },
                    deadline = deadline.ifBlank { "2026-12-31" },
                    instructions = instructions.ifBlank { "Apply via Future Forge Opportunity Gateway." },
                    sourceType = "OFFICIAL_EMPLOYER",
                    sourceName = "$companyName Official Recruiter",
                    lastUpdated = "2026-11-18",
                    verified = true,
                    hiringStatus = hiringStatus,
                    postedByUserId = currentUserId
                )
            )
        }
    }

    fun upsertCompany(company: CompanyProfileEntity) {
        viewModelScope.launch { repository.upsertCompanyProfile(company) }
    }

    fun requestMentorship(
        student: UserEntity,
        mentor: UserEntity,
        topic: String,
        preferredSlot: String,
        isMockInterview: Boolean
    ) {
        viewModelScope.launch {
            repository.requestMentorship(
                MentorshipEntity(
                    id = "ment_${UUID.randomUUID().toString().take(8)}",
                    studentId = student.id,
                    studentName = student.name,
                    mentorId = mentor.id,
                    mentorName = mentor.name,
                    topic = topic.ifBlank { "Career Guidance" },
                    preferredSlot = preferredSlot.ifBlank { mentor.availability.ifBlank { "Flexible" } },
                    isMockInterview = isMockInterview
                )
            )
        }
    }

    fun updateMentorship(mentorship: MentorshipEntity, status: String, feedback: String) {
        viewModelScope.launch { repository.updateMentorshipStatus(mentorship, status, feedback) }
    }

    fun askProfessional(user: UserEntity, category: String, question: String) {
        if (question.isBlank()) return
        viewModelScope.launch {
            repository.postQuestion(
                ProfessionalQnaEntity(
                    id = "qna_${UUID.randomUUID().toString().take(8)}",
                    authorId = user.id,
                    authorName = user.name,
                    category = category,
                    question = question.trim()
                )
            )
        }
    }

    fun answerProfessionalQuestion(qna: ProfessionalQnaEntity, answer: String, mentor: UserEntity) {
        if (answer.isBlank()) return
        viewModelScope.launch { repository.answerQuestion(qna, answer.trim(), mentor) }
    }

    fun publishVideo(
        creator: UserEntity,
        title: String,
        description: String,
        category: String,
        industry: String,
        videoUrl: String,
        dayInCareerSummary: String
    ) {
        if (title.isBlank() || videoUrl.isBlank()) return
        viewModelScope.launch {
            repository.publishVideo(
                VideoItemEntity(
                    id = "vid_${UUID.randomUUID().toString().take(8)}",
                    title = title.trim(),
                    description = description.trim(),
                    creatorId = creator.id,
                    creatorName = creator.name,
                    creatorRole = creator.headline.ifBlank { "Industry Professional" },
                    category = category,
                    industry = industry.ifBlank { "Technology" },
                    videoUrl = videoUrl.trim(),
                    durationLabel = "05:00",
                    dayInCareerSummary = dayInCareerSummary.trim()
                )
            )
        }
    }

    fun saveWatchProgress(videoId: String, positionMs: Long, durationMs: Long) {
        viewModelScope.launch {
            repository.updateVideoWatchProgress(currentUserId, videoId, positionMs, durationMs)
        }
    }

    fun hostLiveSession(
        host: UserEntity,
        title: String,
        topic: String,
        schedule: String,
        description: String
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.hostLiveSession(
                LiveSessionEntity(
                    id = "live_${UUID.randomUUID().toString().take(8)}",
                    title = title.trim(),
                    hostId = host.id,
                    hostName = host.name,
                    hostTitle = host.headline.ifBlank { "Mentor" },
                    topic = topic.ifBlank { "Career Readiness" },
                    scheduleLabel = schedule.ifBlank { "Upcoming • 7:00 PM" },
                    description = description.trim(),
                    recordingUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    liveNotes = "Session created by ${host.name}. Live Q&A open."
                )
            )
        }
    }

    fun toggleSessionRegistration(session: LiveSessionEntity) {
        viewModelScope.launch { repository.toggleSessionRegistration(currentUserId, session) }
    }

    fun submitReview(
        company: CompanyProfileEntity,
        authorName: String,
        roleTitle: String,
        verificationType: String,
        culture: Int,
        wlb: Int,
        mgmt: Int,
        learning: Int,
        hybridPolicy: String,
        interviewExp: String,
        reviewText: String
    ) {
        if (reviewText.isBlank()) return
        viewModelScope.launch {
            repository.addEmployeeReview(
                EmployeeReviewEntity(
                    id = "rev_${UUID.randomUUID().toString().take(8)}",
                    companyId = company.id,
                    companyName = company.name,
                    authorName = if (verificationType == "ANONYMOUS") "Anonymous" else authorName,
                    roleTitle = roleTitle.ifBlank { "Employee" },
                    verificationType = verificationType,
                    workCultureRating = culture,
                    workLifeBalanceRating = wlb,
                    managementRating = mgmt,
                    learningRating = learning,
                    hybridRemotePolicy = hybridPolicy,
                    interviewExperience = interviewExp,
                    reviewText = reviewText.trim()
                )
            )
        }
    }

    fun respondCustomerVoice(item: CustomerVoiceEntity, response: String) {
        if (response.isBlank()) return
        viewModelScope.launch { repository.respondToCustomerVoice(item, response.trim()) }
    }

    fun createCommunityPost(user: UserEntity, category: String, title: String, content: String) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            repository.addCommunityPost(
                CommunityPostEntity(
                    id = "post_${UUID.randomUUID().toString().take(8)}",
                    authorId = user.id,
                    authorName = user.name,
                    category = category,
                    title = title.trim(),
                    content = content.trim()
                )
            )
        }
    }

    fun upvotePost(post: CommunityPostEntity) {
        viewModelScope.launch { repository.upvoteCommunityPost(post) }
    }

    fun reportContent(targetType: String, targetId: String, targetTitle: String, reason: String) {
        if (reason.isBlank()) return
        viewModelScope.launch {
            repository.submitContentReport(currentUserId, targetType, targetId, targetTitle, reason.trim())
        }
    }

    // Admin Governance Actions
    fun adminVerifyUser(user: UserEntity, status: String) {
        viewModelScope.launch { repository.adminVerifyUser(user, status) }
    }

    fun adminToggleSuspendUser(user: UserEntity) {
        viewModelScope.launch { repository.adminToggleSuspendUser(user) }
    }

    fun adminVerifyCompany(company: CompanyProfileEntity, verified: Boolean) {
        viewModelScope.launch { repository.adminVerifyCompany(company, verified) }
    }

    fun adminModerateOpportunity(opp: JobOpportunityEntity, verified: Boolean, status: String) {
        viewModelScope.launch { repository.adminModerateOpportunity(opp, verified, status) }
    }

    fun adminToggleVideoApproval(video: VideoItemEntity) {
        viewModelScope.launch { repository.adminToggleVideoApproval(video) }
    }

    fun adminResolveReport(report: ContentReportEntity, removeContent: Boolean) {
        viewModelScope.launch { repository.adminResolveReport(report, removeContent) }
    }
}
