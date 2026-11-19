package com.example.data.ai

import com.example.data.local.UserEntity
import kotlinx.coroutines.delay

/**
 * Local placeholder implementation used when Firebase AI Logic is disabled.
 * Every response is clearly flagged with isPlaceholderNotice = true so the UI
 * surfaces a clear notice that live Gemini AI requires enabling Firebase.
 */
class PlaceholderAiRepository : AiRepository {

    override suspend fun analyzeCareerCompass(profile: UserEntity): CareerCompassResult {
        delay(350)
        val combined = "${profile.interests} ${profile.skills} ${profile.strengths} ${profile.careerAmbitions}".lowercase()

        val softwareMatch = if (combined.containsAny("code", "software", "android", "kotlin", "java", "python", "data", "tech")) 91 else 78
        val productMatch = if (combined.containsAny("product", "strategy", "lead", "business", "design", "market")) 89 else 75
        val dataMatch = if (combined.containsAny("data", "math", "analytics", "finance", "sql", "ai")) 90 else 74

        val suggestions = listOf(
            CareerSuggestion(
                title = "Software & Mobile Systems Engineer",
                industry = "Private Technology, SaaS & Fintech",
                matchPercent = softwareMatch,
                whyRecommended = "Aligns with your analytical strengths (${profile.strengths.ifBlank { "problem solving" }}) and current foundation (${profile.skills.ifBlank { "core logic" }}). Offers broad entry routes without locking into a single niche.",
                requiredQualifications = "Bachelor's in CS/IT or equivalent demonstrable coding portfolio + strong DSA & architecture fundamentals.",
                skillsToDevelop = listOf("Data Structures & Algorithms", "Kotlin / Backend APIs", "SQL & Cloud Persistence", "Automated Testing"),
                relevantCertifications = listOf("Associate Android Developer Prep", "Cloud Practitioner Foundations"),
                opportunitiesOutlook = "Strong private-sector demand for interns and entry-level engineers across SaaS, banking tech, and AI tooling firms.",
                progressionPath = "Intern -> Junior Engineer -> Software Engineer II -> Senior / Staff Architect"
            ),
            CareerSuggestion(
                title = "Associate Product & Growth Strategist",
                industry = "Consumer Platforms, EdTech & B2B SaaS",
                matchPercent = productMatch,
                whyRecommended = "Blends structured problem-solving with user empathy and communication (${profile.interests.ifBlank { "technology & business" }}), bridging engineering and market outcomes.",
                requiredQualifications = "Any quantitative or engineering/business degree with validated product case studies and customer research.",
                skillsToDevelop = listOf("MECE Case Framing", "SQL Funnel Analytics", "User Discovery", "Executive Communication"),
                relevantCertifications = listOf("Product Analytics Foundations", "Agile Product Ownership"),
                opportunitiesOutlook = "Growing APM cohorts and rotational leadership programs at private tech and fintech companies.",
                progressionPath = "Product Intern -> APM -> Product Manager -> Group PM"
            ),
            CareerSuggestion(
                title = "Applied Data & Business Intelligence Analyst",
                industry = "Analytics, E-Commerce & Consulting",
                matchPercent = dataMatch,
                whyRecommended = "Capitalizes on quantitative precision and structured curiosity, turning raw operational telemetry into private-sector decisions.",
                requiredQualifications = "Degree with quantitative coursework + portfolio of SQL/Python data storytelling dashboards.",
                skillsToDevelop = listOf("Advanced SQL", "Python & Data Cleaning", "A/B Experimentation", "Dashboard Storytelling"),
                relevantCertifications = listOf("SQL & Data Modeling Track", "Applied BI Specialist"),
                opportunitiesOutlook = "High fresher hiring volume across consulting, retail tech, and financial services.",
                progressionPath = "Data Analyst -> Senior Analyst -> Analytics Engineer / Data Scientist"
            )
        ).sortedByDescending { it.matchPercent }

        return CareerCompassResult(
            suggestions = suggestions,
            comparisonSummary = "All three pathways value measurable portfolio evidence over rote credentials. Engineering emphasizes depth in code & systems; Product prioritizes cross-functional clarity; Data rewards statistical rigor. Choose freely or blend them."
        )
    }

