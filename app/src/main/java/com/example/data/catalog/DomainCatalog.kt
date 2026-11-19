package com.example.data.catalog

data class ExamQuestion(
    val id: String,
    val prompt: String,
    val options: List<String>,
    val correctIndex: Int,
    val explanation: String,
    val topic: String
)

data class ExamModule(
    val id: String,
    val title: String,
    val category: String, // Academic, Private Entrance, Corporate Recruitment
    val licenseNote: String,
    val durationMinutes: Int,
    val studyNotes: List<String>,
    val questions: List<ExamQuestion>
)

data class SkillLesson(
    val id: String,
    val title: String,
    val summary: String,
    val keyConcept: String,
    val exercisePrompt: String,
    val videoUrl: String
)

data class SkillPath(
    val id: String,
    val title: String,
    val domain: String,
    val level: String,
    val estimatedHours: Int,
    val overview: String,
    val lessons: List<SkillLesson>,
    val projectBrief: String,
    val quizQuestion: String,
    val quizOptions: List<String>,
    val quizCorrectIndex: Int,
    val nextStageRecommendation: String
)

data class SalaryBenchmark(
    val id: String,
    val role: String,
    val industry: String,
    val location: String,
    val internshipStipendMonthly: String,
    val fresherRangeAnnual: String,
    val midLevelRangeAnnual: String,
    val seniorRangeAnnual: String,
    val demandTrend: String,
    val growthPath: String,
    val sourceName: String,
    val lastUpdated: String,
    val assumptions: String
)

