package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.security.MessageDigest

@Database(
    entities = [
        UserEntity::class,
        SavedItemEntity::class,
        RoadmapEntity::class,
        RoadmapMilestoneEntity::class,
        ExamAttemptEntity::class,
        DoubtEntity::class,
        CourseProgressEntity::class,
        CommunicationPracticeEntity::class,
        InterviewSessionEntity::class,
        ResumeEntity::class,
        JobOpportunityEntity::class,
        ApplicationEntity::class,
        MentorshipEntity::class,
        ProfessionalQnaEntity::class,
        VideoItemEntity::class,
        VideoWatchEntity::class,
        LiveSessionEntity::class,
        SessionRegistrationEntity::class,
        CompanyProfileEntity::class,
        EmployeeReviewEntity::class,
        IndustryWatchEntity::class,
        CustomerVoiceEntity::class,
        StudyPlanTaskEntity::class,
        CommunityPostEntity::class,
        AchievementEntity::class,
        NotificationEntity::class,
        ContentReportEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class FutureForgeDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun learningDao(): LearningDao
    abstract fun careerDao(): CareerDao
    abstract fun networkDao(): NetworkDao

    companion object {
        @Volatile
        private var INSTANCE: FutureForgeDatabase? = null

        fun hashPassword(raw: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(("FF_SALT_2026::$raw").toByteArray())
            return digest.joinToString("") { "%02x".format(it) }
        }

        fun getInstance(context: Context): FutureForgeDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context.applicationContext).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): FutureForgeDatabase {
            lateinit var db: FutureForgeDatabase
            db = Room.databaseBuilder(
                context,
                FutureForgeDatabase::class.java,
                "future_forge_db"
            )
                .addCallback(object : Callback() {
                    override fun onCreate(sqliteDb: SupportSQLiteDatabase) {
                        super.onCreate(sqliteDb)
                        CoroutineScope(Dispatchers.IO).launch {
                            seedInitialData(db)
                        }
                    }
                })
                .build()
            return db
        }

        suspend fun seedInitialData(db: FutureForgeDatabase) {
            val userDao = db.userDao()
            val careerDao = db.careerDao()
            val networkDao = db.networkDao()
            val learningDao = db.learningDao()

            val passHash = hashPassword("Forge@2026")
            // 4 Role Seed Accounts
            val student = UserEntity(
                id = "usr_student_1",
                name = "Aarav Mehta",
                email = "student@futureforge.app",
                passwordHash = passHash,
                role = UserRole.STUDENT.name,
                verificationStatus = "VERIFIED",
                educationLevel = "B.Tech Computer Science (Final Year)",
                qualifications = "B.Tech CS (8.7 CGPA), Cloud Foundations",
                interests = "Mobile Architecture, Applied AI, Product Systems",
                strengths = "Analytical Problem Solving, Rapid Prototyping, Writing",
                skills = "Kotlin, Jetpack Compose, SQL, Data Structures, REST APIs",
                careerAmbitions = "Build resilient mobile & AI-enabled software at a top product engineering firm",
                headline = "Aspiring Mobile & Systems Engineer"
            )
            val mentor1 = UserEntity(
                id = "usr_mentor_1",
                name = "Dr. Elena Vance",
                email = "mentor@futureforge.app",
                passwordHash = passHash,
                role = UserRole.PROFESSIONAL.name,
                verificationStatus = "VERIFIED",
                headline = "Principal Mobile Architect @ Meridian Cloud",
                employmentHistory = "11 yrs across Meridian Cloud, Solaris FinTech, and PulseOS",
                expertise = "Kotlin, Distributed Mobile Systems, Engineering Leadership, System Design Interviews",
                availability = "Tue & Thu 6:00 PM - 8:30 PM"
            )
            val mentor2 = UserEntity(
                id = "usr_mentor_2",
                name = "Rohan Kulkarni",
                email = "rohan.mentor@futureforge.app",
                passwordHash = passHash,
                role = UserRole.PROFESSIONAL.name,
                verificationStatus = "VERIFIED",
                headline = "Group Product Manager @ Paycrest Labs",
                employmentHistory = "8 yrs in Fintech Product Strategy & Growth Analytics",
                expertise = "Product Management, MECE Case Prep, SQL Funnels, APM Hiring",
                availability = "Sat 10:00 AM - 1:00 PM"
            )
            val recruiter = UserEntity(
                id = "usr_recruiter_1",
                name = "Nadia Chen",
                email = "recruiter@futureforge.app",
                passwordHash = passHash,
                role = UserRole.RECRUITER.name,
                verificationStatus = "VERIFIED",
                headline = "University & Early Talent Lead",
                companyName = "Meridian Cloud Systems",
                companyIndustry = "Enterprise Cloud & Developer Tools"
            )
            val admin = UserEntity(
                id = "usr_admin_1",
                name = "Victor Sterling",
                email = "admin@futureforge.app",
                passwordHash = passHash,
                role = UserRole.ADMIN.name,
                verificationStatus = "VERIFIED",
                headline = "Platform Governance & Trust Lead"
            )
            val pendingMentor = UserEntity(
                id = "usr_mentor_pending",
                name = "Sana Mir",
                email = "sana.design@futureforge.app",
                passwordHash = passHash,
                role = UserRole.PROFESSIONAL.name,
                verificationStatus = "PENDING",
                headline = "Staff Product Designer @ Atelier Digital",
                employmentHistory = "7 yrs in Design Systems & Accessibility",
                expertise = "UI/UX Architecture, Design Critique, Portfolio Reviews",
                availability = "Wed 5:00 PM - 7:00 PM"
            )
            listOf(student, mentor1, mentor2, recruiter, admin, pendingMentor).forEach {
                userDao.upsertUser(it)
            }

            // Private Companies Intelligence Hub
            val companies = listOf(
                CompanyProfileEntity(
                    id = "comp_meridian",
                    name = "Meridian Cloud Systems",
                    industry = "Enterprise Cloud & Developer Platforms",
                    productsServices = "Distributed observability, API gateways, Mobile SDK telemetry",
                    locations = "Bengaluru, Austin, London (Hybrid)",
                    roleQualifications = "B.E./B.Tech/M.S. in CS or equivalent project portfolio; strong CS fundamentals",
                    requiredSkills = "Kotlin, Go/Java, SQL, Data Structures, Automated Testing",
                    toolsTech = "Jetpack Compose, Kubernetes, PostgreSQL, OpenTelemetry",
                    certifications = "Cloud Associate / Kotlin Android Specialist (helpful, not mandatory)",
                    recruitmentStages = "1. Online Coding & Aptitude (60m) -> 2. Technical Architecture Round -> 3. Practical Debugging -> 4. Leadership & Values",
                    fresherInfo = "2026 Graduate Engineer Cohort opens Aug-Nov; 6-month conversion internships available",
                    internshipInfo = "Summer & Winter Software Engineering Internships with mentorship & PPO track",
                    prepChecklist = "Master hash tables & graph traversal|Build 1 tested Compose or API project|Review SQL indexing & HTTP status semantics",
                    salaryBandSummary = "Intern: INR 45k-60k/mo | Fresher SWE: INR 12L-16L/yr",
                    sourceType = "OFFICIAL_EMPLOYER",
                    lastUpdated = "2026-11-10",
                    verified = true
                ),
                CompanyProfileEntity(
                    id = "comp_paycrest",
                    name = "Paycrest Labs",
                    industry = "Fintech & Merchant Payments",
                    productsServices = "Instant B2B settlement rails, fraud risk scoring, merchant analytics app",
                    locations = "Mumbai, Gurugram, Singapore (Hybrid 3 days/wk)",
                    roleQualifications = "Engineering, Economics, or Business graduates with strong analytical rigor",
                    requiredSkills = "SQL, Product Analytics, Risk Modeling, Kotlin/Backend, Communication",
                    toolsTech = "Python, Looker, Kafka, Android SDK",
                    certifications = "Payments Compliance & Data Analytics Foundations",
                    recruitmentStages = "1. Aptitude & SQL Case -> 2. Product/Tech Deep Dive -> 3. Stakeholder Simulation",
                    fresherInfo = "APM & Associate Software Analyst drives across Tier-1/2 campuses & open portal",
                    internshipInfo = "12-week Product & Fintech Engineering Internships",
                    prepChecklist = "Practice funnel & unit-economics cases|Know idempotency & transaction safety|Review Customer Voice themes",
                    salaryBandSummary = "Intern: INR 35k-50k/mo | Fresher: INR 10L-15L/yr",
                    sourceType = "TRUSTED_FEED",
                    lastUpdated = "2026-11-08",
                    verified = true
                ),
                CompanyProfileEntity(
                    id = "comp_verve",
                    name = "Verve BioAnalytics",
                    industry = "HealthTech & Applied AI",
                    productsServices = "Clinical workflow intelligence, diagnostic imaging triage",
                    locations = "Hyderabad, Boston (Remote-friendly)",
                    roleQualifications = "STEM or Data graduates with clean Python/SQL and ethics awareness",
                    requiredSkills = "Python, SQL, Statistical Validation, Data Visualization, Technical Writing",
                    toolsTech = "PyTorch, BigQuery, FastAPI, Compose",
                    certifications = "Applied Data Ethics & Healthcare Privacy Essentials",
                    recruitmentStages = "1. Take-home Data Notebook -> 2. Technical Review -> 3. Domain Panel",
                    fresherInfo = "Junior Data & ML Evaluation roles open year-round",
                    internshipInfo = "Research & Data Engineering Co-op (6 months)",
                    prepChecklist = "Focus on precision/recall tradeoffs|Cleanly document assumptions|Practice explaining models plainly",
                    salaryBandSummary = "Intern: INR 30k-45k/mo | Fresher: INR 8.5L-13L/yr",
                    sourceType = "THIRD_PARTY",
                    lastUpdated = "2026-10-29",
                    verified = false
                )
            )
            companies.forEach { careerDao.upsertCompany(it) }

            // Opportunities & Corporate Vacancies
            val opportunities = listOf(
                JobOpportunityEntity(
                    id = "opp_1",
                    title = "Mobile Software Engineering Intern (Kotlin)",
                    companyId = "comp_meridian",
                    companyName = "Meridian Cloud Systems",
                    type = "INTERNSHIP",
                    department = "Mobile Platform",
                    location = "Bengaluru (Hybrid)",
                    eligibility = "2026/2027 Graduates in CS/IT or equivalent project portfolio",
                    requiredSkills = "Kotlin, Jetpack Compose, REST APIs, Data Structures",
                    salaryOrStipend = "INR 55,000 / month",
                    deadline = "2026-12-15",
                    instructions = "Submit tailored Resume Studio profile + 1 GitHub/portfolio project link.",
                    sourceType = "OFFICIAL_EMPLOYER",
                    sourceName = "Meridian Official Recruiter Portal",
                    lastUpdated = "2026-11-15",
                    verified = true,
                    hiringStatus = "OPEN",
                    postedByUserId = "usr_recruiter_1"
                ),
                JobOpportunityEntity(
                    id = "opp_2",
                    title = "Graduate Software Engineer (Fresher Cohort 2026)",
                    companyId = "comp_meridian",
                    companyName = "Meridian Cloud Systems",
                    type = "FRESHER",
                    department = "Core Systems",
                    location = "Bengaluru / Austin",
                    eligibility = "0-1 yr experience; CGPA >= 7.0 or strong open-source record",
                    requiredSkills = "Algorithms, SQL, Kotlin/Java, Unit Testing",
                    salaryOrStipend = "INR 13.5L - 16.0L / yr (Est.)",
                    deadline = "2026-12-22",
                    instructions = "Includes online DSA screening + 2 technical interviews.",
                    sourceType = "OFFICIAL_EMPLOYER",
                    sourceName = "Meridian Campus Drive Announcement",
                    lastUpdated = "2026-11-16",
                    verified = true,
                    hiringStatus = "CAMPUS_DRIVE",
                    postedByUserId = "usr_recruiter_1"
                ),
                JobOpportunityEntity(
                    id = "opp_3",
                    title = "Associate Product Manager Intern",
                    companyId = "comp_paycrest",
                    companyName = "Paycrest Labs",
                    type = "INTERNSHIP",
                    department = "Merchant Growth",
                    location = "Mumbai (Hybrid)",
                    eligibility = "Final-year undergrad or MBA; strong SQL & structured thinking",
                    requiredSkills = "Product Thinking, SQL, Communication, Analytics",
                    salaryOrStipend = "INR 42,000 / month",
                    deadline = "2026-12-05",
                    instructions = "Apply with a 1-page product critique in your Portfolio.",
                    sourceType = "TRUSTED_FEED",
                    sourceName = "Paycrest Careers Verified Feed",
                    lastUpdated = "2026-11-12",
                    verified = true,
                    hiringStatus = "CLOSING_SOON",
                    postedByUserId = "usr_recruiter_1"
                ),
                JobOpportunityEntity(
                    id = "opp_4",
                    title = "Future Tech Merit Scholarship & Mentorship Grant",
                    companyId = "comp_meridian",
                    companyName = "Meridian Foundation (Private)",
                    type = "SCHOLARSHIP",
                    department = "STEM & Digital Design",
                    location = "Pan-India / Remote",
                    eligibility = "Enrolled undergrad learners demonstrating financial need & project initiative",
                    requiredSkills = "Problem Solving, Coding or Digital Design",
                    salaryOrStipend = "INR 1,00,000 Tuition Grant + Laptop Stipend",
                    deadline = "2026-12-30",
                    instructions = "Submit academic record + Destiny Roadmap essay.",
                    sourceType = "OFFICIAL_EMPLOYER",
                    sourceName = "Meridian CSR Portal",
                    lastUpdated = "2026-11-10",
                    verified = true,
                    hiringStatus = "OPEN",
                    postedByUserId = "usr_recruiter_1"
                ),
                JobOpportunityEntity(
                    id = "opp_5",
                    title = "National FinTech Builders Case & Coding Challenge",
                    companyId = "comp_paycrest",
                    companyName = "Paycrest Labs",
                    type = "COMPETITION",
                    department = "Innovation Challenge",
                    location = "Online Finals in Mumbai",
                    eligibility = "Teams of 1-3 students or early-career professionals",
                    requiredSkills = "APIs, Product Design, Presentation",
                    salaryOrStipend = "INR 3,00,000 Prize Pool + Fast-Track Interviews",
                    deadline = "2026-12-18",
                    instructions = "Register with prototype pitch & system diagram.",
                    sourceType = "TRUSTED_FEED",
                    sourceName = "Paycrest Innovation Desk",
                    lastUpdated = "2026-11-14",
                    verified = true,
                    hiringStatus = "OPEN",
                    postedByUserId = "usr_recruiter_1"
                ),
                JobOpportunityEntity(
                    id = "opp_6",
                    title = "Junior Data & BI Analyst (Apprenticeship)",
                    companyId = "comp_verve",
                    companyName = "Verve BioAnalytics",
                    type = "APPRENTICESHIP",
                    department = "Clinical Analytics",
                    location = "Hyderabad / Remote",
                    eligibility = "Graduates from any quantitative discipline",
                    requiredSkills = "SQL, Python, Data Visualization, Communication",
                    salaryOrStipend = "INR 32,000 / month (6-mo Apprenticeship)",
                    deadline = "2026-12-28",
                    instructions = "Third-party aggregated listing; verify directly on employer site.",
                    sourceType = "THIRD_PARTY",
                    sourceName = "TechCareers Aggregator (Dated Report)",
                    lastUpdated = "2026-11-04",
                    verified = false,
                    hiringStatus = "OPEN",
                    postedByUserId = "usr_recruiter_1"
                )
            )
            opportunities.forEach { careerDao.upsertOpportunity(it) }

            // Employee Reviews
            listOf(
                EmployeeReviewEntity(
                    id = "rev_1",
                    companyId = "comp_meridian",
                    companyName = "Meridian Cloud Systems",
                    authorName = "K. Sharma (Verified SWE II)",
                    roleTitle = "Software Engineer II",
                    verificationType = "VERIFIED_EMPLOYEE",
                    workCultureRating = 5,
                    workLifeBalanceRating = 4,
                    managementRating = 4,
                    learningRating = 5,
                    hybridRemotePolicy = "3 days in-office (Tue-Thu), flexible core hours",
                    interviewExperience = "Practical debugging round was realistic and collaborative; no trick riddles.",
                    reviewText = "Strong engineering mentorship and documented RFC culture. On-call rotations can get busy during major releases."
                ),
                EmployeeReviewEntity(
                    id = "rev_2",
                    companyId = "comp_paycrest",
                    companyName = "Paycrest Labs",
                    authorName = "Anonymous Reviewer",
                    roleTitle = "Product Analyst",
                    verificationType = "ANONYMOUS",
                    workCultureRating = 4,
                    workLifeBalanceRating = 3,
                    managementRating = 4,
                    learningRating = 5,
                    hybridRemotePolicy = "4 days in-office in Mumbai HQ",
                    interviewExperience = "Heavy emphasis on SQL funnels and merchant profitability math.",
                    reviewText = "Fast-paced fintech learning curve with high ownership early on, though sprint deadlines around quarter-end are intense."
                ),
                EmployeeReviewEntity(
                    id = "rev_3",
                    companyId = "comp_verve",
                    companyName = "Verve BioAnalytics",
                    authorName = "P. Nair",
                    roleTitle = "Data Intern",
                    verificationType = "UNVERIFIED",
                    workCultureRating = 4,
                    workLifeBalanceRating = 5,
                    managementRating = 4,
                    learningRating = 4,
                    hybridRemotePolicy = "Remote-first with quarterly team offsites",
                    interviewExperience = "Take-home notebook followed by friendly walkthrough.",
                    reviewText = "Great work-life balance and supportive peers; onboarding documentation could be more structured."
                )
            ).forEach { careerDao.upsertReview(it) }

            // Industry Watch
            listOf(
                IndustryWatchEntity(
                    id = "iw_1",
                    companyName = "Meridian Cloud Systems",
                    sector = "Enterprise SaaS & Mobile Tooling",
                    category = "EXPANSION",
                    headline = "Meridian Opens New Bengaluru Mobile Systems Hub, Adding 180 Early-Career Seats",
                    summary = "Expansion focuses on Jetpack Compose SDKs, edge telemetry, and developer observability tooling.",
                    inDemandSkills = "Kotlin, Jetpack Compose, OpenTelemetry, Distributed Caching",
                    sourceName = "TechPulse Corporate Dispatch",
                    publishedDate = "2026-11-11"
                ),
                IndustryWatchEntity(
                    id = "iw_2",
                    companyName = "Paycrest Labs",
                    sector = "Fintech & B2B Payments",
                    category = "PARTNERSHIP_MA",
                    headline = "Paycrest Partners with 14 Regional Banks for Instant Cross-Border SMB Settlements",
                    summary = "Creates rising demand for compliance analysts, API integration engineers, and merchant UX designers.",
                    inDemandSkills = "REST APIs, Idempotency Patterns, SQL, Risk Analytics",
                    sourceName = "FinLedger Daily",
                    publishedDate = "2026-11-06"
                ),
                IndustryWatchEntity(
                    id = "iw_3",
                    companyName = "Sector Watch: Private SaaS",
                    sector = "Cloud Software",
                    category = "LAYOFF_REPORT",
                    headline = "Legacy Support Automation Drives Restructuring Across Mid-Market CRM Vendors",
                    summary = "Dated industry reporting notes 6% workforce reduction in manual QA/tier-1 support while AI-engineering & product analytics hiring rose 14%.",
                    inDemandSkills = "Test Automation, Applied AI Evaluation, Customer Success Engineering",
                    sourceName = "Enterprise Workforce Monitor",
                    publishedDate = "2026-10-30"
                )
            ).forEach { careerDao.upsertIndustryWatch(it) }

            // Customer Voice
            listOf(
                CustomerVoiceEntity(
                    id = "cv_1",
                    companyName = "Paycrest Labs",
                    category = "CASE_STUDY",
                    rating = 4,
                    summary = "Merchants praised 2-minute payout settlement speed, while reporting confusion in multi-currency fee breakdowns.",
                    companyResponse = "Paycrest launched an itemized interactive fee preview drawer in v4.2, cutting support tickets by 28%.",
                    businessCaseTakeaway = "Transparent pricing UX directly lowers customer support overhead and boosts B2B trust.",
                    sourceName = "Merchant Feedback Digest",
                    publishedDate = "2026-11-09"
                ),
                CustomerVoiceEntity(
                    id = "cv_2",
                    companyName = "Meridian Cloud Systems",
                    category = "PRODUCT_REVIEW",
                    rating = 5,
                    summary = "Engineering teams rated Meridian's crash-telemetry SDK 4.7/5 for low battery overhead; requested clearer cold-start flamegraphs.",
                    companyResponse = "Product team added cold-start breakdown markers to Q4 roadmap.",
                    businessCaseTakeaway = "Developer tools win adoption via negligible runtime overhead and actionable diagnostics.",
                    sourceName = "DevTools Buyer Review Index",
                    publishedDate = "2026-11-03"
                )
            ).forEach { careerDao.upsertCustomerVoice(it) }

            // Playable Professional Video Library (Real MP4 streams + Day in My Career)
            val videos = listOf(
                VideoItemEntity(
                    id = "vid_1",
                    title = "Day in My Career: Principal Mobile Architect",
                    description = "Walk through architecture reviews, Compose performance profiling, mentoring juniors, and balancing deep-work mornings.",
                    creatorId = "usr_mentor_1",
                    creatorName = "Dr. Elena Vance",
                    creatorRole = "Principal Mobile Architect @ Meridian Cloud",
                    category = "DAY_IN_CAREER",
                    industry = "Technology & Cloud",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    durationLabel = "06:15",
                    dayInCareerSummary = "Daily Responsibilities: RFC reviews, profiling UI jank, unblocking 4 squads | Skills: Kotlin, Architecture, Leadership | Growth: Staff -> Principal"
                ),
                VideoItemEntity(
                    id = "vid_2",
                    title = "Cracking Product & Fintech Case Interviews",
                    description = "How hiring panels evaluate structured MECE thinking, metric estimation, and calm trade-off reasoning.",
                    creatorId = "usr_mentor_2",
                    creatorName = "Rohan Kulkarni",
                    creatorRole = "Group PM @ Paycrest Labs",
                    category = "INTERVIEW",
                    industry = "Fintech & Product",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    durationLabel = "08:40",
                    dayInCareerSummary = "Focus: Structured frameworks, clarifying questions, and quantifying customer impact."
                ),
                VideoItemEntity(
                    id = "vid_3",
                    title = "Clean StateFlow & Repository Patterns in Production",
                    description = "Practical educational deep-dive on avoiding race conditions and separating UI state cleanly.",
                    creatorId = "usr_mentor_1",
                    creatorName = "Dr. Elena Vance",
                    creatorRole = "Principal Mobile Architect",
                    category = "EDUCATIONAL",
                    industry = "Software Engineering",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerFun.mp4",
                    durationLabel = "11:20"
                ),
                VideoItemEntity(
                    id = "vid_4",
                    title = "Recorded Webinar: 2026 Private-Sector Fresher Hiring Trends",
                    description = "Recorded expert panel breaking down portfolio expectations, internships, and technical assessments.",
                    creatorId = "usr_mentor_2",
                    creatorName = "Rohan Kulkarni",
                    creatorRole = "Group PM @ Paycrest Labs",
                    category = "RECORDED_WEBINAR",
                    industry = "Cross-Industry Careers",
                    videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerJoy.mp4",
                    durationLabel = "14:05"
                )
            )
            videos.forEach { networkDao.upsertVideo(it) }

            // Live Expert Sessions
            listOf(
                LiveSessionEntity(
                    id = "live_1",
                    title = "Live Architecture Review & Portfolio Clinic",
                    hostId = "usr_mentor_1",
                    hostName = "Dr. Elena Vance",
                    hostTitle = "Principal Mobile Architect",
                    topic = "Mobile Engineering & Internships",
                    scheduleLabel = "Thu, Nov 21 • 7:00 PM IST",
                    description = "Bring your Android/GitHub project for live feedback on structure, testing, and recruiter appeal.",
                    recordingUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                    liveNotes = "Q&A Queue Open: Focus on StateFlow testing, Room migrations, and README clarity."
                ),
                LiveSessionEntity(
                    id = "live_2",
                    title = "Fintech Product & Analytics Career Talk",
                    hostId = "usr_mentor_2",
                    hostName = "Rohan Kulkarni",
                    hostTitle = "Group PM @ Paycrest Labs",
                    topic = "Product Management & Strategy",
                    scheduleLabel = "Sat, Nov 23 • 11:00 AM IST",
                    description = "Learn how early-career engineers and business grads transition into APM cohorts.",
                    recordingUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerEscapes.mp4",
                    liveNotes = "Live Poll: 68% attendees targeting Summer 2027 APM/Analytics internships."
                )
            ).forEach { networkDao.upsertLiveSession(it) }

            // Ask a Professional Q&A
            listOf(
                ProfessionalQnaEntity(
                    id = "qna_1",
                    authorId = "usr_student_1",
                    authorName = "Aarav Mehta",
                    category = "Engineering Careers",
                    question = "Do private tech recruiters value 2 deep, tested projects more than 8 small tutorial clones?",
                    answerText = "Yes—100%. Two production-grade projects with tests, clear architecture diagrams, and real error handling immediately stand out over tutorial clones.",
                    answeredByName = "Dr. Elena Vance",
                    answeredByRole = "Principal Mobile Architect"
                ),
                ProfessionalQnaEntity(
                    id = "qna_2",
                    authorId = "usr_student_1",
                    authorName = "Meera Iyer",
                    category = "Product & Business",
                    question = "How can a non-CS student build credible technical fluency for APM interviews?",
                    answerText = "Start by mastering SQL joins/aggregations, REST API basics, and authoring 2 teardown case studies with real customer feedback metrics.",
                    answeredByName = "Rohan Kulkarni",
                    answeredByRole = "Group PM @ Paycrest Labs"
                )
            ).forEach { networkDao.upsertQna(it) }

            // Peer Community Posts
            listOf(
                CommunityPostEntity(
                    id = "post_1",
                    authorId = "usr_student_1",
                    authorName = "Aarav Mehta",
                    category = "CODING",
                    title = "7-Day DSA & Kotlin Architecture Study Sprint",
                    content = "Sharing my daily breakdown: 45 mins complexity practice in Exam Hub + 1 Skill Forge lesson. Anyone want to compare notes on hash-table edge cases?",
                    likesCount = 14
                ),
                CommunityPostEntity(
                    id = "post_2",
                    authorId = "usr_mentor_2",
                    authorName = "Rohan Kulkarni",
                    category = "CAREER_ADVICE",
                    title = "Community Challenge: Audit One Customer Voice Case Study This Week",
                    content = "Pick any company in Customer Voice, identify a real user friction point, and draft a 3-sentence product fix for your portfolio.",
                    likesCount = 22
                )
            ).forEach { networkDao.upsertCommunityPost(it) }

            // Seed initial starter roadmap, study plan, resume & welcome notification for demo student
            val starterRoadmap = RoadmapEntity(
                id = "rm_seed_1",
                userId = student.id,
                targetCareer = "Software & Mobile Systems Engineer",
                summary = "Your connected 16-week Future Forge pathway from CS foundations to verified private-sector engineering internships.",
                educationSnapshot = student.educationLevel,
                skillsSnapshot = student.skills
            )
            learningDao.upsertRoadmap(starterRoadmap)
            learningDao.insertMilestones(
                listOf(
                    RoadmapMilestoneEntity("ms_1", starterRoadmap.id, student.id, "1. Academic Base", "Revise Algorithms & Database Indexing", "Review Big-O, hash tables, and SQL HAVING vs WHERE.", "ExamHub", "Weeks 1-2", true, true, 0),
                    RoadmapMilestoneEntity("ms_2", starterRoadmap.id, student.id, "2. Skill Forge", "Complete Modern Software & Systems Path", "Finish structured video lessons & project brief.", "SkillForge", "Weeks 3-5", true, false, 1),
                    RoadmapMilestoneEntity("ms_3", starterRoadmap.id, student.id, "3. Exam Prep", "Pass Corporate Tech Recruitment Mock", "Score 75%+ on timed DSA & Systems mock exam.", "ExamHub", "Weeks 6-8", false, false, 2),
                    RoadmapMilestoneEntity("ms_4", starterRoadmap.id, student.id, "4. Resume & Portfolio", "Publish Tailored Engineering Resume", "Quantify capstone impact and export resume.", "ResumeStudio", "Week 9", false, false, 3),
                    RoadmapMilestoneEntity("ms_5", starterRoadmap.id, student.id, "5. Interview Arena", "Complete Technical & HR Simulations", "Practice multi-turn system & behavioral Q&A.", "InterviewArena", "Weeks 10-12", false, false, 4),
                    RoadmapMilestoneEntity("ms_6", starterRoadmap.id, student.id, "6. Mentorship & Jobs", "Book Mentor Clinic & Apply to Meridian", "Verify job readiness and submit internship application.", "CompanyJobs", "Weeks 13-16", false, false, 5)
                )
            )
            learningDao.upsertStudyTask(
                StudyPlanTaskEntity(
                    id = "st_1",
                    userId = student.id,
                    title = "Complete Corporate Tech Recruitment Mock #1",
                    subject = "DSA & Systems",
                    dueDate = "2026-11-22",
                    durationMinutes = 45,
                    priority = "HIGH",
                    reminderEnabled = true,
                    completed = false
                )
            )
            careerDao.upsertResume(
                ResumeEntity(
                    id = "res_1",
                    userId = student.id,
                    versionTitle = "Mobile & Systems Intern v1",
                    fullName = student.name,
                    email = student.email,
                    phone = "+91 98000 12345",
                    summary = "Final-year Computer Science undergraduate building clean Kotlin/Jetpack Compose mobile applications and reliable local-first architectures.",
                    education = "B.Tech Computer Science (2023-2027) — CGPA 8.7/10",
                    experience = "Open-Source Campus App Contributor (2025): Built offline caching layer reducing load latency by 38%.",
                    skills = "Kotlin, Jetpack Compose, Room SQLite, Data Structures, REST APIs, Unit Testing",
                    projects = "1. ScholarSync Study Planner (Compose + Room + Robolectric)\n2. Distributed URL Telemetry CLI (Kotlin + Coroutines)",
                    portfolioLinks = "github.com/aarav-mehta-dev",
                    coverLetter = "Dear Meridian Cloud Recruiting Team,\n\nI am excited to apply for the Mobile Software Engineering Intern role. Through my coursework and hands-on Jetpack Compose architecture projects, I have focused on measurable performance and automated testing."
                )
            )
            networkDao.insertNotification(
                NotificationEntity(
                    id = "notif_welcome",
                    userId = student.id,
                    title = "Destiny Roadmap Initialized",
                    message = "Your Software & Mobile Systems Engineer pathway is active with 6 trackable stages.",
                    category = "ROADMAP"
                )
            )
        }
    }
}