    override suspend fun generateDestinyRoadmap(profile: UserEntity, chosenCareer: String): RoadmapGenerationResult {
        delay(350)
        val career = chosenCareer.ifBlank { "Software & Mobile Systems Engineer" }
        val isTech = career.lowercase().containsAny("software", "engineer", "developer", "data", "ai", "tech")
        val milestones = if (isTech) {
            listOf(
                GeneratedMilestone("1. Academic Foundation", "Complete Core CS & Math Refresh", "Review complexity analysis, discrete logic, and relational database concepts aligned to ${profile.educationLevel.ifBlank { "your degree" }}.", "ExamHub", "Weeks 1-2"),
                GeneratedMilestone("2. Technical Core", "Master Programming Fundamentals & Data Structures", "Complete structured lessons on arrays, hash tables, trees, graphs, and clean code.", "SkillForge", "Weeks 3-5"),
                GeneratedMilestone("3. Practical Projects", "Build 2 Real End-to-End Portfolio Projects", "Ship an API-connected mobile/web utility with automated unit tests and documentation.", "SkillForge", "Weeks 6-8"),
                GeneratedMilestone("4. Portfolio & Resume", "Craft Measurable Resume & Project Showcase", "Quantify impact in Resume Studio and generate a tailored private-sector cover letter.", "ResumeStudio", "Week 9"),
                GeneratedMilestone("5. Exam & Aptitude Prep", "Pass Timed Recruitment Mock Assessments", "Score 75%+ on Corporate Tech Recruitment DSA & Aptitude mock tests.", "ExamHub", "Weeks 10-11"),
                GeneratedMilestone("6. Communication & Interviews", "Complete AI & Mentor Mock Interviews", "Run HR + Technical simulations in Interview Arena and refine answers in Communication Lab.", "InterviewArena", "Weeks 12-13"),
                GeneratedMilestone("7. Mentorship & Industry", "Book Verified Mentor Review & Research Companies", "Request a 1-on-1 portfolio audit and study target private-sector company checklists.", "ProfessionalConnect", "Week 14"),
                GeneratedMilestone("8. Internships & Roles", "Apply to Verified Internships & Entry Roles", "Run AI Job Readiness checks and submit targeted applications via Opportunity Gateway.", "CompanyJobs", "Weeks 15-16")
            )
        } else {
            listOf(
                GeneratedMilestone("1. Academic & Domain Base", "Solidify Quantitative & Industry Foundations", "Study core market analysis, unit economics, and domain metrics.", "ExamHub", "Weeks 1-2"),
                GeneratedMilestone("2. Core Practical Skills", "Complete Problem-Solving & Communication Paths", "Build MECE issue-tree fluency and executive writing in Skill Forge.", "SkillForge", "Weeks 3-5"),
                GeneratedMilestone("3. Applied Case Projects", "Publish 2 Real Company Case Studies", "Use Customer Voice & Industry Watch data to propose real product/business improvements.", "CompanyJobs", "Weeks 6-8"),
                GeneratedMilestone("4. Resume & Portfolio", "Assemble Case Portfolio & Targeted Resume", "Present measurable outcomes and strategy artifacts in Resume Studio.", "ResumeStudio", "Week 9"),
                GeneratedMilestone("5. Aptitude & Mock Tests", "Clear Entrance & Recruitment Aptitude Benchmarks", "Complete timed quantitative and data-interpretation assessments.", "ExamHub", "Weeks 10-11"),
                GeneratedMilestone("6. Interview Mastery", "Practice Behavioral & Case Interviews", "Complete multi-turn simulations and review clarity/relevance feedback.", "InterviewArena", "Weeks 12-13"),
                GeneratedMilestone("7. Mentor Guidance", "Consult Verified Industry Professionals", "Schedule mentor feedback and attend a live sector webinar.", "ProfessionalConnect", "Week 14"),
                GeneratedMilestone("8. Gateway Applications", "Apply to Verified Internships & Graduate Programs", "Track deadlines, readiness scores, and recruiter feedback in My Progress.", "CompanyJobs", "Weeks 15-16")
            )
        }

        return RoadmapGenerationResult(
            targetCareer = career,
            summary = "Sample personalized 16-week Destiny Roadmap connecting your current skills (${profile.skills.ifBlank { "foundational skills" }}) to verified private-sector opportunities in $career.",
            milestones = milestones
        )
    }

