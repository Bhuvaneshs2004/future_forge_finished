package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.JobReadinessResult
import com.example.data.catalog.DomainCatalog
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiCallState

@Composable
fun CompanyJobsScreen(
    user: UserEntity,
    opportunities: List<JobOpportunityEntity>,
    companies: List<CompanyProfileEntity>,
    reviews: List<EmployeeReviewEntity>,
    industryWatch: List<IndustryWatchEntity>,
    customerVoice: List<CustomerVoiceEntity>,
    resumes: List<ResumeEntity>,
    applications: List<ApplicationEntity>,
    savedItems: List<SavedItemEntity>,
    readinessState: AiCallState<JobReadinessResult>,
    completedCoursesCount: Int,
    completedProjectsCount: Int,
    avgExamScore: Int,
    onApplyOpportunity: (JobOpportunityEntity, String, String) -> Unit,
    onToggleSaveOpportunity: (JobOpportunityEntity) -> Unit,
    onAnalyzeReadiness: (jobTitle: String, jobDescription: String) -> Unit,
    onSubmitReview: (CompanyProfileEntity, String, String, Int, Int, Int, Int, String, String, String) -> Unit,
    onReportReview: (EmployeeReviewEntity, String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedOppType by remember { mutableStateOf("ALL") }
    var applyTarget by remember { mutableStateOf<JobOpportunityEntity?>(null) }
    var reportReviewTarget by remember { mutableStateOf<EmployeeReviewEntity?>(null) }
    var showAddReviewFor by remember { mutableStateOf<CompanyProfileEntity?>(null) }

    // Job Readiness local fields
    var readinessTitle by remember { mutableStateOf("") }
    var readinessJd by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "Company & Jobs Hub",
                subtitle = "Verified vacancies, company intel, salaries, reviews & readiness"
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForgePillChip("Vacancies", selectedTab == 0, { selectedTab = 0 })
                ForgePillChip("Companies", selectedTab == 1, { selectedTab = 1 })
                ForgePillChip("Salaries", selectedTab == 2, { selectedTab = 2 })
                ForgePillChip("Reviews & Watch", selectedTab == 3, { selectedTab = 3 })
                ForgePillChip("Job Readiness", selectedTab == 4, { selectedTab = 4 })
            }
        }

        when (selectedTab) {
            0 -> {
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search roles, companies, skills, or locations") },
                        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("ALL", "INTERNSHIP", "FRESHER", "SCHOLARSHIP", "COMPETITION", "APPRENTICESHIP").forEach { type ->
                            ForgePillChip(type, selectedOppType == type, { selectedOppType = type })
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ForgeCard(containerColor = MidnightIndigo) {
                        val openCount = opportunities.count { it.hiringStatus != "CLOSED" }
                        val driveCount = opportunities.count { it.hiringStatus == "CAMPUS_DRIVE" }
                        Text("Hiring Seat Tracker • $openCount Active Listings ($driveCount Campus Drives)", style = MaterialTheme.typography.labelLarge, color = GlacierBlueGlow)
                        Text("Every listing discloses its source (Official Employer, Trusted Feed, or Third-Party Report) and update date.", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    }
                }

                val filtered = opportunities.filter { opp ->
                    (selectedOppType == "ALL" || opp.type == selectedOppType) &&
                        (searchQuery.isBlank() ||
                            opp.title.contains(searchQuery, true) ||
                            opp.companyName.contains(searchQuery, true) ||
                            opp.location.contains(searchQuery, true) ||
                            opp.requiredSkills.contains(searchQuery, true))
                }

                items(filtered, key = { it.id }) { opp ->
                    val alreadyApplied = applications.any { it.opportunityId == opp.id }
                    val isSaved = savedItems.any { it.itemId == opp.id }
                    ForgeCard {
                        SourceVerificationBadge(opp.sourceType, opp.sourceName, opp.lastUpdated)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                            Column(Modifier.weight(1f)) {
                                Text("${opp.type} • ${opp.hiringStatus.replace("_", " ")}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                                Text(opp.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                                Text("${opp.companyName} • ${opp.location} • ${opp.department}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                            }
                            IconButton(onClick = { onToggleSaveOpportunity(opp) }) {
                                Icon(
                                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                    contentDescription = "Save Opportunity",
                                    tint = if (isSaved) EtherealMist else MoonlitSilver
                                )
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("Compensation: ${opp.salaryOrStipend} | Deadline: ${opp.deadline}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Text("Eligibility: ${opp.eligibility}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Skills: ${opp.requiredSkills}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    readinessTitle = opp.title
                                    readinessJd = "${opp.title} at ${opp.companyName}. Required skills: ${opp.requiredSkills}. Eligibility: ${opp.eligibility}"
                                    onAnalyzeReadiness(readinessTitle, readinessJd)
                                    selectedTab = 4
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Check Readiness")
                            }
                            Button(
                                onClick = { applyTarget = opp },
                                enabled = !alreadyApplied,
                                modifier = Modifier.weight(1f).testTag("apply_${opp.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                            ) {
                                Text(if (alreadyApplied) "Applied" else "Apply Now")
                            }
                        }
                    }
                }
            }

            1 -> {
                items(companies, key = { it.id }) { company ->
                    ForgeCard {
                        SourceVerificationBadge(company.sourceType, if (company.verified) "Verified Company Profile" else "Public Directory", company.lastUpdated)
                        Spacer(Modifier.height(8.dp))
                        Text(company.name, style = MaterialTheme.typography.headlineMedium, color = LuminousPearl)
                        Text("${company.industry} • ${company.locations}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                        Spacer(Modifier.height(6.dp))
                        Text("Products & Services: ${company.productsServices}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Qualifications: ${company.roleQualifications}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Tools & Tech: ${company.toolsTech}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text("Selection Stages: ${company.recruitmentStages}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Text("Fresher & Interns: ${company.fresherInfo} | ${company.internshipInfo}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        Spacer(Modifier.height(6.dp))
                        Text("Prep Checklist:", style = MaterialTheme.typography.labelMedium, color = GlacierBlueGlow)
                        company.prepChecklist.split("|").forEach { item ->
                            Text("• $item", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { showAddReviewFor = company }) {
                            Text("Share Workplace / Interview Review")
                        }
                    }
                }
            }

            2 -> {
                item {
                    ForgeCard(borderColor = CautionAmber.copy(alpha = 0.6f)) {
                        Text("Salary & Career Growth Explorer", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Text(
                            text = stringResource(id = R.string.salary_disclaimer),
                            style = MaterialTheme.typography.bodySmall,
                            color = CautionAmber
                        )
                    }
                }
                items(DomainCatalog.salaryBenchmarks) { sal ->
                    ForgeCard {
                        Text("${sal.industry} • ${sal.location}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(sal.role, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        Text("Internship Stipend: ${sal.internshipStipendMonthly}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text("Fresher (0-2 yrs): ${sal.fresherRangeAnnual}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Text("Mid-Level (3-6 yrs): ${sal.midLevelRangeAnnual}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Text("Senior (7+ yrs): ${sal.seniorRangeAnnual}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Spacer(Modifier.height(4.dp))
                        Text("Demand: ${sal.demandTrend}", style = MaterialTheme.typography.bodySmall, color = VerifiedEmerald)
                        Text("Progression: ${sal.growthPath}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Spacer(Modifier.height(4.dp))
                        Text("Source: ${sal.sourceName} (${sal.lastUpdated})", style = MaterialTheme.typography.labelSmall, color = DustyLavender)
                        Text("Assumptions: ${sal.assumptions}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver.copy(alpha = 0.8f))
                    }
                }
            }

            3 -> {
                item {
                    Text("Employee Reviews & Workplace Insights", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                items(reviews, key = { it.id }) { rev ->
                    val badgeColor = when (rev.verificationType) {
                        "VERIFIED_EMPLOYEE" -> VerifiedEmerald
                        "UNVERIFIED" -> CautionAmber
                        else -> DustyLavender
                    }
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(rev.companyName, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.5f))
                            ) {
                                Text(rev.verificationType.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = badgeColor, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                        }
                        Text("${rev.roleTitle} • ${rev.authorName}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text("Culture ${rev.workCultureRating}/5 • WLB ${rev.workLifeBalanceRating}/5 • Mgmt ${rev.managementRating}/5 • Learning ${rev.learningRating}/5", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text("Work Model: ${rev.hybridRemotePolicy}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Interview Exp: ${rev.interviewExperience}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        Spacer(Modifier.height(4.dp))
                        Text(rev.reviewText, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { reportReviewTarget = rev }) {
                                Text(if (rev.disputeStatus == "NONE") "Report / Dispute" else "Dispute: ${rev.disputeStatus}", color = AlertRose)
                            }
                        }
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Company Strategy & Industry Watch", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                items(industryWatch, key = { it.id }) { iw ->
                    ForgeCard {
                        Text("${iw.sector} • ${iw.category.replace("_", " ")} • ${iw.publishedDate}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(iw.headline, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Text(iw.summary, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("In-Demand Skills: ${iw.inDemandSkills}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text("Source: ${iw.sourceName}", style = MaterialTheme.typography.labelSmall, color = DustyLavender)
                    }
                }

                item {
                    Spacer(Modifier.height(4.dp))
                    Text("Customer Voice & Product Feedback", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                items(customerVoice, key = { it.id }) { cv ->
                    ForgeCard {
                        Text("${cv.companyName} • ${cv.category.replace("_", " ")} • Rating ${cv.rating}/5", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                        Text(cv.summary, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Text("Company Response: ${cv.companyResponse}", style = MaterialTheme.typography.bodySmall, color = VerifiedEmerald)
                        Text("Case Takeaway: ${cv.businessCaseTakeaway}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                        Text("Source: ${cv.sourceName} (${cv.publishedDate})", style = MaterialTheme.typography.labelSmall, color = DustyLavender)
                    }
                }
            }

            4 -> {
                item { AiPlaceholderNoticeBanner() }
                item {
                    ForgeCard {
                        Text("AI Job Readiness Analyzer", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Text("Compares job requirements with your profile, completed courses ($completedCoursesCount), projects ($completedProjectsCount), and exam avg ($avgExamScore%).", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = readinessTitle, onValueChange = { readinessTitle = it }, label = { Text("Target Role Title") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = readinessJd,
                            onValueChange = { readinessJd = it },
                            label = { Text("Paste Job Description or Requirements") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth().testTag("readiness_jd_input")
                        )
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = { onAnalyzeReadiness(readinessTitle, readinessJd) },
                            enabled = readinessJd.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("run_readiness_analyzer"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) {
                            Text("Run Skill-Gap & Readiness Analysis")
                        }
                    }
                }

                when (readinessState) {
                    is AiCallState.Loading -> item {
                        ForgeCard { CircularProgressIndicator(color = EtherealMist) }
                    }
                    is AiCallState.Error -> item {
                        ForgeCard(borderColor = AlertRose) { Text(readinessState.message, color = AlertRose) }
                    }
                    is AiCallState.Success -> {
                        val res = readinessState.data
                        item {
                            ForgeCard(borderColor = GlacierBlueGlow) {
                                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                    ForgeProgressRing(res.readinessScore / 100f, "${res.readinessScore}%")
                                    Column(Modifier.weight(1f)) {
                                        Text(res.targetRole, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                                        Text(res.confidenceLabel, style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                                        Text(res.explanation, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                                    }
                                }
                                Spacer(Modifier.height(10.dp))
                                Text("Skill-by-Skill Transparency", style = MaterialTheme.typography.titleSmall, color = EtherealMist)
                                res.skillBreakdown.forEach { sk ->
                                    val color = when (sk.status) {
                                        "MATCHED" -> VerifiedEmerald
                                        "PARTIAL" -> CautionAmber
                                        else -> AlertRose
                                    }
                                    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column(Modifier.weight(1f)) {
                                            Text(sk.skillName, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                                            Text(sk.evidenceSource, style = MaterialTheme.typography.labelSmall, color = MoonlitSilver.copy(alpha = 0.8f))
                                        }
                                        Text(sk.status, style = MaterialTheme.typography.labelSmall, color = color)
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text("Recommended Next Steps:", style = MaterialTheme.typography.titleSmall, color = GlacierBlueGlow)
                                res.recommendedActions.forEach { act ->
                                    Text("• $act", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                                }
                            }
                        }
                    }
                    AiCallState.Idle -> {}
                }
            }
        }
    }

    if (applyTarget != null) {
        val opp = applyTarget!!
        var chosenResume by remember { mutableStateOf(resumes.firstOrNull()?.versionTitle ?: "Profile Summary Resume") }
        var coverNote by remember { mutableStateOf("Excited to apply for ${opp.title} at ${opp.companyName} with skills in ${user.skills}.") }
        AlertDialog(
            onDismissRequest = { applyTarget = null },
            containerColor = MidnightIndigo,
            title = { Text("Apply: ${opp.title}", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Resume Version:", style = MaterialTheme.typography.labelMedium, color = EtherealMist)
                    resumes.forEach { r ->
                        ForgePillChip(r.versionTitle, chosenResume == r.versionTitle, { chosenResume = r.versionTitle })
                    }
                    OutlinedTextField(value = coverNote, onValueChange = { coverNote = it }, label = { Text("Cover Note") }, minLines = 3)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onApplyOpportunity(opp, chosenResume, coverNote)
                        applyTarget = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                ) { Text("Submit Application") }
            },
            dismissButton = {
                TextButton(onClick = { applyTarget = null }) { Text("Cancel", color = MoonlitSilver) }
            }
        )
    }

    if (reportReviewTarget != null) {
        val rev = reportReviewTarget!!
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportReviewTarget = null },
            containerColor = MidnightIndigo,
            title = { Text("Report or Dispute Review", color = LuminousPearl) },
            text = {
                OutlinedTextField(value = reason, onValueChange = { reason = it }, label = { Text("Reason (inaccurate, spam, harassment)") }, minLines = 2)
            },
            confirmButton = {
                Button(onClick = {
                    onReportReview(rev, reason)
                    reportReviewTarget = null
                }) { Text("Submit Report") }
            },
            dismissButton = {
                TextButton(onClick = { reportReviewTarget = null }) { Text("Cancel") }
            }
        )
    }

    if (showAddReviewFor != null) {
        val company = showAddReviewFor!!
        var roleTitle by remember { mutableStateOf("Software Intern") }
        var verType by remember { mutableStateOf("UNVERIFIED") }
        var hybrid by remember { mutableStateOf("Hybrid 3 days/week") }
        var interviewExp by remember { mutableStateOf("") }
        var reviewText by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAddReviewFor = null },
            containerColor = MidnightIndigo,
            title = { Text("Review ${company.name}", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ForgePillChip("Unverified", verType == "UNVERIFIED", { verType = "UNVERIFIED" })
                        ForgePillChip("Anonymous", verType == "ANONYMOUS", { verType = "ANONYMOUS" })
                    }
                    OutlinedTextField(value = roleTitle, onValueChange = { roleTitle = it }, label = { Text("Role") })
                    OutlinedTextField(value = hybrid, onValueChange = { hybrid = it }, label = { Text("Hybrid/Remote Policy") })
                    OutlinedTextField(value = interviewExp, onValueChange = { interviewExp = it }, label = { Text("Interview Experience") })
                    OutlinedTextField(value = reviewText, onValueChange = { reviewText = it }, label = { Text("Workplace Feedback") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onSubmitReview(company, user.name, roleTitle, 4, 4, 4, 4, hybrid, interviewExp, reviewText)
                    showAddReviewFor = null
                }) { Text("Publish Review") }
            },
            dismissButton = { TextButton(onClick = { showAddReviewFor = null }) { Text("Cancel") } }
        )
    }
}
