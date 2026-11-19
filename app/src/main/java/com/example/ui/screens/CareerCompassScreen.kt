package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.ai.CareerCompassResult
import com.example.data.ai.CareerSuggestion
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiCallState

@Composable
fun CareerCompassScreen(
    user: UserEntity,
    compassState: AiCallState<CareerCompassResult>,
    activeRoadmap: RoadmapEntity?,
    milestones: List<RoadmapMilestoneEntity>,
    roadmapGenerating: Boolean,
    savedItems: List<SavedItemEntity>,
    onUpdateProfileAndAnalyze: (UserEntity) -> Unit,
    onForgeRoadmap: (UserEntity, String) -> Unit,
    onToggleMilestone: (RoadmapMilestoneEntity) -> Unit,
    onToggleReminder: (RoadmapMilestoneEntity) -> Unit,
    onToggleSaveCareer: (CareerSuggestion) -> Unit,
    onNavigateToModule: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Compass, 1 = Destiny Roadmap
    var compareSelection by remember { mutableStateOf<List<CareerSuggestion>>(emptyList()) }

    var education by remember(user) { mutableStateOf(user.educationLevel) }
    var interests by remember(user) { mutableStateOf(user.interests) }
    var strengths by remember(user) { mutableStateOf(user.strengths) }
    var skills by remember(user) { mutableStateOf(user.skills) }
    var ambitions by remember(user) { mutableStateOf(user.careerAmbitions) }
    var customCareerInput by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "Career Compass & Destiny Roadmap",
                subtitle = "Discover multiple private-sector pathways & forge a trackable plan"
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ForgePillChip("AI Career Compass", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                ForgePillChip("Destiny Roadmap", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
            }
        }

        if (selectedTab == 0) {
            item {
                ForgeCard {
                    Text("Learner Discovery Profile", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    Text("Update any field to refine AI career guidance — recommendations adapt without locking you into one path.", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = education, onValueChange = { education = it }, label = { Text("Education & Qualifications") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = interests, onValueChange = { interests = it }, label = { Text("Interests") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = strengths, onValueChange = { strengths = it }, label = { Text("Core Strengths") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = skills, onValueChange = { skills = it }, label = { Text("Existing Skills") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = ambitions, onValueChange = { ambitions = it }, label = { Text("Career Ambitions") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onUpdateProfileAndAnalyze(
                                user.copy(
                                    educationLevel = education,
                                    interests = interests,
                                    strengths = strengths,
                                    skills = skills,
                                    careerAmbitions = ambitions
                                )
                            )
                        },
                        modifier = Modifier.fillMaxWidth().testTag("analyze_career_compass_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                    ) {
                        Icon(Icons.Filled.Explore, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Analyze Suitable Private-Sector Careers")
                    }
                }
            }

            when (compassState) {
                is AiCallState.Loading -> item {
                    ForgeCard {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), color = EtherealMist)
                            Text("Analyzing qualifications, strengths, and industry paths...", color = MoonlitSilver)
                        }
                    }
                }
                is AiCallState.Error -> item {
                    ForgeCard(borderColor = AlertRose) {
                        Text(compassState.message, color = AlertRose)
                    }
                }
                is AiCallState.Success -> {
                    item { AiPlaceholderNoticeBanner() }
                    item {
                        ForgeCard(containerColor = MidnightIndigo) {
                            Text("Cross-Pathway Comparison", style = MaterialTheme.typography.titleSmall, color = GlacierBlueGlow)
                            Spacer(Modifier.height(4.dp))
                            Text(compassState.data.comparisonSummary, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        }
                    }
                    if (compareSelection.size == 2) {
                        item {
                            val a = compareSelection[0]
                            val b = compareSelection[1]
                            ForgeCard(borderColor = GlacierBlueGlow) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Side-by-Side Comparison", style = MaterialTheme.typography.titleMedium, color = GlacierBlueGlow)
                                    TextButton(onClick = { compareSelection = emptyList() }) { Text("Clear") }
                                }
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Column(Modifier.weight(1f)) {
                                        Text(a.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                        Text("Match: ${a.matchPercent}%", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                                        Text("Skills: ${a.skillsToDevelop.take(3).joinToString()}", style = MaterialTheme.typography.bodySmall)
                                        Text("Path: ${a.progressionPath}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                                    }
                                    Column(Modifier.weight(1f)) {
                                        Text(b.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                        Text("Match: ${b.matchPercent}%", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                                        Text("Skills: ${b.skillsToDevelop.take(3).joinToString()}", style = MaterialTheme.typography.bodySmall)
                                        Text("Path: ${b.progressionPath}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                                    }
                                }
                            }
                        }
                    }
                    items(compassState.data.suggestions) { career ->
                        val isSaved = savedItems.any { it.itemId == "career_${career.title}" }
                        val inCompare = compareSelection.any { it.title == career.title }
                        ForgeCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                                Column(Modifier.weight(1f)) {
                                    Text("${career.matchPercent}% Alignment • ${career.industry}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                                    Text(career.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                                }
                                IconButton(onClick = { onToggleSaveCareer(career) }) {
                                    Icon(
                                        imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                                        contentDescription = "Save Career",
                                        tint = if (isSaved) EtherealMist else MoonlitSilver
                                    )
                                }
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("Why It Fits: ${career.whyRecommended}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            Spacer(Modifier.height(4.dp))
                            Text("Qualifications: ${career.requiredQualifications}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                            Spacer(Modifier.height(4.dp))
                            Text("Skills to Build: ${career.skillsToDevelop.joinToString(" • ")}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                            Text("Certifications: ${career.relevantCertifications.joinToString()}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            Text("Progression: ${career.progressionPath}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        compareSelection = if (inCompare) compareSelection.filterNot { it.title == career.title }
                                        else (compareSelection + career).takeLast(2)
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(if (inCompare) "Selected" else "Compare")
                                }
                                Button(
                                    onClick = {
                                        onForgeRoadmap(user, career.title)
                                        selectedTab = 1
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                                ) {
                                    Text("Forge Roadmap")
                                }
                            }
                        }
                    }
                }
                AiCallState.Idle -> item {
                    ForgeCard {
                        Text("Tap 'Analyze Suitable Private-Sector Careers' above to generate multi-career insights tailored to your profile.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            // DESTINY ROADMAP TAB
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_destiny_compass),
                        contentDescription = "Destiny Roadmap Compass",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Text("SIGNATURE FEATURE", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(
                            text = activeRoadmap?.targetCareer ?: "No Active Roadmap Yet",
                            style = MaterialTheme.typography.headlineLarge,
                            color = LuminousPearl
                        )
                    }
                }
            }

            item {
                ForgeCard {
                    Text("Forge or Switch Target Career", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = customCareerInput,
                            onValueChange = { customCareerInput = it },
                            label = { Text("Target Role (e.g., Product Manager, Data Analyst)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                onForgeRoadmap(user, customCareerInput.ifBlank { user.careerAmbitions.ifBlank { "Software & Mobile Systems Engineer" } })
                            },
                            enabled = !roadmapGenerating,
                            modifier = Modifier.testTag("generate_roadmap_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) {
                            Text(if (roadmapGenerating) "..." else "Forge")
                        }
                    }
                    if (activeRoadmap != null) {
                        Spacer(Modifier.height(8.dp))
                        AiPlaceholderNoticeBanner()
                        Spacer(Modifier.height(8.dp))
                        val done = milestones.count { it.completed }
                        val pct = if (milestones.isEmpty()) 0 else (done * 100) / milestones.size
                        Text("Overall Completion: $pct% ($done / ${milestones.size} stages)", style = MaterialTheme.typography.labelLarge, color = EtherealMist)
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { if (milestones.isEmpty()) 0f else done.toFloat() / milestones.size },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = EtherealMist,
                            trackColor = SmokyViolet
                        )
                    }
                }
            }

            items(milestones, key = { it.id }) { ms ->
                ForgeCard(
                    borderColor = if (ms.completed) VerifiedEmerald.copy(alpha = 0.55f) else DustyLavender.copy(alpha = 0.3f)
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Checkbox(
                            checked = ms.completed,
                            onCheckedChange = { onToggleMilestone(ms) },
                            modifier = Modifier.testTag("milestone_check_${ms.orderIndex}")
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${ms.stage} • ${ms.targetWeek}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                            Text(ms.title, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                            Text(ms.description, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            Spacer(Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        val route = when (ms.linkedModule) {
                                            "ExamHub" -> StudentRoutes.EXAM_HUB
                                            "SkillForge" -> StudentRoutes.SKILL_FORGE
                                            "InterviewArena" -> StudentRoutes.INTERVIEW_ARENA
                                            "ResumeStudio" -> StudentRoutes.MY_PROGRESS
                                            "ProfessionalConnect" -> StudentRoutes.NETWORK
                                            else -> StudentRoutes.COMPANY_JOBS
                                        }
                                        onNavigateToModule(route)
                                    },
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Open ${ms.linkedModule}", style = MaterialTheme.typography.labelSmall)
                                }
                                IconButton(onClick = { onToggleReminder(ms) }) {
                                    Icon(
                                        imageVector = if (ms.reminderEnabled) Icons.Filled.NotificationsActive else Icons.Filled.NotificationsNone,
                                        contentDescription = "Toggle Milestone Reminder",
                                        tint = if (ms.reminderEnabled) GlacierBlueGlow else MoonlitSilver
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
