package com.example.data.ai

import com.example.data.local.UserEntity

data class CareerSuggestion(
    val title: String,
    val industry: String,
    val matchPercent: Int,
    val whyRecommended: String,
    val requiredQualifications: String,
    val skillsToDevelop: List<String>,
    val relevantCertifications: List<String>,
    val opportunitiesOutlook: String,
    val progressionPath: String
)

data class CareerCompassResult(
    val suggestions: List<CareerSuggestion>,
    val comparisonSummary: String,
    val isPlaceholderNotice: Boolean = true
)

data class GeneratedMilestone(
    val stage: String,
    val title: String,
    val description: String,
    val linkedModule: String,
    val targetWeek: String
)

data class RoadmapGenerationResult(
    val targetCareer: String,
    val summary: String,
    val milestones: List<GeneratedMilestone>,
    val isPlaceholderNotice: Boolean = true
)

data class DoubtSolutionResult(
    val subject: String,
    val topic: String,
    val stepByStepExplanation: String,
    val relatedExample: String,
    val commonMistakeExplanation: String,
    val isPlaceholderNotice: Boolean = true
)

data class CommunicationEvaluationResult(
    val score: Int,
    val clarityFeedback: String,
    val vocabularyTip: String,
    val grammarNote: String,
    val improvedExample: String,
    val isPlaceholderNotice: Boolean = true
)

data class InterviewTurnResult(
    val clarityScore: Int,
    val relevanceScore: Int,
    val constructiveFeedback: String,
    val improvementFocus: String,
    val nextQuestion: String,
    val guidanceDisclaimer: String,
    val isPlaceholderNotice: Boolean = true
)

data class SkillMatchItem(
    val skillName: String,
    val status: String, // MATCHED, PARTIAL, MISSING
    val evidenceSource: String
)

data class JobReadinessResult(
    val targetRole: String,
    val readinessScore: Int,
    val confidenceLabel: String,
    val extractedRequirements: List<String>,
    val skillBreakdown: List<SkillMatchItem>,
    val recommendedActions: List<String>,
    val explanation: String,
    val isPlaceholderNotice: Boolean = true
)

interface AiRepository {
    suspend fun analyzeCareerCompass(profile: UserEntity): CareerCompassResult
    suspend fun generateDestinyRoadmap(profile: UserEntity, chosenCareer: String): RoadmapGenerationResult
    suspend fun solveDoubt(question: String, hasImage: Boolean): DoubtSolutionResult
    suspend fun answerDoubtFollowUp(originalQuestion: String, followUp: String): String
    suspend fun evaluateCommunication(mode: String, prompt: String, response: String): CommunicationEvaluationResult
    suspend fun evaluateInterviewAnswer(
        mode: String,
        roleTitle: String,
        companyName: String,
        questionAsked: String,
        userAnswer: String,
        turnNumber: Int
    ): InterviewTurnResult

    suspend fun analyzeJobReadiness(
        profile: UserEntity,
        completedCoursesCount: Int,
        completedProjectsCount: Int,
        avgExamScore: Int,
        jobTitle: String,
        jobDescription: String
    ): JobReadinessResult
}