object DomainCatalog {
    val exams: List<ExamModule> = listOf(
        ExamModule(
            id = "exam_swe_aptitude",
            title = "Corporate Tech Recruitment: DSA & Systems",
            category = "Corporate Recruitment",
            licenseNote = "Original Future Forge curated practice set (2026 pattern)",
            durationMinutes = 12,
            studyNotes = listOf(
                "Time Complexity: Hash map lookups average O(1); balanced BST operations run in O(log n).",
                "Concurrency: Prefer immutable state and structured concurrency to prevent race conditions.",
                "Database Indexing: B-Tree indexes accelerate range queries while composite index column order matters."
            ),
            questions = listOf(
                ExamQuestion(
                    id = "q1",
                    prompt = "Which data structure guarantees O(1) amortized lookup and insertion for unique keys without ordering guarantees?",
                    options = listOf("Binary Search Tree", "Hash Table", "Linked List", "Min-Heap"),
                    correctIndex = 1,
                    explanation = "A Hash Table computes bucket indices via a hash function, yielding O(1) average lookup and insertion.",
                    topic = "Data Structures"
                ),
                ExamQuestion(
                    id = "q2",
                    prompt = "What is the worst-case time complexity of Merge Sort on an array of n elements?",
                    options = listOf("O(n)", "O(n log n)", "O(n^2)", "O(log n)"),
                    correctIndex = 1,
                    explanation = "Merge Sort always divides the array in half (log n levels) and merges in O(n) per level, guaranteeing O(n log n).",
                    topic = "Algorithms"
                ),
                ExamQuestion(
                    id = "q3",
                    prompt = "In SQL, which clause filters aggregated groups produced by GROUP BY?",
                    options = listOf("WHERE", "HAVING", "ORDER BY", "DISTINCT"),
                    correctIndex = 1,
                    explanation = "WHERE filters rows before grouping, whereas HAVING filters grouped results after aggregation.",
                    topic = "Databases"
                ),
                ExamQuestion(
                    id = "q4",
                    prompt = "Which HTTP status code indicates a client request lacked valid authentication credentials?",
                    options = listOf("400 Bad Request", "401 Unauthorized", "404 Not Found", "502 Bad Gateway"),
                    correctIndex = 1,
                    explanation = "HTTP 401 signals missing or invalid authentication credentials; 403 means authenticated but forbidden.",
                    topic = "Backend & APIs"
                )
            )
        ),
        ExamModule(
            id = "exam_quant_entrance",
            title = "Private Management & Quant Entrance Test",
            category = "Private Entrance",
            licenseNote = "Licensed-style quantitative & logical reasoning mock",
            durationMinutes = 15,
            studyNotes = listOf(
                "Percentages: Net sequential change of +a% and -a% always yields a net loss of (a^2 / 100)%.",
                "Probability: Complement rule P(at least one) = 1 - P(none) simplifies multi-draw problems.",
                "Data Interpretation: Always check axis units and base year before comparing growth rates."
            ),
            questions = listOf(
                ExamQuestion(
                    id = "q1",
                    prompt = "A product price rises by 20% and then drops by 20%. What is the net percentage change?",
                    options = listOf("0% change", "-4% decrease", "+4% increase", "-2% decrease"),
                    correctIndex = 1,
                    explanation = "100 -> 120 -> 96, which is a net 4% decrease (20^2 / 100 = 4%).",
                    topic = "Percentages"
                ),
                ExamQuestion(
                    id = "q2",
                    prompt = "If 6 analysts complete an audit in 10 days, how many days will 15 analysts take at the same pace?",
                    options = listOf("3 days", "4 days", "5 days", "6 days"),
                    correctIndex = 1,
                    explanation = "Total effort = 6 * 10 = 60 analyst-days. Divided by 15 analysts = 4 days.",
                    topic = "Work & Time"
                ),
                ExamQuestion(
                    id = "q3",
                    prompt = "What is the probability of rolling a sum of 7 with two fair six-sided dice?",
                    options = listOf("1/12", "1/6", "5/36", "7/36"),
                    correctIndex = 1,
                    explanation = "6 favorable pairs ((1,6),(2,5),(3,4),(4,3),(5,2),(6,1)) out of 36 outcomes = 6/36 = 1/6.",
                    topic = "Probability"
                )
            )
        ),
        ExamModule(
            id = "exam_academic_cs",
            title = "Academic Core: Operating Systems & Networks",
            category = "Academic",
            licenseNote = "University Computer Science revision paper",
            durationMinutes = 10,
            studyNotes = listOf(
                "Deadlock requires 4 Coffman conditions: Mutual Exclusion, Hold & Wait, No Preemption, Circular Wait.",
                "TCP guarantees ordered delivery with 3-way handshake; UDP prioritizes low-latency datagrams."
            ),
            questions = listOf(
                ExamQuestion(
                    id = "q1",
                    prompt = "Which condition is NOT one of the four Coffman conditions for deadlock?",
                    options = listOf("Mutual Exclusion", "Circular Wait", "Preemptive Scheduling", "Hold and Wait"),
                    correctIndex = 2,
                    explanation = "Deadlock requires NO preemption. Preemptive scheduling prevents deadlock rather than causing it.",
                    topic = "Operating Systems"
                ),
                ExamQuestion(
                    id = "q2",
                    prompt = "Which OSI layer handles logical IP addressing and packet routing?",
                    options = listOf("Data Link Layer", "Network Layer", "Transport Layer", "Session Layer"),
                    correctIndex = 1,
                    explanation = "Layer 3 (Network Layer) handles IP addressing and routing across networks.",
                    topic = "Computer Networks"
                ),
                ExamQuestion(
                    id = "q3",
                    prompt = "What problem does virtual memory paging primarily solve?",
                    options = listOf("Eliminates CPU cache misses", "Allows non-contiguous physical memory allocation", "Replaces disk storage", "Prevents SQL injection"),
                    correctIndex = 1,
                    explanation = "Paging maps contiguous virtual pages to non-contiguous physical frames, preventing external fragmentation.",
                    topic = "Memory Management"
                )
            )
        )
    )

