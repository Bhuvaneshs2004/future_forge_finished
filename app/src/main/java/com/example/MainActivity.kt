package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.ai.PlaceholderAiRepository
import com.example.data.local.FutureForgeDatabase
import com.example.data.local.SessionPreferences
import com.example.data.local.UserEntity
import com.example.data.local.UserRole
import com.example.data.repository.FutureForgeRepository
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutureForgeTheme {
                FutureForgeRoot()
            }
        }
    }
}

@Composable
fun FutureForgeRoot() {
    val context = LocalContext.current.applicationContext
    val repository = remember {
        FutureForgeRepository(
            db = FutureForgeDatabase.getInstance(context),
            sessionPrefs = SessionPreferences(context)
        )
    }
    val aiRepository = remember { PlaceholderAiRepository() }

    val appViewModel: AppViewModel = viewModel(
        factory = viewModelFactory {
            initializer { AppViewModel(repository) }
        }
    )

    val authState by appViewModel.authState.collectAsStateWithLifecycle()
    val authError by appViewModel.authError.collectAsStateWithLifecycle()
    val authLoading by appViewModel.authLoading.collectAsStateWithLifecycle()

    when (val state = authState) {
        AuthUiState.Loading -> {
            ForgeAtmosphericBackdrop {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = EtherealMist)
                }
            }
        }
        AuthUiState.Unauthenticated -> {
            AuthScreen(
                isLoading = authLoading,
                errorMessage = authError,
                onLogin = appViewModel::login,
                onRegister = appViewModel::register,
                onClearError = appViewModel::clearAuthError
            )
        }
        is AuthUiState.Authenticated -> {
            val user = state.user
            key(user.id) {
                AuthenticatedWorkspace(
                    user = user,
                    repository = repository,
                    aiRepository = aiRepository,
                    appViewModel = appViewModel
                )
            }
        }
    }
}

