package com.example

import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.UserEntity
import com.example.data.local.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    private val aiRepo = PlaceholderAiRepository()
    private val sampleUser = UserEntity(
        id = "u1",
        name = "Test Learner",
        email = "test@futureforge.app",
        passwordHash = "hash",
        role = UserRole.STUDENT.name,
        educationLevel = "B.Tech CS",
        interests = "Software, Mobile, AI",
        strengths = "Analytical Problem Solving",
        skills = "Kotlin, SQL, Data Structures",
        careerAmbitions = "Mobile Systems Engineer"
    )

    @Test
    fun careerCompass_returnsMultipleCareers_withPlaceholderNotice() = runTest {
        val result = aiRepo.analyzeCareerCompass(sampleUser)
        assertTrue(result.isPlaceholderNotice)
        assertTrue(result.suggestions.size >= 3)
        assertTrue(result.suggestions.all { it.whyRecommended.isNotBlank() && it.skillsToDevelop.isNotEmpty() })
    }

    @Test
    fun destinyRoadmap_generatesConnectedMilestones() = runTest {
        val roadmap = aiRepo.generateDestinyRoadmap(sampleUser, "Software Engineer")
        assertTrue(roadmap.milestones.size >= 6)
        assertTrue(roadmap.milestones.any { it.linkedModule == "ExamHub" })
        assertTrue(roadmap.milestones.any { it.linkedModule == "InterviewArena" })
    }

    @Test
    fun doubtSolver_and_jobReadiness_produceExplainableGuidance() = runTest {
        val doubt = aiRepo.solveDoubt("What is the time complexity of a hash table lookup?", hasImage = true)
        assertTrue(doubt.subject.contains("Computer Science"))
        assertTrue(doubt.stepByStepExplanation.contains("Image Attachment"))

        val readiness = aiRepo.analyzeJobReadiness(
            profile = sampleUser,
            completedCoursesCount = 2,
            completedProjectsCount = 1,
            avgExamScore = 82,
            jobTitle = "Kotlin Engineer",
            jobDescription = "Looking for Kotlin, SQL, REST APIs, and Communication skills."
        )
        assertTrue(readiness.readinessScore in 25..95)
        assertTrue(readiness.skillBreakdown.isNotEmpty())
        assertTrue(readiness.explanation.contains("Never a guarantee"))
    }
}