    val skillPaths: List<SkillPath> = listOf(
        SkillPath(
            id = "path_coding",
            title = "Modern Software & Systems Engineering",
            domain = "Coding",
            level = "Foundation to Job-Ready",
            estimatedHours = 18,
            overview = "Master clean architecture, data structures, REST APIs, and automated testing for private-sector tech roles.",
            lessons = listOf(
                SkillLesson(
                    "l_code_1",
                    "Algorithmic Thinking & Complexity",
                    "Analyze time/space tradeoffs before writing production code.",
                    "Big-O notation measures asymptotic growth; always identify bottleneck loops and auxiliary memory.",
                    "Refactor a nested O(n^2) duplicate checker into an O(n) HashSet implementation.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
                ),
                SkillLesson(
                    "l_code_2",
                    "Layered Architecture & State Management",
                    "Separate UI, ViewModel state flows, and repository persistence cleanly.",
                    "Unidirectional data flow makes mobile and backend services testable and predictable.",
                    "Design a Repository + StateFlow contract for offline-resilient task syncing.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4"
                )
            ),
            projectBrief = "Build a REST-ready Job Application Tracker module with input validation, local persistence, and 3 unit tests.",
            quizQuestion = "Why does Unidirectional Data Flow improve maintainability in Compose apps?",
            quizOptions = listOf(
                "It eliminates the need for databases",
                "State flows down and events flow up, making state changes predictable and testable",
                "It skips compilation checks"
            ),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Take the Corporate Tech Recruitment Mock Exam and practice Technical Interviews."
        ),
        SkillPath(
            id = "path_comm",
            title = "Executive & Workplace Communication",
            domain = "Communication",
            level = "Intermediate",
            estimatedHours = 10,
            overview = "Structure persuasive briefings, stakeholder updates, and crisp STAR interview narratives.",
            lessons = listOf(
                SkillLesson(
                    "l_comm_1",
                    "Pyramid Principle for Crisp Briefings",
                    "Lead with the recommendation, then support with three structured pillars.",
                    "Executives decide faster when the core conclusion appears in the first 20 seconds.",
                    "Rewrite a 200-word project status paragraph into a 3-bullet executive headline.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
                ),
                SkillLesson(
                    "l_comm_2",
                    "Active Listening & Clarifying Questions",
                    "Decode ambiguous client or interviewer prompts with precision.",
                    "Confirm constraints, success metrics, and timeline before proposing solutions.",
                    "Draft 4 clarifying questions for an ambiguous product redesign brief.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4"
                )
            ),
            projectBrief = "Record a 2-minute STAR story explaining a complex conflict you resolved and critique clarity.",
            quizQuestion = "What belongs in the opening sentence of a Pyramid Principle update?",
            quizOptions = listOf("Background history", "The primary answer or recommendation", "A list of team members"),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Complete 3 Public Speaking exercises in Communication Lab."
        ),
        SkillPath(
            id = "path_leadership",
            title = "Adaptive Leadership & Team Ownership",
            domain = "Leadership",
            level = "Intermediate",
            estimatedHours = 12,
            overview = "Lead cross-functional peers, delegate clearly, run retrospectives, and resolve blockers calmly.",
            lessons = listOf(
                SkillLesson(
                    "l_lead_1",
                    "Delegation by Outcomes, Not Micromanagement",
                    "Define clear ownership boundaries, constraints, and review checkpoints.",
                    "Psychological safety + explicit accountability drives high-trust delivery.",
                    "Create a RACI delegation brief for a 4-person college hackathon team.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerMeltdowns.mp4"
                )
            ),
            projectBrief = "Facilitate a 15-minute team retrospective and document 3 actionable process improvements.",
            quizQuestion = "What is the primary goal of a blameless retrospective?",
            quizOptions = listOf("Assign individual blame", "Identify systemic improvements to prevent repeat issues", "Cancel future sprints"),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Book a mentor session to review your leadership case study."
        ),
        SkillPath(
            id = "path_problem_solving",
            title = "Structured Problem-Solving & Case Logic",
            domain = "Problem-Solving",
            level = "All Levels",
            estimatedHours = 14,
            overview = "Break open-ended business and engineering bottlenecks into MECE issue trees and testable hypotheses.",
            lessons = listOf(
                SkillLesson(
                    "l_prob_1",
                    "MECE Issue Trees & Root Cause Analysis",
                    "Ensure categories are Mutually Exclusive and Collectively Exhaustive.",
                    "Never jump to solutions before isolating whether a drop is acquisition, activation, or retention.",
                    "Construct a 2-level MECE tree for a 15% drop in mobile checkout conversion.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackOnStreetAndDirt.mp4"
                )
            ),
            projectBrief = "Diagnose a simulated SaaS churn spike using funnel metrics and write a 1-page action memo.",
            quizQuestion = "What does MECE stand for in structured problem solving?",
            quizOptions = listOf(
                "Most Efficient Cost Estimate",
                "Mutually Exclusive, Collectively Exhaustive",
                "Managed Execution Control Engine"
            ),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Explore Product Management roles in Company Intelligence Hub."
        ),
        SkillPath(
            id = "path_creativity",
            title = "Applied Creativity & Design Thinking",
            domain = "Creativity",
            level = "Foundation",
            estimatedHours = 9,
            overview = "Move from user empathy and rapid divergent ideation to testable low-fidelity prototypes.",
            lessons = listOf(
                SkillLesson(
                    "l_cre_1",
                    "Divergent Ideation & Constraint Framing",
                    "Use 'How Might We' prompts and strict constraints to spark novel solutions.",
                    "Separate idea generation from judgment to double original concept quality.",
                    "Generate 8 distinct onboarding concepts for first-generation college students.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/WeAreGoingOnBullrun.mp4"
                )
            ),
            projectBrief = "Run 2 user interviews and synthesize 3 testable product prototypes in your portfolio.",
            quizQuestion = "Why separate divergent ideation from convergent evaluation?",
            quizOptions = listOf(
                "Premature criticism suppresses non-obvious creative concepts",
                "It eliminates the need for user research",
                "It guarantees patent approval"
            ),
            quizCorrectIndex = 0,
            nextStageRecommendation = "Pair this with Graphic & Digital Design."
        ),
        SkillPath(
            id = "path_marketing",
            title = "Digital Marketing, SEO & Growth Analytics",
            domain = "Digital Marketing",
            level = "Job-Ready",
            estimatedHours = 15,
            overview = "Plan full-funnel acquisition campaigns, attribution metrics, SEO intent mapping, and retention loops.",
            lessons = listOf(
                SkillLesson(
                    "l_mkt_1",
                    "Unit Economics: CAC, LTV & Conversion Funnels",
                    "Evaluate campaign viability using payback period and LTV:CAC ratio.",
                    "Vanity impressions do not pay salaries; qualified pipeline and retention do.",
                    "Calculate CAC and 12-month LTV for a simulated ed-tech subscription funnel.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4"
                )
            ),
            projectBrief = "Author a 30-day multi-channel launch campaign with keyword clusters and KPI targets.",
            quizQuestion = "Which metric ratio indicates healthy sustainable acquisition economics?",
            quizOptions = listOf("CAC higher than LTV", "LTV significantly greater than CAC (typically >= 3:1)", "Zero impressions"),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Inspect Customer Voice case studies for marketing insights."
        ),
        SkillPath(
            id = "path_design",
            title = "Graphic & Digital Product Design",
            domain = "Graphic & Digital Design",
            level = "Job-Ready",
            estimatedHours = 16,
            overview = "Master typographic hierarchy, grid discipline, color contrast accessibility, and component systems.",
            lessons = listOf(
                SkillLesson(
                    "l_des_1",
                    "Typography, 8dp Spatial Grids & WCAG Contrast",
                    "Design calm, readable digital interfaces with intentional visual hierarchy.",
                    "Consistent 8dp spacing and 4.5:1 text contrast separate senior craft from amateur layouts.",
                    "Audit an existing screen for spacing alignment, type scale, and contrast ratio.",
                    "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4"
                )
            ),
            projectBrief = "Create a 4-screen mobile design system showcase with tokens, components, and accessibility notes.",
            quizQuestion = "What is the standard minimum WCAG AA contrast ratio for normal body text?",
            quizOptions = listOf("1.5 : 1", "4.5 : 1", "10 : 1"),
            quizCorrectIndex = 1,
            nextStageRecommendation = "Showcase your design project inside Resume & Portfolio Studio."
        )
    )

