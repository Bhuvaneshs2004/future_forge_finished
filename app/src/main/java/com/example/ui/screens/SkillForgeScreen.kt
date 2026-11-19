package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.ai.CommunicationEvaluationResult
import com.example.data.catalog.DomainCatalog
import com.example.data.catalog.SkillPath
import com.example.data.local.CommunicationPracticeEntity
import com.example.data.local.CourseProgressEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AiCallState

@Composable
fun SkillForgeScreen(
    courseProgress: List<CourseProgressEntity>,
    commPractice: List<CommunicationPracticeEntity>,
    commEvalState: AiCallState<CommunicationEvaluationResult>,
    onToggleLesson: (courseId: String, lessonId: String) -> Unit,
    onSubmitProjectQuiz: (courseId: String, projectNotes: String, pickedQuizIdx: Int) -> Unit,
    onEvaluateCommunication: (mode: String, prompt: String, response: String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Skill Paths, 1 = Communication Lab
    var openedPath by remember { mutableStateOf<SkillPath?>(null) }
    var playingVideoTitle by remember { mutableStateOf<String?>(null) }
    var playingVideoUrl by remember { mutableStateOf<String?>(null) }

    if (openedPath != null) {
        BackHandler { openedPath = null }
        val path = openedPath!!
        val progress = courseProgress.firstOrNull { it.courseId == path.id }
        val completedLessonIds = progress?.completedLessons?.split(",")?.filter { it.isNotBlank() }?.toSet() ?: emptySet()
        var projectNotes by remember(progress?.id) { mutableStateOf(progress?.projectNotes ?: "") }
        var selectedQuizOption by remember { mutableIntStateOf(0) }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ForgeCard {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("${path.domain} • ${path.level}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                            Text(path.title, style = MaterialTheme.typography.headlineLarge, color = LuminousPearl)
                        }
                        TextButton(onClick = { openedPath = null }) { Text("Back") }
                    }
                    Text(path.overview, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    if (progress?.completed == true) {
                        Spacer(Modifier.height(6.dp))
                        Text("Completed! Recommended Next: ${path.nextStageRecommendation}", style = MaterialTheme.typography.labelMedium, color = VerifiedEmerald)
                    }
                }
            }

            items(path.lessons) { lesson ->
                val done = completedLessonIds.contains(lesson.id)
                ForgeCard(borderColor = if (done) VerifiedEmerald.copy(alpha = 0.55f) else DustyLavender.copy(alpha = 0.3f)) {
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                        Checkbox(checked = done, onCheckedChange = { onToggleLesson(path.id, lesson.id) })
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                            Text(lesson.summary, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            Spacer(Modifier.height(4.dp))
                            Text("Concept: ${lesson.keyConcept}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            Text("Exercise: ${lesson.exercisePrompt}", style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    playingVideoTitle = lesson.title
                                    playingVideoUrl = lesson.videoUrl
                                }
                            ) {
                                Icon(Icons.Filled.PlayCircle, contentDescription = "Watch Lesson Video", modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Play Video Lesson")
                            }
                        }
                    }
                }
            }

            item {
                ForgeCard {
                    Text("Practical Project & Assessment", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    Spacer(Modifier.height(4.dp))
                    Text("Project Brief: ${path.projectBrief}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = projectNotes,
                        onValueChange = { projectNotes = it },
                        label = { Text("Project Deliverable Summary / Repo Notes") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Assessment: ${path.quizQuestion}", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                    Spacer(Modifier.height(6.dp))
                    path.quizOptions.forEachIndexed { idx, opt ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (selectedQuizOption == idx) SmokyViolet else DeepObsidianPlum,
                            border = BorderStroke(1.dp, if (selectedQuizOption == idx) EtherealMist else SmokyViolet),
                            onClick = { selectedQuizOption = idx }
                        ) {
                            Text(opt, style = MaterialTheme.typography.bodySmall, color = LuminousPearl, modifier = Modifier.padding(10.dp))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onSubmitProjectQuiz(path.id, projectNotes, selectedQuizOption) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                    ) {
                        Text("Submit Project & Quiz")
                    }
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                SectionTitle(
                    title = "Skill Forge & Communication Lab",
                    subtitle = "Practical learning paths, projects & AI communication coaching"
                )
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ForgePillChip("Skill Paths", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                    ForgePillChip("Communication Lab", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                }
            }

            if (selectedTab == 0) {
                items(DomainCatalog.skillPaths) { path ->
                    val progress = courseProgress.firstOrNull { it.courseId == path.id }
                    val doneCount = progress?.completedLessons?.split(",")?.count { it.isNotBlank() } ?: 0
                    val totalSteps = path.lessons.size + 1
                    val pct = if (progress?.completed == true) 100 else ((doneCount * 100) / totalSteps)
                    ForgeCard(onClick = { openedPath = path }) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text("${path.domain} • ${path.estimatedHours}h • ${path.level}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                                Text(path.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                            }
                            Text("$pct%", style = MaterialTheme.typography.titleMedium, color = if (pct == 100) VerifiedEmerald else EtherealMist)
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(path.overview, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Spacer(Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { pct / 100f },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = EtherealMist,
                            trackColor = SmokyViolet
                        )
                    }
                }
            } else {
                item {
                    CommunicationLabSection(
                        history = commPractice,
                        evalState = commEvalState,
                        onEvaluate = onEvaluateCommunication
                    )
                }
            }
        }
    }

    if (playingVideoUrl != null && playingVideoTitle != null) {
        VideoPlayerDialog(
            title = playingVideoTitle!!,
            creatorLabel = "Skill Forge Video Lesson",
            videoUrl = playingVideoUrl!!,
            resumePositionMs = 0L,
            onSaveProgress = { _, _ -> },
            onDismiss = {
                playingVideoUrl = null
                playingVideoTitle = null
            }
        )
    }
}

@Composable
private fun CommunicationLabSection(
    history: List<CommunicationPracticeEntity>,
    evalState: AiCallState<CommunicationEvaluationResult>,
    onEvaluate: (String, String, String) -> Unit
) {
    val modes = listOf("SPEAKING", "VOCABULARY", "GRAMMAR", "SENTENCE", "PUBLIC_SPEAKING")
    var selectedMode by remember { mutableStateOf("SPEAKING") }
    val prompts = DomainCatalog.communicationPrompts[selectedMode].orEmpty()
    var promptIdx by remember(selectedMode) { mutableIntStateOf(0) }
    val activePrompt = prompts.getOrElse(promptIdx % prompts.size.coerceAtLeast(1)) { "" }
    var userResponse by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AiPlaceholderNoticeBanner()
        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            modes.forEach { m ->
                ForgePillChip(m.replace("_", " "), selectedMode == m, { selectedMode = m })
            }
        }
        ForgeCard {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Challenge Prompt", style = MaterialTheme.typography.titleMedium, color = GlacierBlueGlow)
                TextButton(onClick = { promptIdx++ }) { Text("Next Prompt") }
            }
            Text(activePrompt, style = MaterialTheme.typography.bodyMedium, color = LuminousPearl)
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = userResponse,
                onValueChange = { userResponse = it },
                label = { Text("Write or paste your spoken/written response") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth().testTag("comm_lab_input")
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    onEvaluate(selectedMode, activePrompt, userResponse)
                    userResponse = ""
                },
                enabled = userResponse.isNotBlank() && evalState !is AiCallState.Loading,
                modifier = Modifier.fillMaxWidth().testTag("comm_lab_submit"),
                colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
            ) {
                Text(if (evalState is AiCallState.Loading) "Evaluating..." else "Get AI Communication Feedback")
            }
        }

        if (evalState is AiCallState.Success) {
            val res = evalState.data
            ForgeCard(borderColor = GlacierBlueGlow) {
                Text("Latest Feedback • Score ${res.score}/100", style = MaterialTheme.typography.titleMedium, color = VerifiedEmerald)
                Text("Clarity: ${res.clarityFeedback}", style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                Text("Vocabulary: ${res.vocabularyTip}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                Text("Grammar: ${res.grammarNote}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                Text(res.improvedExample, style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
            }
        }

        if (history.isNotEmpty()) {
            Text("Practice History (${history.size})", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
            history.take(5).forEach { item ->
                ForgeCard {
                    Text("${item.mode} • Score ${item.score}/100", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                    Text(item.userResponse, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                    Text(item.aiFeedback, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                }
            }
        }
    }
}
