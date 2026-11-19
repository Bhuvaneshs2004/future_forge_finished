package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performScrollTo
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.repository.FutureForgeRepository
import com.example.ui.screens.AuthScreen
import com.example.ui.theme.FutureForgeTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w411dp-h915dp")
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private lateinit var context: Context
    private lateinit var db: FutureForgeDatabase
    private lateinit var repo: FutureForgeRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, FutureForgeDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repo = FutureForgeRepository(db, SessionPreferences(context))
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun appName_and_branding_match_FutureForge() {
        assertEquals("Future Forge", context.getString(R.string.app_name))
        assertEquals("FORGE YOUR OWN DESTINY.", context.getString(R.string.app_tagline))
    }

    @Test
    fun seedDatabase_populatesAllFourRoles_andEcosystemData() = runTest {
        FutureForgeDatabase.seedInitialData(db)
        val users = repo.observeAllUsers().first()
        assertTrue(users.any { it.role == UserRole.STUDENT.name })
        assertTrue(users.any { it.role == UserRole.PROFESSIONAL.name })
        assertTrue(users.any { it.role == UserRole.RECRUITER.name })
        assertTrue(users.any { it.role == UserRole.ADMIN.name })

        assertTrue(repo.observeOpportunities().first().isNotEmpty())
        assertTrue(repo.observeCompanies().first().isNotEmpty())
        assertTrue(repo.observeVideos().first().isNotEmpty())
        assertTrue(repo.observeLiveSessions().first().isNotEmpty())
    }

    @Test
    fun registration_and_login_enforce_rbac_and_admin_invite_code() = runTest {
        val badAdmin = repo.register("Fake Admin", "bad@ff.app", "Secret123", UserRole.ADMIN, "Gov", "WRONG")
        assertTrue(badAdmin.isFailure)

        val goodStudent = repo.register("Maya Rao", "maya@ff.app", "Secret123", UserRole.STUDENT, "B.Sc Data", "")
        assertTrue(goodStudent.isSuccess)
        assertEquals("VERIFIED", goodStudent.getOrNull()?.verificationStatus)

        val loginOk = repo.login("maya@ff.app", "Secret123")
        assertTrue(loginOk.isSuccess)

        val loginFail = repo.login("maya@ff.app", "WrongPass")
        assertTrue(loginFail.isFailure)
    }

    @Test
    fun achievements_only_unlock_when_criteria_met() = runTest {
        FutureForgeDatabase.seedInitialData(db)
        val studentId = "usr_student_1"
        assertTrue(repo.observeAchievements(studentId).first().isEmpty())

        repo.recordExamAttempt(
            ExamAttemptEntity(
                id = "att_low",
                userId = studentId,
                examId = "exam_swe_aptitude",
                examTitle = "Mock",
                category = "Corporate",
                scorePercent = 40,
                correctCount = 2,
                incorrectCount = 3,
                totalQuestions = 5,
                durationSeconds = 90,
                reviewSummary = "Sample"
            )
        )
        assertTrue(repo.observeAchievements(studentId).first().isEmpty())

        repo.recordExamAttempt(
            ExamAttemptEntity(
                id = "att_high",
                userId = studentId,
                examId = "exam_swe_aptitude",
                examTitle = "Mock",
                category = "Corporate",
                scorePercent = 85,
                correctCount = 4,
                incorrectCount = 1,
                totalQuestions = 5,
                durationSeconds = 90,
                reviewSummary = "Sample"
            )
        )
        val earned = repo.observeAchievements(studentId).first()
        assertEquals(1, earned.size)
        assertEquals("EXAM_HONORS", earned.first().badgeKey)
    }

    @Test
    fun student_recruiter_application_and_feedback_workflow_persists() = runTest {
        FutureForgeDatabase.seedInitialData(db)
        val studentId = "usr_student_1"
        val opp = repo.observeOpportunities().first().first()
        val app = ApplicationEntity(
            id = "app_test_1",
            userId = studentId,
            applicantName = "Aarav Mehta",
            applicantSkills = "Kotlin, Jetpack Compose, SQL",
            opportunityId = opp.id,
            opportunityTitle = opp.title,
            companyName = opp.companyName,
            resumeVersionTitle = "Android & Kotlin Engineer CV",
            coverNote = "Built Room and Compose apps."
        )
        repo.submitApplication(app)

        val savedApps = repo.observeUserApplications(studentId).first()
        assertTrue(savedApps.any { it.id == "app_test_1" && it.status == "SUBMITTED" })
        assertTrue(repo.observeAchievements(studentId).first().any { it.badgeKey == "FIRST_APPLICATION" })

        repo.updateApplicationRecruiterAction(
            application = savedApps.first { it.id == "app_test_1" },
            newStatus = "SHORTLISTED",
            assessmentScore = 92,
            interviewDate = "May 25, 11:00 AM",
            feedback = "Strong Android architecture fundamentals."
        )

        val updatedApp = repo.observeUserApplications(studentId).first().first { it.id == "app_test_1" }
        assertEquals("SHORTLISTED", updatedApp.status)
        assertEquals(92, updatedApp.assessmentScore)
        assertEquals("May 25, 11:00 AM", updatedApp.interviewDate)
        assertTrue(repo.observeNotifications(studentId).first().any { it.title.contains("Application Update") })
    }

    @Test
    fun mentor_qa_mentorship_and_admin_governance_workflows() = runTest {
        FutureForgeDatabase.seedInitialData(db)
        val student = repo.observeUser("usr_student_1").first()!!
        val mentor = repo.observeUser("usr_mentor_1").first()!!

        val mentorship = MentorshipEntity(
            id = "mnt_test_1",
            studentId = student.id,
            studentName = student.name,
            mentorId = mentor.id,
            mentorName = mentor.name,
            topic = "Android System Design Mock",
            preferredSlot = "Sat 5 PM",
            isMockInterview = true
        )
        repo.requestMentorship(mentorship)
        repo.updateMentorshipStatus(mentorship, "APPROVED", "Confirmed! Bring your portfolio repo.")
        assertEquals("APPROVED", repo.observeMentorshipsForUser(student.id).first().first { it.id == "mnt_test_1" }.status)

        val qna = ProfessionalQnaEntity(
            id = "qna_test_1",
            authorId = student.id,
            authorName = student.name,
            category = "Mobile",
            question = "How should juniors prepare for offline-first Room architecture?"
        )
        repo.postQuestion(qna)
        repo.answerQuestion(qna, "Focus on reactive Flow DAOs and clear separation of state.", mentor)
        assertTrue(repo.observeQna().first().first { it.id == "qna_test_1" }.answerText.contains("reactive Flow"))

        val review = repo.observeReviews().first().first()
        repo.submitContentReport(student.id, "REVIEW", review.id, review.companyName, "Suspected unverified spam")
        val reported = repo.observeReports().first().first { it.targetId == review.id }
        repo.adminResolveReport(reported, removeTarget = true)
        assertEquals("ACTION_TAKEN", repo.observeReports().first().first { it.id == reported.id }.status)
        assertTrue(repo.observeReviews().first().none { it.id == review.id })

        val pendingMentor = repo.observeUser("usr_mentor_pending").first()!!
        repo.adminVerifyUser(pendingMentor, "VERIFIED")
        assertEquals("VERIFIED", repo.observeUser("usr_mentor_pending").first()!!.verificationStatus)
    }

    @Test
    fun authScreen_renders_FutureForge_branding_and_controls() {
        composeRule.setContent {
            FutureForgeTheme {
                AuthScreen(
                    isLoading = false,
                    errorMessage = null,
                    onLogin = { _, _ -> },
                    onRegister = { _, _, _, _, _, _ -> },
                    onClearError = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("FUTURE FORGE").assertIsDisplayed()
        composeRule.onNodeWithTag("auth_submit_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_login_student").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("quick_login_admin").performScrollTo().assertIsDisplayed()
    }
}