@Composable
private fun AuthenticatedWorkspace(
    user: UserEntity,
    repository: FutureForgeRepository,
    aiRepository: PlaceholderAiRepository,
    appViewModel: AppViewModel
) {
    val learningVm: LearningViewModel = viewModel(
        key = "learn_${user.id}",
        factory = viewModelFactory {
            initializer {
                LearningViewModel(repository, aiRepository, user.id)
            }
        }
    )
    val ecosystemVm: EcosystemViewModel = viewModel(
        key = "eco_${user.id}",
        factory = viewModelFactory {
            initializer {
                EcosystemViewModel(repository, aiRepository, user.id)
            }
        }
    )

    val statusBanner by appViewModel.statusBanner.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(statusBanner) {
        statusBanner?.let {
            snackbarHostState.showSnackbar(it)
            appViewModel.clearStatusBanner()
        }
    }

    ForgeAtmosphericBackdrop {
        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            snackbarHost = { SnackbarHost(snackbarHostState) },
            contentWindowInsets = WindowInsets.safeDrawing
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (user.role) {
                    UserRole.PROFESSIONAL.name -> {
                        val mentorships by ecosystemVm.allMentorships.collectAsStateWithLifecycle()
                        val qna by ecosystemVm.qnaList.collectAsStateWithLifecycle()
                        val videos by ecosystemVm.videos.collectAsStateWithLifecycle()
                        val liveSessions by ecosystemVm.liveSessions.collectAsStateWithLifecycle()
                        MentorDashboardScreen(
                            user = user,
                            mentorships = mentorships,
                            qnaList = qna,
                            videos = videos,
                            liveSessions = liveSessions,
                            onUpdateProfile = appViewModel::updateProfile,
                            onUpdateMentorship = { m, st, fb ->
                                ecosystemVm.updateMentorship(m, st, fb)
                                appViewModel.showBanner("Mentorship updated: $st")
                            },
                            onAnswerQuestion = { q, ans ->
                                ecosystemVm.answerProfessionalQuestion(q, ans, user)
                                appViewModel.showBanner("Answer published.")
                            },
                            onPublishVideo = { t, d, cat, ind, url, day ->
                                ecosystemVm.publishVideo(user, t, d, cat, ind, url, day)
                                appViewModel.showBanner("Video published to Library.")
                            },
                            onHostLiveSession = { t, top, sched, desc ->
                                ecosystemVm.hostLiveSession(user, t, top, sched, desc)
                                appViewModel.showBanner("Live session scheduled.")
                            },
                            onLogout = appViewModel::logout
                        )
                    }
                    UserRole.RECRUITER.name -> {
                        val opps by ecosystemVm.opportunities.collectAsStateWithLifecycle()
                        val apps by ecosystemVm.allApplications.collectAsStateWithLifecycle()
                        val companies by ecosystemVm.companies.collectAsStateWithLifecycle()
                        val customerVoice by ecosystemVm.customerVoice.collectAsStateWithLifecycle()
                        RecruiterDashboardScreen(
                            user = user,
                            opportunities = opps,
                            applications = apps,
                            companies = companies,
                            customerVoice = customerVoice,
                            onPublishOpportunity = { t, c, typ, dep, loc, el, sk, sal, dl, ins, hs ->
                                ecosystemVm.publishOpportunity(t, c, typ, dep, loc, el, sk, sal, dl, ins, hs)
                                appViewModel.showBanner("Opportunity published.")
                            },
                            onUpdateApplication = { a, st, sc, dt, fb ->
                                ecosystemVm.updateApplicationRecruiter(a, st, sc, dt, fb)
                                appViewModel.showBanner("Applicant updated.")
                            },
                            onUpsertCompany = { comp ->
                                ecosystemVm.upsertCompany(comp)
                                appViewModel.showBanner("Company profile updated.")
                            },
                            onRespondCustomerVoice = { cv, resp ->
                                ecosystemVm.respondCustomerVoice(cv, resp)
                                appViewModel.showBanner("Customer response posted.")
                            },
                            onLogout = appViewModel::logout
                        )
                    }
                    UserRole.ADMIN.name -> {
                        val users by ecosystemVm.allUsers.collectAsStateWithLifecycle()
                        val companies by ecosystemVm.companies.collectAsStateWithLifecycle()
                        val opps by ecosystemVm.opportunities.collectAsStateWithLifecycle()
                        val videos by ecosystemVm.videos.collectAsStateWithLifecycle()
                        val reports by ecosystemVm.reports.collectAsStateWithLifecycle()
                        AdminDashboardScreen(
                            adminUser = user,
                            allUsers = users,
                            companies = companies,
                            opportunities = opps,
                            videos = videos,
                            reports = reports,
                            onVerifyUser = { u, st ->
                                ecosystemVm.adminVerifyUser(u, st)
                                appViewModel.showBanner("${u.name} marked $st")
                            },
                            onToggleSuspendUser = { u ->
                                ecosystemVm.adminToggleSuspendUser(u)
                                appViewModel.showBanner("Updated suspension for ${u.name}")
                            },
                            onVerifyCompany = { c, v -> ecosystemVm.adminVerifyCompany(c, v) },
                            onModerateOpportunity = { o, v, st -> ecosystemVm.adminModerateOpportunity(o, v, st) },
                            onToggleVideoApproval = { v -> ecosystemVm.adminToggleVideoApproval(v) },
                            onResolveReport = { r, rm ->
                                ecosystemVm.adminResolveReport(r, rm)
                                appViewModel.showBanner("Report resolved.")
                            },
                            onLogout = appViewModel::logout
                        )
                    }
                    else -> {
                        StudentPortalShell(
                            user = user,
                            appViewModel = appViewModel,
                            learningVm = learningVm,
                            ecosystemVm = ecosystemVm
                        )
                    }
                }
            }
        }
    }
}

private data class NavDestinationItem(val route: String, val label: String, val icon: ImageVector)

