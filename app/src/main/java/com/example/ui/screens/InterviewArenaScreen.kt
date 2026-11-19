package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.InterviewSessionEntity
import com.example.data.local.UserEntity
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveInterviewState

@Composable
fun InterviewArenaScreen(
    user: UserEntity,
    activeState: ActiveInterviewState,
    history: List<InterviewSessionEntity>,
    verifiedMentors: List<UserEntity>,
    onStartInterview: (mode: String, role: String, company: String) -> Unit,
    onSubmitAnswer: (String) -> Unit,
    onFinishInterview: () -> Unit,
    onBookMentorMock: (mentor: UserEntity, topic: String, slot: String) -> Unit
) {
    val modes = listOf("TECHNICAL", "HR", "APTITUDE", "ROLE_SPECIFIC")
    var selectedMode by remember { mutableStateOf("TECHNICAL") }
    var roleTitle by remember { mutableStateOf("Software Engineer") }
    var companyName by remember { mutableStateOf("Meridian Cloud Systems") }
    var answerInput by remember { mutableStateOf("") }
    var mockBookingMentor by remember { mutableStateOf<UserEntity?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionTitle(
                title = "AI Interview Arena",
                subtitle = "Interactive HR, Technical & Aptitude simulations + Mentor Mocks"
            )
        }
        item { AiPlaceholderNoticeBanner() }

        if (!activeState.active) {
            item {
                ForgeCard {
                    Text("Configure Interview Session", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        modes.forEach { mode ->
                            ForgePillChip(mode.replace("_", " "), selectedMode == mode, { selectedMode = mode })
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(value = roleTitle, onValueChange = { roleTitle = it }, label = { Text("Target Role") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Target Private Company") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = stringResource(id = R.string.ai_guidance_disclaimer),
                        style = MaterialTheme.typography.bodySmall,
                        color = CautionAmber
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onStartInterview(selectedMode, roleTitle, companyName) },
                        modifier = Modifier.fillMaxWidth().testTag("start_interview_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                    ) {
                        Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Begin Interactive Simulation")
                    }
                }
            }

            item {
                ForgeCard(borderColor = GlacierBlueGlow.copy(alpha = 0.45f)) {
                    Text("Book a Human Mentor Mock Interview", style = MaterialTheme.typography.titleMedium, color = GlacierBlueGlow)
                    Text("Practice with verified industry professionals for authentic live feedback.", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    Spacer(Modifier.height(8.dp))
                    verifiedMentors.forEach { mentor ->
                        Surface(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = DeepObsidianPlum,
                            border = BorderStroke(1.dp, SmokyViolet)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(mentor.name, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                                    Text(mentor.headline, style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                                }
                                Button(
                                    onClick = { mockBookingMentor = mentor },
                                    colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                                ) { Text("Book Mock") }
                            }
                        }
                    }
                }
            }

            if (history.isNotEmpty()) {
                item {
                    Text("Saved Interview History", style = MaterialTheme.typography.titleLarge, color = LuminousPearl)
                }
                items(history, key = { it.id }) { session ->
                    ForgeCard {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("${session.mode} • ${session.roleTitle}", style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                            Text("Clarity ${session.clarityScore}% | Rel ${session.relevanceScore}%", style = MaterialTheme.typography.labelSmall, color = VerifiedEmerald)
                        }
                        Text(session.companyName, style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                        Text(session.feedbackSummary, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Practice Next: ${session.recommendedTopics}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    }
                }
            }
        } else {
            item {
                ForgeCard(borderColor = GlacierBlueGlow) {
                    Text("${activeState.mode} • ${activeState.roleTitle} @ ${activeState.companyName}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                    Spacer(Modifier.height(4.dp))
                    Text("Turn ${activeState.turnNumber}", style = MaterialTheme.typography.titleSmall, color = EtherealMist)
                    Spacer(Modifier.height(6.dp))
                    Text(activeState.currentQuestion, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                }
            }

            activeState.lastEvaluation?.let { eval ->
                item {
                    ForgeCard(containerColor = MidnightIndigo) {
                        Text("Answer Analysis • Clarity ${eval.clarityScore}% | Relevance ${eval.relevanceScore}%", style = MaterialTheme.typography.titleSmall, color = VerifiedEmerald)
                        Text(eval.constructiveFeedback, style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                        Text("Focus Area: ${eval.improvementFocus}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                        Text(eval.guidanceDisclaimer, style = MaterialTheme.typography.labelSmall, color = CautionAmber)
                    }
                }
            }

            item {
                ForgeCard {
                    OutlinedTextField(
                        value = answerInput,
                        onValueChange = { answerInput = it },
                        label = { Text("Your Interview Response") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth().testTag("interview_answer_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onSubmitAnswer(answerInput)
                                answerInput = ""
                            },
                            enabled = !activeState.loading && answerInput.isNotBlank(),
                            modifier = Modifier.weight(1f).testTag("submit_interview_answer"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) {
                            Text(if (activeState.loading) "Analyzing..." else "Submit Answer")
                        }
                        OutlinedButton(
                            onClick = onFinishInterview,
                            modifier = Modifier.testTag("finish_interview_button")
                        ) {
                            Text("Save & End")
                        }
                    }
                }
            }
        }
    }

    if (mockBookingMentor != null) {
        val mentor = mockBookingMentor!!
        var topic by remember { mutableStateOf("$roleTitle Mock Interview") }
        var slot by remember { mutableStateOf(mentor.availability.ifBlank { "Sat 11:00 AM" }) }
        AlertDialog(
            onDismissRequest = { mockBookingMentor = null },
            containerColor = MidnightIndigo,
            title = { Text("Book Mock with ${mentor.name}", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(mentor.expertise, style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    OutlinedTextField(value = topic, onValueChange = { topic = it }, label = { Text("Interview Focus") })
                    OutlinedTextField(value = slot, onValueChange = { slot = it }, label = { Text("Preferred Slot") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onBookMentorMock(mentor, topic, slot)
                        mockBookingMentor = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                ) { Text("Send Request") }
            },
            dismissButton = {
                TextButton(onClick = { mockBookingMentor = null }) { Text("Cancel", color = MoonlitSilver) }
            }
        )
    }
}