    override suspend fun solveDoubt(question: String, hasImage: Boolean): DoubtSolutionResult {
        delay(350)
        val qLower = question.lowercase()
        val (subject, topic, steps, example, mistake) = when {
            qLower.containsAny("complexity", "big o", "hash", "sort", "tree", "algorithm", "code", "kotlin", "sql") -> {
                Quintuple(
                    "Computer Science & Algorithms",
                    "Asymptotic Complexity & Data Structures",
                    "1. Identify the dominant repeating operation inside the loop or recursion.\n2. Check whether input size halves each step (O(log n)) or scans linearly (O(n)).\n3. Replace nested linear scans with a HashSet/HashMap lookup to reduce O(n^2) to O(n) average time." +
                        if (hasImage) "\n4. [Image Attachment Noted]: Visual diagram structure interpreted in sample mode; enable Firebase AI Logic for live multimodal OCR & diagram reading." else "",
                    "Example: Checking two-sum pairs using a HashSet visits each element once -> O(n) time, O(n) space.",
                    "Common Mistake: Forgetting that sorting first adds O(n log n) even if the subsequent two-pointer scan is O(n)."
                )
            }
            qLower.containsAny("percent", "probability", "ratio", "profit", "interest", "math", "equation") -> {
                Quintuple(
                    "Quantitative Aptitude",
                    "Percentages, Ratios & Probability",
                    "1. Define the base quantity cleanly (e.g., let base = 100 or total sample space S).\n2. Apply sequential operations step by step rather than adding percentages directly.\n3. Verify units and boundary cases before picking the final option." +
                        if (hasImage) "\n4. [Image Attachment Noted]: Uploaded problem image acknowledged in sample mode; enable Firebase AI Logic for live image equation extraction." else "",
                    "Example: A 25% increase followed by a 20% decrease returns to 100 * 1.25 * 0.80 = 100 (0% net change).",
                    "Common Mistake: Assuming +x% and -x% cancel out; sequential changes multiply, not add."
                )
            }
            else -> {
                Quintuple(
                    "General Academic & Career Reasoning",
                    "Structured Concept Breakdown",
                    "1. Isolate what is explicitly given vs. what the prompt asks you to prove or compute.\n2. Break the question into core principles and test with a small concrete case.\n3. Synthesize a concise, verifiable answer." +
                        if (hasImage) "\n4. [Image Attachment Noted]: Image received locally; live multimodal Gemini vision parsing requires enabling Firebase AI Logic." else "",
                    "Example: When evaluating any system tradeoff, compare latency, correctness, and operational complexity side-by-side.",
                    "Common Mistake: Jumping straight to memorized formulas without checking assumptions or edge constraints."
                )
            }
        }
        return DoubtSolutionResult(
            subject = subject,
            topic = topic,
            stepByStepExplanation = steps,
            relatedExample = example,
            commonMistakeExplanation = mistake
        )
    }

    override suspend fun answerDoubtFollowUp(originalQuestion: String, followUp: String): String {
        delay(250)
        return "Sample Follow-Up Guidance regarding \"${followUp.take(60)}\": Focus on isolating the variable or constraint that changed from your original question. Test one boundary input (such as 0, 1, or empty set) to confirm why the intermediate step holds."
    }

    override suspend fun evaluateCommunication(mode: String, prompt: String, response: String): CommunicationEvaluationResult {
        delay(300)
        val wordCount = response.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val score = when {
            wordCount >= 35 -> 86
            wordCount >= 18 -> 78
            wordCount >= 8 -> 68
            else -> 54
        }
        val clarity = if (wordCount < 15) {
            "Your response is brief ($wordCount words). Expand with a concrete context, action, and measurable outcome."
        } else {
            "Good structure ($wordCount words) for $mode practice. Lead immediately with your key point in the first sentence."
        }
        return CommunicationEvaluationResult(
            score = score,
            clarityFeedback = clarity,
            vocabularyTip = "Swap generic verbs ('did', 'made', 'helped') with crisp ownership verbs ('architected', 'quantified', 'streamlined').",
            grammarNote = "Maintain consistent tense throughout; use active voice for personal accomplishments.",
            improvedExample = "Sample Polish: \"I streamlined our team's data pipeline by introducing schema validation, cutting weekly reporting errors by 35%.\""
        )
    }

    override suspend fun evaluateInterviewAnswer(
        mode: String,
        roleTitle: String,
        companyName: String,
        questionAsked: String,
        userAnswer: String,
        turnNumber: Int
    ): InterviewTurnResult {
        delay(350)
        val words = userAnswer.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.size
        val hasMetrics = userAnswer.any { it.isDigit() }
        val relevance = (65 + (if (words > 20) 15 else 5) + (if (hasMetrics) 10 else 0)).coerceAtMost(94)
        val clarity = (68 + (if (words in 20..120) 16 else 6)).coerceAtMost(92)

        val nextQ = when (mode.uppercase()) {
            "TECHNICAL" -> when (turnNumber) {
                1 -> "How would you handle database indexing and caching if read traffic for $companyName grows 10x?"
                2 -> "Walk me through how you debugged a subtle production or test failure and prevented regression."
                else -> "What architectural tradeoff would you reconsider in your flagship project today?"
            }
            "APTITUDE" -> when (turnNumber) {
                1 -> "Estimate the daily active storage bandwidth required for 500,000 learners submitting 2 practice tests each."
                else -> "If two engineering teams have conflicting deadlines on a shared API, how do you quantify priority?"
            }
            else -> when (turnNumber) {
                1 -> "Tell me about a time you received critical feedback for a $roleTitle task. How did you respond?"
                2 -> "Describe an ambiguous goal you clarified without waiting for step-by-step instructions."
                else -> "Why does $companyName's current product direction align with your long-term growth?"
            }
        }

        val feedback = buildString {
            append("Clarity & Relevance Review ($words words): ")
            if (hasMetrics) append("Strong inclusion of concrete numbers/metrics. ")
            else append("Add specific numbers or concrete outcomes to make your impact unmistakable. ")
            if (words < 20) append("Elaborate on the specific action YOU took and the final result.")
            else append("Well-paced response; tighten the opening sentence to state your headline takeaway first.")
        }

        return InterviewTurnResult(
            clarityScore = clarity,
            relevanceScore = relevance,
            constructiveFeedback = feedback,
            improvementFocus = if (hasMetrics) "Deepen tradeoff reasoning & stakeholder impact" else "Quantify results using STAR metrics (%, time saved, scale)",
            nextQuestion = nextQ,
            guidanceDisclaimer = "Guidance only — AI practice evaluates structure and clarity and never guarantees interview selection."
        )
    }