    val communicationPrompts: Map<String, List<String>> = mapOf(
        "SPEAKING" to listOf(
            "Introduce yourself in 60 seconds for a private-sector graduate role, highlighting one measurable project outcome.",
            "Explain a technical or complex concept from your field to a non-technical business stakeholder."
        ),
        "VOCABULARY" to listOf(
            "Use 'mitigate', 'scalable', and 'stakeholder alignment' accurately in a 3-sentence project update.",
            "Replace vague words ('good', 'big', 'stuff') with precise analytical vocabulary in a product critique."
        ),
        "GRAMMAR" to listOf(
            "Correct and polish: 'Me and my team was working on the database since six months and it don't crash no more.'",
            "Convert passive sentences into crisp active voice describing your internship accomplishments."
        ),
        "SENTENCE" to listOf(
            "Combine these facts into one clear executive sentence: API latency dropped 42%, caching added, user retention rose 9%.",
            "Write a concise cold outreach message requesting an informational coffee chat with a senior engineer."
        ),
        "PUBLIC_SPEAKING" to listOf(
            "Deliver a 90-second persuasive pitch for why a private company should pilot your capstone solution.",
            "Structure a 3-part opening hook, core insight, and call-to-action for a campus tech symposium."
        )
    )

    val salaryBenchmarks: List<SalaryBenchmark> = listOf(
        SalaryBenchmark(
            id = "sal_swe_blr",
            role = "Software Engineer",
            industry = "Cloud & Enterprise SaaS",
            location = "Bengaluru / Hybrid",
            internshipStipendMonthly = "INR 35,000 - 65,000 / mo",
            fresherRangeAnnual = "INR 8.0L - 16.0L / yr",
            midLevelRangeAnnual = "INR 18.0L - 34.0L / yr",
            seniorRangeAnnual = "INR 36.0L - 65.0L+ / yr",
            demandTrend = "High demand (+18% YoY in Cloud, AI tooling & Kotlin/Backend)",
            growthPath = "SWE I -> SWE II -> Senior SWE -> Staff Engineer / Engineering Manager",
            sourceName = "Aggregated Verified Employer Bands & Public Tech Compensation Survey",
            lastUpdated = "2026-10-15",
            assumptions = "Base + standard annual bonus in mid-to-large private product firms; excludes volatile startup equity."
        ),
        SalaryBenchmark(
            id = "sal_swe_us",
            role = "Software Engineer (North America)",
            industry = "Enterprise Tech & AI",
            location = "Austin / Remote US",
            internshipStipendMonthly = "$5,200 - $8,400 / mo",
            fresherRangeAnnual = "$92,000 - $132,000 / yr",
            midLevelRangeAnnual = "$135,000 - $185,000 / yr",
            seniorRangeAnnual = "$190,000 - $275,000 / yr",
            demandTrend = "Selective hiring; strong premium for distributed systems & AI integration",
            growthPath = "Associate Engineer -> Software Engineer -> Senior -> Staff",
            sourceName = "Publicly Filed Wage Disclosures & Private Tech Employer Reports",
            lastUpdated = "2026-10-28",
            assumptions = "Total cash compensation median quartiles; cost-of-living adjustments apply by metro."
        ),
        SalaryBenchmark(
            id = "sal_pm",
            role = "Associate Product Manager / PM",
            industry = "Fintech & Consumer Platforms",
            location = "Mumbai / Gurugram",
            internshipStipendMonthly = "INR 25,000 - 50,000 / mo",
            fresherRangeAnnual = "INR 9.0L - 15.5L / yr",
            midLevelRangeAnnual = "INR 20.0L - 36.0L / yr",
            seniorRangeAnnual = "INR 40.0L - 70.0L / yr",
            demandTrend = "Steady (+12% YoY for analytics-fluent product builders)",
            growthPath = "APM -> Product Manager -> Senior PM -> Group PM / Head of Product",
            sourceName = "Private Industry Compensation Report Q4 2026",
            lastUpdated = "2026-11-02",
            assumptions = "Assumes strong SQL/experimentation fluency and customer discovery portfolio."
        ),
        SalaryBenchmark(
            id = "sal_data",
            role = "Data & Applied AI Analyst",
            industry = "Analytics, Retail & Consulting",
            location = "Hyderabad / Pune",
            internshipStipendMonthly = "INR 22,000 - 45,000 / mo",
            fresherRangeAnnual = "INR 6.5L - 12.5L / yr",
            midLevelRangeAnnual = "INR 14.0L - 26.0L / yr",
            seniorRangeAnnual = "INR 28.0L - 50.0L / yr",
            demandTrend = "Very High (+24% YoY for Python, SQL, BI & ML evaluation skills)",
            growthPath = "Data Analyst -> Senior Analyst -> Analytics Engineer / Data Scientist",
            sourceName = "Verified Recruiter Benchmarks & Analytics Guild Survey",
            lastUpdated = "2026-11-05",
            assumptions = "Mid-market and enterprise private employers; varies by domain complexity."
        ),
        SalaryBenchmark(
            id = "sal_design",
            role = "Product & Digital Designer (UI/UX)",
            industry = "Digital Agencies & SaaS",
            location = "Remote / Tier-1 Metros",
            internshipStipendMonthly = "INR 18,000 - 35,000 / mo",
            fresherRangeAnnual = "INR 5.5L - 10.5L / yr",
            midLevelRangeAnnual = "INR 12.0L - 22.0L / yr",
            seniorRangeAnnual = "INR 25.0L - 44.0L / yr",
            demandTrend = "Growing (+14% YoY for design-system & accessibility specialists)",
            growthPath = "Junior Designer -> Product Designer -> Lead Designer -> Design Director",
            sourceName = "Design Leadership Collective Benchmark",
            lastUpdated = "2026-10-20",
            assumptions = "Portfolio quality and interactive prototyping heavily influence entry band."
        )
    )
}
