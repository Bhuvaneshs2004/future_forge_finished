package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.catalog.DomainCatalog
import com.example.data.catalog.ExamModule
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun ExamHubScreen(
    attempts: List<ExamAttemptEntity>,
    doubts: List<DoubtEntity>,
    doubtLoading: Boolean,
    studyTasks: List<StudyPlanTaskEntity>,
    onSubmitExam: (examId: String, answers: Map<String, Int>, durationSeconds: Int) -> Unit,
    onSolveDoubt: (question: String, imageUri: String?) -> Unit,
    onAskFollowUp: (DoubtEntity, String) -> Unit,
    onAddStudyTask: (title: String, subject: String, dueDate: String, durationMinutes: Int, priority: String) -> Unit,
    onToggleStudyTask: (StudyPlanTaskEntity) -> Unit,
    onDeleteStudyTask: (String) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Exams, 1 = AI Doubt Solver, 2 = Study Planner
    var activeExam by remember { mutableStateOf<ExamModule?>(null) }
    var selectedAnswers by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }
    var elapsedSeconds by remember { mutableIntStateOf(0) }
    var reviewAttempt by remember { mutableStateOf<ExamAttemptEntity?>(null) }

    if (activeExam != null) {
        BackHandler { activeExam = null }
        LaunchedEffect(activeExam?.id) {
            elapsedSeconds = 0
            while (activeExam != null) {
                delay(1000)
                elapsedSeconds++
            }
        }
        val exam = activeExam!!
        val remainingSec = (exam.durationMinutes * 60 - elapsedSeconds).coerceAtLeast(0)
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ForgeCard(borderColor = GlacierBlueGlow) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(exam.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                            Text("Remaining: ${remainingSec / 60}m ${remainingSec % 60}s", style = MaterialTheme.typography.labelLarge, color = GlacierBlueGlow)
                        }
                        TextButton(onClick = { activeExam = null }) { Text("Exit") }
                    }
                }
            }
            items(exam.questions) { q ->
                ForgeCard {
                    Text("${q.topic}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                    Spacer(Modifier.height(4.dp))
                    Text(q.prompt, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    Spacer(Modifier.height(8.dp))
                    q.options.forEachIndexed { idx, opt ->
                        val picked = selectedAnswers[q.id] == idx
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = if (picked) SmokyViolet else DeepObsidianPlum,
                            border = BorderStroke(1.dp, if (picked) EtherealMist else SmokyViolet),
                            onClick = { selectedAnswers = selectedAnswers + (q.id to idx) }
                        ) {
                            Text(
                                text = opt,
                                style = MaterialTheme.typography.bodyMedium,
                                color = LuminousPearl,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = {
                        onSubmitExam(exam.id, selectedAnswers, elapsedSeconds)
                        activeExam = null
                        selectedAnswers = emptyMap()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp).testTag("submit_exam_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                ) {
                    Text("Submit Mock Examination")
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "Exam Preparation Hub",
                subtitle = "Timed assessments, AI Doubt Solver & Smart Study Planner"
            )
        }
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgePillChip("Mock Exams", selectedTab == 0, { selectedTab = 0 }, Modifier.weight(1f))
                ForgePillChip("Doubt Solver", selectedTab == 1, { selectedTab = 1 }, Modifier.weight(1f))
                ForgePillChip("Study Planner", selectedTab == 2, { selectedTab = 2 }, Modifier.weight(1f))
            }
        }

        when (selectedTab) {
            0 -> {
                val avgScore = if (attempts.isEmpty()) 0 else attempts.map { it.scorePercent }.average().toInt()
                item {
                    ForgeCard {
                        Text("Examination Readiness Tracker", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(4.dp))
                        Text("Tests Taken: ${attempts.size} • Average Score: $avgScore%", style = MaterialTheme.typography.bodyMedium, color = EtherealMist)
                    }
                }
                items(DomainCatalog.exams) { exam ->
                    ForgeCard {
                        Text("${exam.category} • ${exam.durationMinutes} min • ${exam.questions.size} Qs", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(exam.title, style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                        Text(exam.licenseNote, style = MaterialTheme.typography.bodySmall, color = DustyLavender)
                        Spacer(Modifier.height(6.dp))
                        Text("Key Study Notes:", style = MaterialTheme.typography.labelMedium, color = EtherealMist)
                        exam.studyNotes.forEach { note ->
                            Text("• $note", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                selectedAnswers = emptyMap()
                                activeExam = exam
                            },
                            modifier = Modifier.fillMaxWidth().testTag("start_${exam.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) {
                            Text("Start Timed Assessment")
                        }
                    }
                }
                if (attempts.isNotEmpty()) {
                    item {
                        Text("Recent Test History & Review", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                    }
                    items(attempts, key = { it.id }) { att ->
                        ForgeCard(onClick = { reviewAttempt = att }) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(att.examTitle, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                    Text("Correct: ${att.correctCount}/${att.totalQuestions} • ${att.durationSeconds}s", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                                }
                                Text("${att.scorePercent}%", style = MaterialTheme.typography.titleLarge, color = if (att.scorePercent >= 70) VerifiedEmerald else CautionAmber)
                            }
                            Text("Tap to inspect step-by-step question explanations", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        }
                    }
                }
            }

            1 -> {
                item {
                    DoubtSolverSection(
                        doubts = doubts,
                        isLoading = doubtLoading,
                        onSolveDoubt = onSolveDoubt,
                        onAskFollowUp = onAskFollowUp
                    )
                }
            }

            2 -> {
                item {
                    StudyPlannerSection(
                        tasks = studyTasks,
                        onAddTask = onAddStudyTask,
                        onToggleTask = onToggleStudyTask,
                        onDeleteTask = onDeleteStudyTask
                    )
                }
            }
        }
    }

    if (reviewAttempt != null) {
        val att = reviewAttempt!!
        AlertDialog(
            onDismissRequest = { reviewAttempt = null },
            containerColor = MidnightIndigo,
            title = { Text("Question Review • ${att.scorePercent}%", color = LuminousPearl) },
            text = { Text(att.reviewSummary, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver) },
            confirmButton = {
                TextButton(onClick = { reviewAttempt = null }) { Text("Close", color = EtherealMist) }
            }
        )
    }
}

@Composable
private fun DoubtSolverSection(
    doubts: List<DoubtEntity>,
    isLoading: Boolean,
    onSolveDoubt: (String, String?) -> Unit,
    onAskFollowUp: (DoubtEntity, String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var followUpDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> pickedImageUri = uri }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        AiPlaceholderNoticeBanner()
        ForgeCard {
            Text("Ask Any Academic or Aptitude Question", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Type your question or describe the problem") },
                minLines = 2,
                modifier = Modifier.fillMaxWidth().testTag("doubt_question_input")
            )
            Spacer(Modifier.height(8.dp))
            if (pickedImageUri != null) {
                AsyncImage(
                    model = pickedImageUri,
                    contentDescription = "Uploaded doubt question image",
                    modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = {
                        imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Image, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (pickedImageUri == null) "Attach Photo" else "Change Photo")
                }
                Button(
                    onClick = {
                        onSolveDoubt(question, pickedImageUri?.toString())
                        question = ""
                        pickedImageUri = null
                    },
                    enabled = !isLoading && (question.isNotBlank() || pickedImageUri != null),
                    modifier = Modifier.weight(1f).testTag("solve_doubt_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                ) {
                    Text(if (isLoading) "Solving..." else "Solve Step-by-Step")
                }
            }
        }

        doubts.forEach { d ->
            ForgeCard {
                Text("${d.subject} • ${d.topic}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                Text(d.questionText, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                if (!d.imageUri.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    AsyncImage(
                        model = Uri.parse(d.imageUri),
                        contentDescription = "Doubt attachment",
                        modifier = Modifier.fillMaxWidth().height(110.dp).clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(d.explanation, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                if (d.followUpHistory.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(10.dp), color = DeepObsidianPlum) {
                        Text(d.followUpHistory, style = MaterialTheme.typography.bodySmall, color = EtherealMist, modifier = Modifier.padding(10.dp))
                    }
                }
                Spacer(Modifier.height(8.dp))
                val draft = followUpDrafts[d.id] ?: ""
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = draft,
                        onValueChange = { followUpDrafts = followUpDrafts + (d.id to it) },
                        label = { Text("Ask follow-up") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            onAskFollowUp(d, draft)
                            followUpDrafts = followUpDrafts - d.id
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                    ) { Text("Ask") }
                }
            }
        }
    }
}

@Composable
private fun StudyPlannerSection(
    tasks: List<StudyPlanTaskEntity>,
    onAddTask: (String, String, String, Int, String) -> Unit,
    onToggleTask: (StudyPlanTaskEntity) -> Unit,
    onDeleteTask: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-11-28") }
    var duration by remember { mutableStateOf("45") }
    var priority by remember { mutableStateOf("HIGH") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        ForgeCard {
            Text("Schedule Daily / Weekly Study Block", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Study Goal") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Date") }, modifier = Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(value = duration, onValueChange = { duration = it.filter { c -> c.isDigit() } }, label = { Text("Minutes") }, modifier = Modifier.weight(1f))
                ForgePillChip("HIGH", priority == "HIGH", { priority = "HIGH" })
                ForgePillChip("NORMAL", priority == "NORMAL", { priority = "NORMAL" })
            }
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    onAddTask(title, subject, dueDate, duration.toIntOrNull() ?: 45, priority)
                    title = ""
                    subject = ""
                },
                modifier = Modifier.fillMaxWidth().testTag("add_study_task_button"),
                colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
            ) {
                Text("Add to Study Planner")
            }
        }

        tasks.forEach { task ->
            ForgeCard {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = task.completed, onCheckedChange = { onToggleTask(task) })
                    Column(Modifier.weight(1f)) {
                        Text(task.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Text("${task.subject} • ${task.dueDate} • ${task.durationMinutes} min • ${task.priority}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    }
                    IconButton(onClick = { onDeleteTask(task.id) }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete Study Task", tint = AlertRose)
                    }
                }
            }
        }
    }
}