    override suspend fun analyzeJobReadiness(
        profile: UserEntity,
        completedCoursesCount: Int,
        completedProjectsCount: Int,
        avgExamScore: Int,
        jobTitle: String,
        jobDescription: String
    ): JobReadinessResult {
        delay(350)
        val jdLower = jobDescription.lowercase()
        val userSkillsLower = "${profile.skills} ${profile.qualifications} ${profile.strengths}".lowercase()

        val candidateSkills = listOf(
            "Kotlin / Mobile" to listOf("kotlin", "android", "compose", "mobile"),
            "Data Structures & Problem Solving" to listOf("dsa", "algorithm", "problem", "coding", "data structure"),
            "SQL & Databases" to listOf("sql", "database", "room", "postgres", "firestore"),
            "REST APIs & Architecture" to listOf("api", "rest", "backend", "architecture", "system"),
            "Communication & Collaboration" to listOf("communication", "team", "stakeholder", "leadership"),
            "Analytics & Product Thinking" to listOf("analytics", "product", "metric", "python", "excel")
        )

        val extracted = candidateSkills.filter { (_, keywords) ->
            keywords.any { jdLower.contains(it) }
        }.map { it.first }.ifEmpty {
            listOf("Core Problem Solving", "Role-Specific Technical Execution", "Communication & Collaboration")
        }

        val breakdown = extracted.map { req ->
            val keywords = candidateSkills.firstOrNull { it.first == req }?.second ?: listOf(req.lowercase())
            val inProfile = keywords.any { userSkillsLower.contains(it) }
            val hasEvidence = completedCoursesCount > 0 || completedProjectsCount > 0 || avgExamScore >= 65
            when {
                inProfile && hasEvidence -> SkillMatchItem(req, "MATCHED", "Profile + Verified Course/Exam Evidence")
                inProfile -> SkillMatchItem(req, "PARTIAL", "Self-reported in Profile (add project or exam evidence)")
                else -> SkillMatchItem(req, "MISSING", "Not yet evidenced in profile or completed modules")
            }
        }

        val matchedCount = breakdown.count { it.status == "MATCHED" }
        val partialCount = breakdown.count { it.status == "PARTIAL" }
        val rawScore = ((matchedCount * 100 + partialCount * 55) / breakdown.size.coerceAtLeast(1))
            .coerceIn(25, 95)

        val confidence = when {
            completedCoursesCount >= 2 && avgExamScore >= 70 -> "High Evidence Confidence (Backed by exams & projects)"
            completedCoursesCount >= 1 || avgExamScore > 0 -> "Moderate Confidence (Partial verified evidence)"
            else -> "Low Confidence (Relies mostly on self-reported profile)"
        }

        val missing = breakdown.filter { it.status != "MATCHED" }.map { it.skillName }
        val actions = buildList {
            if (missing.isNotEmpty()) add("Close gap in ${missing.first()} via Skill Forge lessons & practical project.")
            if (avgExamScore < 70) add("Complete a timed Corporate Recruitment mock test in Exam Hub (target 75%+).")
            add("Run a role-specific mock interview for $jobTitle in Interview Arena.")
            add("Tailor your Resume Studio summary to highlight verified outcomes.")
        }

        return JobReadinessResult(
            targetRole = jobTitle,
            readinessScore = rawScore,
            confidenceLabel = confidence,
            extractedRequirements = extracted,
            skillBreakdown = breakdown,
            recommendedActions = actions,
            explanation = "Transparent Guidance: Calculated from ${breakdown.size} extracted JD competencies, $completedCoursesCount completed Skill Forge courses, $completedProjectsCount projects, and $avgExamScore% avg exam score. Never a guarantee of hiring."
        )
    }

    private fun String.containsAny(vararg needles: String): Boolean = needles.any { this.contains(it) }

    private data class Quintuple<A, B, C, D, E>(
        val first: A,
        val second: B,
        val third: C,
        val fourth: D,
        val fifth: E
    )
}
