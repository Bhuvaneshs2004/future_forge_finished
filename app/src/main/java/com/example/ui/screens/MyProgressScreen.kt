package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun MyProgressScreen(
    user: UserEntity,
    milestones: List<RoadmapMilestoneEntity>,
    examAttempts: List<ExamAttemptEntity>,
    courseProgress: List<CourseProgressEntity>,
    interviews: List<InterviewSessionEntity>,
    applications: List<ApplicationEntity>,
    savedItems: List<SavedItemEntity>,
    resumes: List<ResumeEntity>,
    achievements: List<AchievementEntity>,
    onSaveResume: (ResumeEntity) -> Unit,
    onDeleteResume: (String) -> Unit,
    onShowMessage: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Analytics & Applications, 1=Resume Studio, 2=Achievements
    val context = LocalContext.current
    var pendingExportText by remember { mutableStateOf<String?>(null) }

    val exportDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val content = pendingExportText
        if (uri != null && content != null) {
            runCatching {
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(content.toByteArray())
                }
                onShowMessage("Resume exported successfully.")
            }.onFailure {
                onShowMessage("Export failed: ${it.localizedMessage}")
            }
        }
        pendingExportText = null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "My Progress & Studio",
                subtitle = "Learning analytics, Resume & Portfolio Studio, applications & rewards"
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgePillChip("Analytics", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                ForgePillChip("Resume Studio", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                ForgePillChip("Achievements", selectedTab == 2, { selectedTab = 2 }, Modifier.weight(1f))
            }
        }

        when (selectedTab) {
            0 -> {
                val roadmapFrac = if (milestones.isEmpty()) 0f else milestones.count { it.completed }.toFloat() / milestones.size
                val avgExamFrac = if (examAttempts.isEmpty()) 0f else (examAttempts.map { it.scorePercent }.average().toFloat() / 100f)
                val coursesFrac = (courseProgress.count { it.completed }.toFloat() / 4f).coerceIn(0f, 1f)
                val interviewFrac = (interviews.size.toFloat() / 3f).coerceIn(0f, 1f)

                item {
                    ForgeCard {
                        Text("Unified Growth Telemetry", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(10.dp))
                        ForgeBarChart(
                            bars = listOf(
                                "Roadmap" to roadmapFrac,
                                "Exams" to avgExamFrac,
                                "Courses" to coursesFrac,
                                "Interview" to interviewFrac
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Exams: ${examAttempts.size} • Courses Completed: ${courseProgress.count { it.completed }} • Interviews: ${interviews.size} • Applications: ${applications.size}",
                            style = MaterialTheme.typography.bodySmall,
                            color = EtherealMist
                        )
                    }
                }

                item {
                    Text("Tracked Job & Internship Applications", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                if (applications.isEmpty()) {
                    item {
                        ForgeCard {
                            Text("No applications submitted yet. Explore verified openings in Company & Jobs.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    items(applications, key = { it.id }) { app ->
                        ForgeCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(app.opportunityTitle, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                    Text("${app.companyName} • Resume: ${app.resumeVersionTitle}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                                }
                                Text(app.status.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = VerifiedEmerald)
                            }
                            if (app.assessmentScore != null) {
                                Text("Assessment Score: ${app.assessmentScore}%", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            }
                            if (app.interviewDate.isNotBlank()) {
                                Text("Interview Scheduled: ${app.interviewDate}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                            }
                            if (app.recruiterFeedback.isNotBlank()) {
                                Text("Recruiter Feedback: ${app.recruiterFeedback}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            }
                        }
                    }
                }

                item {
                    Text("Saved Careers, Opportunities & Videos (${savedItems.size})", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                }
                items(savedItems, key = { it.id }) { saved ->
                    ForgeCard {
                        Text("${saved.itemType} • ${saved.title}", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Text(saved.subtitle, style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    }
                }
            }

            1 -> {
                item {
                    ResumeStudioSection(
                        user = user,
                        resumes = resumes,
                        onSaveResume = onSaveResume,
                        onDeleteResume = onDeleteResume,
                        onExportResume = { fileName, text ->
                            pendingExportText = text
                            exportDocumentLauncher.launch(fileName)
                        }
                    )
                }
            }

            2 -> {
                item {
                    ForgeCard {
                        Text("Total Forge Points: ${achievements.sumOf { it.points }}", style = MaterialTheme.typography.headlineMedium, color = GlacierBlueGlow)
                        Text("Badges unlock automatically only when you genuinely complete milestones, high-score exams, courses, interviews, or applications.", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    }
                }
                if (achievements.isEmpty()) {
                    item {
                        ForgeCard {
                            Text("Complete your first Destiny Roadmap milestone, score 70%+ on an Exam, or build a Resume to unlock your first badge.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(achievements, key = { it.id }) { ach ->
                        ForgeCard(borderColor = VerifiedEmerald.copy(alpha = 0.55f)) {
                            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Icon(Icons.Filled.EmojiEvents, contentDescription = "Achievement Badge", tint = CautionAmber, modifier = Modifier.size(30.dp))
                                Column(Modifier.weight(1f)) {
                                    Text("${ach.title} (+${ach.points} pts)", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                                    Text(ach.description, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResumeStudioSection(
    user: UserEntity,
    resumes: List<ResumeEntity>,
    onSaveResume: (ResumeEntity) -> Unit,
    onDeleteResume: (String) -> Unit,
    onExportResume: (fileName: String, content: String) -> Unit
) {
    var editingId by remember { mutableStateOf(resumes.firstOrNull()?.id ?: "") }
    val active = resumes.firstOrNull { it.id == editingId }

    var versionTitle by remember(active?.id) { mutableStateOf(active?.versionTitle ?: "Tailored Career Resume") }
    var fullName by remember(active?.id) { mutableStateOf(active?.fullName ?: user.name) }
    var email by remember(active?.id) { mutableStateOf(active?.email ?: user.email) }
    var phone by remember(active?.id) { mutableStateOf(active?.phone ?: "") }
    var summary by remember(active?.id) { mutableStateOf(active?.summary ?: user.careerAmbitions) }
    var education by remember(active?.id) { mutableStateOf(active?.education ?: user.educationLevel) }
    var experience by remember(active?.id) { mutableStateOf(active?.experience ?: "") }
    var skills by remember(active?.id) { mutableStateOf(active?.skills ?: user.skills) }
    var projects by remember(active?.id) { mutableStateOf(active?.projects ?: "") }
    var portfolio by remember(active?.id) { mutableStateOf(active?.portfolioLinks ?: "") }
    var coverLetter by remember(active?.id) { mutableStateOf(active?.coverLetter ?: "") }
    var showPreview by remember { mutableStateOf(false) }

    val formattedResumeText = buildString {
        appendLine("========================================")
        appendLine(fullName.uppercase())
        appendLine("$email | $phone | $portfolio")
        appendLine("========================================")
        appendLine("\nPROFESSIONAL SUMMARY\n$summary")
        appendLine("\nEDUCATION\n$education")
        appendLine("\nSKILLS\n$skills")
        appendLine("\nEXPERIENCE\n$experience")
        appendLine("\nPROJECTS & PORTFOLIO\n$projects")
        if (coverLetter.isNotBlank()) appendLine("\nCOVER LETTER\n$coverLetter")
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (resumes.isNotEmpty()) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                resumes.forEach { r ->
                    ForgePillChip(r.versionTitle, editingId == r.id, { editingId = r.id })
                }
                ForgePillChip("+ New", editingId.isBlank(), { editingId = "" })
            }
        }

        ForgeCard {
            Text("Resume & Portfolio Editor", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = versionTitle, onValueChange = { versionTitle = it }, label = { Text("Resume Version Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = fullName, onValueChange = { fullName = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = summary, onValueChange = { summary = it }, label = { Text("Professional Summary") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Core Skills") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = experience, onValueChange = { experience = it }, label = { Text("Experience & Internships") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = projects, onValueChange = { projects = it }, label = { Text("Projects Showcase") }, minLines = 2, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = portfolio, onValueChange = { portfolio = it }, label = { Text("Portfolio / GitHub / Behance Link") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(6.dp))
            OutlinedTextField(value = coverLetter, onValueChange = { coverLetter = it }, label = { Text("Tailored Cover Letter") }, minLines = 3, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        coverLetter = "Dear Hiring Committee,\n\nI am writing to express strong interest in joining your team. With my foundation in $education and applied skills in $skills, I have built measurable projects including ${projects.lines().firstOrNull().orEmpty()}.\n\nSincerely,\n$fullName"
                    },
                    modifier = Modifier.weight(1f)
                ) { Text("Draft Cover Letter") }
                OutlinedButton(onClick = { showPreview = !showPreview }, modifier = Modifier.weight(1f)) {
                    Text(if (showPreview) "Hide Preview" else "Preview")
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val id = editingId.ifBlank { "res_${UUID.randomUUID().toString().take(8)}" }
                        editingId = id
                        onSaveResume(
                            ResumeEntity(
                                id = id,
                                userId = user.id,
                                versionTitle = versionTitle.ifBlank { "Resume v1" },
                                fullName = fullName,
                                email = email,
                                phone = phone,
                                summary = summary,
                                education = education,
                                experience = experience,
                                skills = skills,
                                projects = projects,
                                portfolioLinks = portfolio,
                                coverLetter = coverLetter
                            )
                        )
                    },
                    modifier = Modifier.weight(1f).testTag("save_resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                ) { Text("Save Version") }
                Button(
                    onClick = {
                        val safeName = versionTitle.replace(Regex("[^a-zA-Z0-9_]"), "_") + ".txt"
                        onExportResume(safeName, formattedResumeText)
                    },
                    modifier = Modifier.weight(1f).testTag("export_resume_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                ) { Text("Download .TXT") }
            }
            if (active != null && resumes.size > 1) {
                Spacer(Modifier.height(6.dp))
                TextButton(onClick = { onDeleteResume(active.id); editingId = "" }) {
                    Text("Delete This Version", color = AlertRose)
                }
            }
        }

        if (showPreview) {
            ForgeCard(containerColor = MidnightIndigo, borderColor = GlacierBlueGlow) {
                Text("Formatted Document Preview", style = MaterialTheme.typography.titleSmall, color = GlacierBlueGlow)
                Spacer(Modifier.height(6.dp))
                Text(formattedResumeText, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
            }
        }
    }
}