@Composable
private fun StudentPortalShell(
    user: UserEntity,
    appViewModel: AppViewModel,
    learningVm: LearningViewModel,
    ecosystemVm: EcosystemViewModel
) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route ?: StudentRoutes.HOME

    val allModules = listOf(
        NavDestinationItem(StudentRoutes.HOME, "Home", Icons.Filled.Home),
        NavDestinationItem(StudentRoutes.COMPASS, "Compass", Icons.Filled.Explore),
        NavDestinationItem(StudentRoutes.EXAM_HUB, "Exam Hub", Icons.Filled.Quiz),
        NavDestinationItem(StudentRoutes.SKILL_FORGE, "Skill Forge", Icons.Filled.Code),
        NavDestinationItem(StudentRoutes.INTERVIEW_ARENA, "Interviews", Icons.Filled.RecordVoiceOver),
        NavDestinationItem(StudentRoutes.COMPANY_JOBS, "Jobs & Co.", Icons.Filled.BusinessCenter),
        NavDestinationItem(StudentRoutes.NETWORK, "Network", Icons.Filled.Groups),
        NavDestinationItem(StudentRoutes.MY_PROGRESS, "Progress", Icons.Filled.Insights)
    )
    val primaryBottomTabs = listOf(
        allModules[0],
        allModules[1],
        allModules[2],
        allModules[5],
        allModules[7]
    )

    fun navigateTo(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    val savedItems by appViewModel.savedItems.collectAsStateWithLifecycle()
    val notifications by appViewModel.notifications.collectAsStateWithLifecycle()
    val achievements by appViewModel.achievements.collectAsStateWithLifecycle()

    val roadmap by learningVm.activeRoadmap.collectAsStateWithLifecycle()
    val milestones by learningVm.milestones.collectAsStateWithLifecycle()
    val examAttempts by learningVm.examAttempts.collectAsStateWithLifecycle()
    val doubts by learningVm.doubts.collectAsStateWithLifecycle()
    val doubtLoading by learningVm.doubtLoading.collectAsStateWithLifecycle()
    val courseProgress by learningVm.courseProgress.collectAsStateWithLifecycle()
    val commPractice by learningVm.commPractice.collectAsStateWithLifecycle()
    val commEvalState by learningVm.commEvalState.collectAsStateWithLifecycle()
    val interviewState by learningVm.interviewState.collectAsStateWithLifecycle()
    val interviewHistory by learningVm.interviewHistory.collectAsStateWithLifecycle()
    val studyTasks by learningVm.studyTasks.collectAsStateWithLifecycle()
    val compassState by learningVm.compassState.collectAsStateWithLifecycle()
    val roadmapGenerating by learningVm.roadmapGenerating.collectAsStateWithLifecycle()

    val opportunities by ecosystemVm.opportunities.collectAsStateWithLifecycle()
    val companies by ecosystemVm.companies.collectAsStateWithLifecycle()
    val reviews by ecosystemVm.reviews.collectAsStateWithLifecycle()
    val industryWatch by ecosystemVm.industryWatch.collectAsStateWithLifecycle()
    val customerVoice by ecosystemVm.customerVoice.collectAsStateWithLifecycle()
    val resumes by ecosystemVm.resumes.collectAsStateWithLifecycle()
    val applications by ecosystemVm.myApplications.collectAsStateWithLifecycle()
    val readinessState by ecosystemVm.readinessState.collectAsStateWithLifecycle()
    val videos by ecosystemVm.videos.collectAsStateWithLifecycle()
    val watchHistory by ecosystemVm.watchHistory.collectAsStateWithLifecycle()
    val mentors by ecosystemVm.verifiedMentors.collectAsStateWithLifecycle()
    val mentorships by ecosystemVm.myMentorships.collectAsStateWithLifecycle()
    val qnaList by ecosystemVm.qnaList.collectAsStateWithLifecycle()
    val liveSessions by ecosystemVm.liveSessions.collectAsStateWithLifecycle()
    val registrations by ecosystemVm.sessionRegistrations.collectAsStateWithLifecycle()
    val communityPosts by ecosystemVm.communityPosts.collectAsStateWithLifecycle()

    val completedCoursesCount = courseProgress.count { it.completed }
    val completedProjectsCount = courseProgress.count { it.projectSubmitted }
    val avgExamScore = if (examAttempts.isEmpty()) 0 else examAttempts.map { it.scorePercent }.average().toInt()

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useRail = maxWidth >= 700.dp
        Row(modifier = Modifier.fillMaxSize()) {
            if (useRail) {
                NavigationRail(containerColor = MidnightIndigo.copy(alpha = 0.94f)) {
                    allModules.forEach { item ->
                        NavigationRailItem(
                            selected = currentRoute == item.route,
                            onClick = { navigateTo(item.route) },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                if (!useRail) {
                    // Compact quick-switch strip exposing all 8 ecosystem modules cleanly
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        allModules.forEach { item ->
                            val selected = currentRoute == item.route
                            Surface(
                                modifier = Modifier
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clickable { navigateTo(item.route) }
                                    .testTag("top_nav_${item.route}"),
                                shape = RoundedCornerShape(50),
                                color = if (selected) EtherealMist else MidnightIndigo.copy(alpha = 0.85f),
                                border = BorderStroke(1.dp, if (selected) LuminousPearl else SmokyViolet)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = item.icon,
                                        contentDescription = item.label,
                                        tint = if (selected) MidnightIndigo else MoonlitSilver,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = item.label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (selected) MidnightIndigo else MoonlitSilver
                                    )
                                }
                            }
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    NavHost(
                        navController = navController,
                        startDestination = StudentRoutes.HOME
                    ) {
                        composable(StudentRoutes.HOME) {
                            HomeScreen(
                                user = user,
                                roadmap = roadmap,
                                milestones = milestones,
                                studyTasks = studyTasks,
                                notifications = notifications,
                                achievements = achievements,
                                onNavigate = ::navigateTo,
                                onToggleTask = learningVm::toggleStudyTask,
                                onUpdateProfile = appViewModel::updateProfile,
                                onMarkNotificationsRead = { appViewModel.markNotificationsRead(user.id) },
                                onLogout = appViewModel::logout
                            )
                        }
                        composable(StudentRoutes.COMPASS) {
                            CareerCompassScreen(
                                user = user,
                                compassState = compassState,
                                activeRoadmap = roadmap,
                                milestones = milestones,
                                roadmapGenerating = roadmapGenerating,
                                savedItems = savedItems,
                                onUpdateProfileAndAnalyze = { updated ->
                                    appViewModel.updateProfile(updated)
                                    learningVm.analyzeCareers(updated)
                                },
                                onForgeRoadmap = learningVm::forgeDestinyRoadmap,
                                onToggleMilestone = learningVm::toggleMilestone,
                                onToggleReminder = learningVm::toggleMilestoneReminder,
                                onToggleSaveCareer = { career ->
                                    appViewModel.toggleSaved(
                                        user.id,
                                        "CAREER",
                                        "career_${career.title}",
                                        career.title,
                                        career.industry
                                    )
                                },
                                onNavigateToModule = ::navigateTo
                            )
                        }
                        composable(StudentRoutes.EXAM_HUB) {
                            ExamHubScreen(
                                attempts = examAttempts,
                                doubts = doubts,
                                doubtLoading = doubtLoading,
                                studyTasks = studyTasks,
                                onSubmitExam = learningVm::submitExam,
                                onSolveDoubt = learningVm::solveDoubt,
                                onAskFollowUp = learningVm::askDoubtFollowUp,
                                onAddStudyTask = learningVm::addStudyTask,
                                onToggleStudyTask = learningVm::toggleStudyTask,
                                onDeleteStudyTask = learningVm::deleteStudyTask
                            )
                        }
                        composable(StudentRoutes.SKILL_FORGE) {
                            SkillForgeScreen(
                                courseProgress = courseProgress,
                                commPractice = commPractice,
                                commEvalState = commEvalState,
                                onToggleLesson = learningVm::toggleLessonComplete,
                                onSubmitProjectQuiz = learningVm::submitCourseProjectAndQuiz,
                                onEvaluateCommunication = learningVm::evaluateCommunication
                            )
                        }
                        composable(StudentRoutes.INTERVIEW_ARENA) {
                            InterviewArenaScreen(
                                user = user,
                                activeState = interviewState,
                                history = interviewHistory,
                                verifiedMentors = mentors,
                                onStartInterview = learningVm::startInterview,
                                onSubmitAnswer = learningVm::submitInterviewAnswer,
                                onFinishInterview = {
                                    learningVm.finishAndSaveInterview()
                                    appViewModel.showBanner("Interview saved to history.")
                                },
                                onBookMentorMock = { mentor, topic, slot ->
                                    ecosystemVm.requestMentorship(user, mentor, topic, slot, true)
                                    appViewModel.showBanner("Mock interview request sent to ${mentor.name}.")
                                }
                            )
                        }
                        composable(StudentRoutes.COMPANY_JOBS) {
                            CompanyJobsScreen(
                                user = user,
                                opportunities = opportunities,
                                companies = companies,
                                reviews = reviews,
                                industryWatch = industryWatch,
                                customerVoice = customerVoice,
                                resumes = resumes,
                                applications = applications,
                                savedItems = savedItems,
                                readinessState = readinessState,
                                completedCoursesCount = completedCoursesCount,
                                completedProjectsCount = completedProjectsCount,
                                avgExamScore = avgExamScore,
                                onApplyOpportunity = { opp, resTitle, note ->
                                    ecosystemVm.applyToOpportunity(user, opp, resTitle, note)
                                    appViewModel.showBanner("Application sent to ${opp.companyName}.")
                                },
                                onToggleSaveOpportunity = { opp ->
                                    appViewModel.toggleSaved(user.id, "OPPORTUNITY", opp.id, opp.title, opp.companyName)
                                },
                                onAnalyzeReadiness = { title, jd ->
                                    ecosystemVm.analyzeReadiness(user, completedCoursesCount, completedProjectsCount, avgExamScore, title, jd)
                                },
                                onSubmitReview = { comp, auth, role, c, w, m, l, hyb, intExp, txt ->
                                    ecosystemVm.submitReview(comp, auth, role, "UNVERIFIED", c, w, m, l, hyb, intExp, txt)
                                    appViewModel.showBanner("Workplace review published.")
                                },
                                onReportReview = { rev, reason ->
                                    ecosystemVm.reportContent("REVIEW", rev.id, "${rev.companyName} review", reason)
                                    appViewModel.showBanner("Review dispute submitted for moderation.")
                                }
                            )
                        }
                        composable(StudentRoutes.NETWORK) {
                            ProfessionalNetworkScreen(
                                user = user,
                                videos = videos,
                                watchHistory = watchHistory,
                                savedItems = savedItems,
                                mentors = mentors,
                                mentorships = mentorships,
                                qnaList = qnaList,
                                liveSessions = liveSessions,
                                registrations = registrations,
                                communityPosts = communityPosts,
                                onToggleSaveVideo = { vid ->
                                    appViewModel.toggleSaved(user.id, "VIDEO", vid.id, vid.title, vid.creatorName)
                                },
                                onSaveVideoProgress = ecosystemVm::saveWatchProgress,
                                onRequestMentorship = { mentor, topic, slot, isMock ->
                                    ecosystemVm.requestMentorship(user, mentor, topic, slot, isMock)
                                    appViewModel.showBanner("Request sent to ${mentor.name}.")
                                },
                                onAskQuestion = { cat, q ->
                                    ecosystemVm.askProfessional(user, cat, q)
                                    appViewModel.showBanner("Question posted to Ask a Professional.")
                                },
                                onToggleSessionRegister = ecosystemVm::toggleSessionRegistration,
                                onCreateCommunityPost = { cat, t, body ->
                                    ecosystemVm.createCommunityPost(user, cat, t, body)
                                    appViewModel.showBanner("Posted to Peer Community.")
                                },
                                onUpvotePost = ecosystemVm::upvotePost,
                                onReportContent = { type, id, title, reason ->
                                    ecosystemVm.reportContent(type, id, title, reason)
                                    appViewModel.showBanner("Report submitted to moderators.")
                                }
                            )
                        }
                        composable(StudentRoutes.MY_PROGRESS) {
                            MyProgressScreen(
                                user = user,
                                milestones = milestones,
                                examAttempts = examAttempts,
                                courseProgress = courseProgress,
                                interviews = interviewHistory,
                                applications = applications,
                                savedItems = savedItems,
                                resumes = resumes,
                                achievements = achievements,
                                onSaveResume = { res ->
                                    ecosystemVm.saveResume(res)
                                    appViewModel.showBanner("Resume version saved.")
                                },
                                onDeleteResume = ecosystemVm::deleteResume,
                                onShowMessage = appViewModel::showBanner
                            )
                        }
                    }
                }

                if (!useRail) {
                    NavigationBar(
                        containerColor = MidnightIndigo.copy(alpha = 0.96f),
                        contentColor = MoonlitSilver
                    ) {
                        primaryBottomTabs.forEach { item ->
                            val selected = currentRoute == item.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateTo(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label, maxLines = 1) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MidnightIndigo,
                                    selectedTextColor = EtherealMist,
                                    indicatorColor = EtherealMist,
                                    unselectedIconColor = MoonlitSilver,
                                    unselectedTextColor = MoonlitSilver.copy(alpha = 0.75f)
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}
