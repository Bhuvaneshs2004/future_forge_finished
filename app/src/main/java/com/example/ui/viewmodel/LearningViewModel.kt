package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.*
import com.example.data.catalog.DomainCatalog
import com.example.data.local.*
import com.example.data.repository.FutureForgeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

sealed interface AiCallState<out T> {
    data object Idle : AiCallState<Nothing>
    data object Loading : AiCallState<Nothing>
    data class Success<T>(val data: T) : AiCallState<T>
    data class Error(val message: String) : AiCallState<Nothing>
}

data class ActiveInterviewState(
    val active: Boolean = false,
    val mode: String = "TECHNICAL",
    val roleTitle: String = "Software Engineer",
    val companyName: String = "Meridian Cloud Systems",
    val currentQuestion: String = "",
    val turnNumber: Int = 1,
    val transcriptLines: List<String> = emptyList(),
    val lastEvaluation: InterviewTurnResult? = null,
    val loading: Boolean = false
)

class LearningViewModel(
    private val repository: FutureForgeRepository,
    private val aiRepository: AiRepository,
    private val userId: String
) : ViewModel() {

    val activeRoadmap: StateFlow<RoadmapEntity?> = repository.observeActiveRoadmap(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val milestones: StateFlow<List<RoadmapMilestoneEntity>> = activeRoadmap
        .flatMapLatest { rm ->
            if (rm == null) flowOf(emptyList()) else repository.observeMilestones(rm.id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val examAttempts: StateFlow<List<ExamAttemptEntity>> = repository.observeExamAttempts(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val doubts: StateFlow<List<DoubtEntity>> = repository.observeDoubts(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val courseProgress: StateFlow<List<CourseProgressEntity>> = repository.observeCourseProgress(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val commPractice: StateFlow<List<CommunicationPracticeEntity>> = repository.observeCommPractice(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val interviewHistory: StateFlow<List<InterviewSessionEntity>> = repository.observeInterviews(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    val studyTasks: StateFlow<List<StudyPlanTaskEntity>> = repository.observeStudyPlan(userId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _compassState = MutableStateFlow<AiCallState<CareerCompassResult>>(AiCallState.Idle)
    val compassState: StateFlow<AiCallState<CareerCompassResult>> = _compassState.asStateFlow()

    private val _doubtLoading = MutableStateFlow(false)
    val doubtLoading: StateFlow<Boolean> = _doubtLoading.asStateFlow()

    private val _commEvalState = MutableStateFlow<AiCallState<CommunicationEvaluationResult>>(AiCallState.Idle)
    val commEvalState: StateFlow<AiCallState<CommunicationEvaluationResult>> = _commEvalState.asStateFlow()

    private val _interviewState = MutableStateFlow(ActiveInterviewState())
    val interviewState: StateFlow<ActiveInterviewState> = _interviewState.asStateFlow()

    private val _roadmapGenerating = MutableStateFlow(false)
    val roadmapGenerating: StateFlow<Boolean> = _roadmapGenerating.asStateFlow()

    fun analyzeCareers(profile: UserEntity) {
        viewModelScope.launch {
            _compassState.value = AiCallState.Loading
            try {
                val result = aiRepository.analyzeCareerCompass(profile)
                _compassState.value = AiCallState.Success(result)
            } catch (e: Exception) {
                _compassState.value = AiCallState.Error(e.localizedMessage ?: "Unable to analyze profile.")
            }
        }
    }

    fun forgeDestinyRoadmap(profile: UserEntity, chosenCareer: String) {
        viewModelScope.launch {
            _roadmapGenerating.value = true
            try {
                val generated = aiRepository.generateDestinyRoadmap(profile, chosenCareer)
                val entities = generated.milestones.mapIndexed { idx, item ->
                    RoadmapMilestoneEntity(
                        id = "ms_${userId}_${idx}_${UUID.randomUUID().toString().take(4)}",
                        roadmapId = "",
                        userId = userId,
                        stage = item.stage,
                        title = item.title,
                        description = item.description,
                        linkedModule = item.linkedModule,
                        targetWeek = item.targetWeek,
                        reminderEnabled = idx == 0,
                        completed = false,
                        orderIndex = idx
                    )
                }
                repository.saveGeneratedRoadmap(
                    userId = userId,
                    targetCareer = generated.targetCareer,
                    summary = generated.summary,
                    educationSnapshot = profile.educationLevel,
                    skillsSnapshot = profile.skills,
                    milestones = entities
                )
            } finally {
                _roadmapGenerating.value = false
            }
        }
    }

    fun toggleMilestone(ms: RoadmapMilestoneEntity) {
        viewModelScope.launch { repository.toggleMilestone(ms) }
    }

    fun toggleMilestoneReminder(ms: RoadmapMilestoneEntity) {
        viewModelScope.launch { repository.toggleMilestoneReminder(ms) }
    }

    fun submitExam(examId: String, selectedAnswers: Map<String, Int>, durationSeconds: Int) {
        val exam = DomainCatalog.exams.firstOrNull { it.id == examId } ?: return
        var correct = 0
        val reviewBuilder = StringBuilder()
        exam.questions.forEachIndexed { idx, q ->
            val picked = selectedAnswers[q.id]
            val isCorrect = picked == q.correctIndex
            if (isCorrect) correct++
            reviewBuilder.append("Q${idx + 1} (${q.topic}): ")
            reviewBuilder.append(if (isCorrect) "Correct" else "Incorrect (Answer: ${q.options[q.correctIndex]})")
            reviewBuilder.append(" — ${q.explanation}\n")
        }
        val total = exam.questions.size.coerceAtLeast(1)
        val score = (correct * 100) / total
        viewModelScope.launch {
            repository.recordExamAttempt(
                ExamAttemptEntity(
                    id = "att_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    examId = exam.id,
                    examTitle = exam.title,
                    category = exam.category,
                    scorePercent = score,
                    correctCount = correct,
                    incorrectCount = total - correct,
                    totalQuestions = total,
                    durationSeconds = durationSeconds,
                    reviewSummary = reviewBuilder.toString().trim()
                )
            )
        }
    }

    fun solveDoubt(questionText: String, imageUri: String?) {
        if (questionText.isBlank() && imageUri.isNullOrBlank()) return
        viewModelScope.launch {
            _doubtLoading.value = true
            try {
                val res = aiRepository.solveDoubt(
                    question = questionText.ifBlank { "Image-based problem submitted" },
                    hasImage = !imageUri.isNullOrBlank()
                )
                val fullExplanation = buildString {
                    appendLine("Steps:")
                    appendLine(res.stepByStepExplanation)
                    appendLine("\nRelated Example:")
                    appendLine(res.relatedExample)
                    appendLine("\nAvoid This Mistake:")
                    append(res.commonMistakeExplanation)
                }
                repository.saveDoubt(
                    DoubtEntity(
                        id = "dbt_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        questionText = questionText.ifBlank { "Uploaded Question Image" },
                        imageUri = imageUri,
                        subject = res.subject,
                        topic = res.topic,
                        explanation = fullExplanation
                    )
                )
            } finally {
                _doubtLoading.value = false
            }
        }
    }

    fun askDoubtFollowUp(doubt: DoubtEntity, followUp: String) {
        if (followUp.isBlank()) return
        viewModelScope.launch {
            val reply = aiRepository.answerDoubtFollowUp(doubt.questionText, followUp)
            val updatedHistory = buildString {
                if (doubt.followUpHistory.isNotBlank()) appendLine(doubt.followUpHistory)
                appendLine("You: $followUp")
                append("AI Tutor: $reply")
            }
            repository.saveDoubt(doubt.copy(followUpHistory = updatedHistory))
        }
    }

    fun toggleLessonComplete(courseId: String, lessonId: String) {
        val course = DomainCatalog.skillPaths.firstOrNull { it.id == courseId } ?: return
        viewModelScope.launch {
            val existing = courseProgress.value.firstOrNull { it.courseId == courseId }
            val currentSet = existing?.completedLessons
                ?.split(",")
                ?.filter { it.isNotBlank() }
                ?.toMutableSet() ?: mutableSetOf()
            if (currentSet.contains(lessonId)) currentSet.remove(lessonId) else currentSet.add(lessonId)
            val allLessonsDone = course.lessons.all { currentSet.contains(it.id) }
            val completed = allLessonsDone && (existing?.projectSubmitted == true) && ((existing.quizScore) >= 100)
            repository.saveCourseProgress(
                CourseProgressEntity(
                    id = existing?.id ?: "cp_${userId}_$courseId",
                    userId = userId,
                    courseId = courseId,
                    courseTitle = course.title,
                    completedLessons = currentSet.joinToString(","),
                    projectSubmitted = existing?.projectSubmitted ?: false,
                    projectNotes = existing?.projectNotes ?: "",
                    quizScore = existing?.quizScore ?: 0,
                    completed = completed
                )
            )
        }
    }

    fun submitCourseProjectAndQuiz(courseId: String, projectNotes: String, pickedQuizIdx: Int) {
        val course = DomainCatalog.skillPaths.firstOrNull { it.id == courseId } ?: return
        val quizScore = if (pickedQuizIdx == course.quizCorrectIndex) 100 else 50
        viewModelScope.launch {
            val existing = courseProgress.value.firstOrNull { it.courseId == courseId }
            val lessonSet = existing?.completedLessons?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
            val allLessonsDone = course.lessons.all { lessonSet.contains(it.id) }
            val completed = allLessonsDone && projectNotes.isNotBlank() && quizScore == 100
            repository.saveCourseProgress(
                CourseProgressEntity(
                    id = existing?.id ?: "cp_${userId}_$courseId",
                    userId = userId,
                    courseId = courseId,
                    courseTitle = course.title,
                    completedLessons = existing?.completedLessons ?: "",
                    projectSubmitted = projectNotes.isNotBlank(),
                    projectNotes = projectNotes.trim(),
                    quizScore = quizScore,
                    completed = completed
                )
            )
        }
    }

    fun evaluateCommunication(mode: String, prompt: String, response: String) {
        if (response.isBlank()) return
        viewModelScope.launch {
            _commEvalState.value = AiCallState.Loading
            try {
                val result = aiRepository.evaluateCommunication(mode, prompt, response)
                _commEvalState.value = AiCallState.Success(result)
                repository.recordCommPractice(
                    CommunicationPracticeEntity(
                        id = "comm_${UUID.randomUUID().toString().take(8)}",
                        userId = userId,
                        mode = mode,
                        prompt = prompt,
                        userResponse = response,
                        aiFeedback = "${result.clarityFeedback} Tip: ${result.vocabularyTip}",
                        score = result.score
                    )
                )
            } catch (e: Exception) {
                _commEvalState.value = AiCallState.Error(e.localizedMessage ?: "Evaluation failed")
            }
        }
    }

    fun startInterview(mode: String, roleTitle: String, companyName: String) {
        val opening = when (mode.uppercase()) {
            "TECHNICAL" -> "Welcome to the Technical Interview for $roleTitle at $companyName. Walk me through a challenging engineering or architecture problem you solved and why you chose that data structure or design."
            "APTITUDE" -> "Welcome to the Analytical Screening for $roleTitle at $companyName. How would you estimate server and storage capacity for 100,000 daily active learners?"
            else -> "Welcome to the $mode Interview for $roleTitle at $companyName. Tell me about yourself and why your recent work prepares you for this role."
        }
        _interviewState.value = ActiveInterviewState(
            active = true,
            mode = mode,
            roleTitle = roleTitle.ifBlank { "Graduate Engineer" },
            companyName = companyName.ifBlank { "Private Tech Firm" },
            currentQuestion = opening,
            turnNumber = 1,
            transcriptLines = listOf("Interviewer: $opening")
        )
    }

    fun submitInterviewAnswer(answer: String) {
        val current = _interviewState.value
        if (!current.active || answer.isBlank()) return
        viewModelScope.launch {
            _interviewState.value = current.copy(loading = true)
            val eval = aiRepository.evaluateInterviewAnswer(
                mode = current.mode,
                roleTitle = current.roleTitle,
                companyName = current.companyName,
                questionAsked = current.currentQuestion,
                userAnswer = answer,
                turnNumber = current.turnNumber
            )
            val updatedTranscript = current.transcriptLines + listOf(
                "Candidate: $answer",
                "Feedback (${eval.clarityScore}% clarity, ${eval.relevanceScore}% relevance): ${eval.constructiveFeedback}",
                "Interviewer: ${eval.nextQuestion}"
            )
            _interviewState.value = current.copy(
                currentQuestion = eval.nextQuestion,
                turnNumber = current.turnNumber + 1,
                transcriptLines = updatedTranscript,
                lastEvaluation = eval,
                loading = false
            )
        }
    }

    fun finishAndSaveInterview() {
        val current = _interviewState.value
        if (!current.active) return
        val last = current.lastEvaluation
        viewModelScope.launch {
            repository.saveInterviewSession(
                InterviewSessionEntity(
                    id = "int_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    mode = current.mode,
                    roleTitle = current.roleTitle,
                    companyName = current.companyName,
                    transcript = current.transcriptLines.joinToString("\n\n"),
                    clarityScore = last?.clarityScore ?: 72,
                    relevanceScore = last?.relevanceScore ?: 72,
                    feedbackSummary = last?.constructiveFeedback ?: "Session completed.",
                    recommendedTopics = last?.improvementFocus ?: "Continue practicing STAR clarity."
                )
            )
            _interviewState.value = ActiveInterviewState()
        }
    }

    fun addStudyTask(title: String, subject: String, dueDate: String, durationMinutes: Int, priority: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.upsertStudyTask(
                StudyPlanTaskEntity(
                    id = "st_${UUID.randomUUID().toString().take(8)}",
                    userId = userId,
                    title = title.trim(),
                    subject = subject.ifBlank { "General Prep" },
                    dueDate = dueDate.ifBlank { "2026-11-30" },
                    durationMinutes = durationMinutes.coerceIn(15, 240),
                    priority = priority
                )
            )
        }
    }

    fun toggleStudyTask(task: StudyPlanTaskEntity) {
        viewModelScope.launch {
            repository.upsertStudyTask(task.copy(completed = !task.completed))
        }
    }

    fun deleteStudyTask(taskId: String) {
        viewModelScope.launch { repository.deleteStudyTask(taskId) }
    }
}
