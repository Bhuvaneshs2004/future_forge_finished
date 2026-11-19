package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*
import java.util.UUID

@Composable
fun RecruiterDashboardScreen(
    user: UserEntity,
    opportunities: List<JobOpportunityEntity>,
    applications: List<ApplicationEntity>,
    companies: List<CompanyProfileEntity>,
    customerVoice: List<CustomerVoiceEntity>,
    onPublishOpportunity: (String, String, String, String, String, String, String, String, String, String, String) -> Unit,
    onUpdateApplication: (ApplicationEntity, status: String, score: Int?, interviewDate: String, feedback: String) -> Unit,
    onUpsertCompany: (CompanyProfileEntity) -> Unit,
    onRespondCustomerVoice: (CustomerVoiceEntity, String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Applicants, 1=Publish Vacancy, 2=Company & Insights

    // Vacancy form
    var jobTitle by remember { mutableStateOf("") }
    var companyName by remember { mutableStateOf(user.companyName.ifBlank { "Meridian Cloud Systems" }) }
    var jobType by remember { mutableStateOf("INTERNSHIP") }
    var department by remember { mutableStateOf("Software Engineering") }
    var location by remember { mutableStateOf("Bengaluru / Hybrid") }
    var eligibility by remember { mutableStateOf("2026/2027 Graduates with portfolio projects") }
    var reqSkills by remember { mutableStateOf("Kotlin, Data Structures, SQL, REST APIs") }
    var compensation by remember { mutableStateOf("INR 45,000 / month") }
    var deadline by remember { mutableStateOf("2026-12-20") }
    var hiringStatus by remember { mutableStateOf("OPEN") }

    // Applicant management state
    var managingApp by remember { mutableStateOf<ApplicationEntity?>(null) }

    // Company profile editor
    val existingCompany = companies.firstOrNull { it.name.equals(companyName, true) }
    var compIndustry by remember(existingCompany?.id) { mutableStateOf(existingCompany?.industry ?: user.companyIndustry.ifBlank { "Private Technology" }) }
    var compProducts by remember(existingCompany?.id) { mutableStateOf(existingCompany?.productsServices ?: "") }
    var compLocations by remember(existingCompany?.id) { mutableStateOf(existingCompany?.locations ?: "Bengaluru, Remote") }
    var compStages by remember(existingCompany?.id) { mutableStateOf(existingCompany?.recruitmentStages ?: "1. Online Assessment -> 2. Technical Panel -> 3. Final HR") }
    var cvResponseDrafts by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ForgeBrandMark(size = 40.dp)
                    Column {
                        Text("Employer & Recruiter Portal", style = MaterialTheme.typography.headlineMedium, color = LuminousPearl)
                        Text("${user.name} • ${user.companyName.ifBlank { "Corporate Partner" }} (${user.verificationStatus})", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                    }
                }
                IconButton(onClick = onLogout, modifier = Modifier.testTag("recruiter_logout")) {
                    Icon(Icons.Filled.Logout, contentDescription = "Sign Out", tint = AlertRose)
                }
            }
        }

        item {
            Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ForgePillChip("Applicants (${applications.size})", selectedTab == 0, { selectedTab = 0 })
                ForgePillChip("Publish Vacancy", selectedTab == 1, { selectedTab = 1 })
                ForgePillChip("Company & Voice", selectedTab == 2, { selectedTab = 2 })
            }
        }

        when (selectedTab) {
            0 -> {
                if (applications.isEmpty()) {
                    item {
                        ForgeCard {
                            Text("No candidate applications received yet. Published opportunities appear immediately in Opportunity Gateway.", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    items(applications, key = { it.id }) { app ->
                        ForgeCard {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(app.applicantName, style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                                    Text("${app.opportunityTitle} • ${app.companyName}", style = MaterialTheme.typography.bodySmall, color = GlacierBlueGlow)
                                }
                                Text(app.status.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = VerifiedEmerald)
                            }
                            Text("Skills: ${app.applicantSkills}", style = MaterialTheme.typography.bodySmall, color = EtherealMist)
                            Text("Resume: ${app.resumeVersionTitle}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            if (app.assessmentScore != null) {
                                Text("Assessment Score: ${app.assessmentScore}%", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                            }
                            if (app.interviewDate.isNotBlank()) {
                                Text("Interview: ${app.interviewDate}", style = MaterialTheme.typography.labelSmall, color = EtherealMist)
                            }
                            if (app.recruiterFeedback.isNotBlank()) {
                                Text("Feedback Sent: ${app.recruiterFeedback}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                            }
                            Spacer(Modifier.height(8.dp))
                            Button(
                                onClick = { managingApp = app },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = SmokyViolet, contentColor = LuminousPearl)
                            ) { Text("Shortlist / Score / Schedule Interview") }
                        }
                    }
                }
            }

            1 -> {
                item {
                    ForgeCard {
                        Text("Publish Job, Internship or Campus Recruitment Drive", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(8.dp))
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("INTERNSHIP", "FRESHER", "EXPERIENCED", "APPRENTICESHIP").forEach { t ->
                                ForgePillChip(t, jobType == t, { jobType = t })
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ForgePillChip("OPEN", hiringStatus == "OPEN", { hiringStatus = "OPEN" })
                            ForgePillChip("CAMPUS DRIVE", hiringStatus == "CAMPUS_DRIVE", { hiringStatus = "CAMPUS_DRIVE" })
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(value = jobTitle, onValueChange = { jobTitle = it }, label = { Text("Role Title") }, modifier = Modifier.fillMaxWidth().testTag("recruiter_job_title"))
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Company Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = department, onValueChange = { department = it }, label = { Text("Department") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = location, onValueChange = { location = it }, label = { Text("Location") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = eligibility, onValueChange = { eligibility = it }, label = { Text("Eligibility Criteria") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = reqSkills, onValueChange = { reqSkills = it }, label = { Text("Required Skills") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(value = compensation, onValueChange = { compensation = it }, label = { Text("Salary / Stipend") }, modifier = Modifier.weight(1f))
                            OutlinedTextField(value = deadline, onValueChange = { deadline = it }, label = { Text("Deadline") }, modifier = Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onPublishOpportunity(jobTitle, companyName, jobType, department, location, eligibility, reqSkills, compensation, deadline, "Apply via Future Forge Portal.", hiringStatus)
                                jobTitle = ""
                            },
                            enabled = jobTitle.isNotBlank() && companyName.isNotBlank(),
                            modifier = Modifier.fillMaxWidth().testTag("publish_vacancy_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text("Publish Official Listing") }
                    }
                }
                items(opportunities.take(6), key = { it.id }) { opp ->
                    ForgeCard {
                        Text("${opp.type} • ${opp.hiringStatus}", style = MaterialTheme.typography.labelSmall, color = GlacierBlueGlow)
                        Text(opp.title, style = MaterialTheme.typography.titleSmall, color = LuminousPearl)
                        Text("${opp.companyName} • ${opp.location} • Deadline ${opp.deadline}", style = MaterialTheme.typography.bodySmall, color = MoonlitSilver)
                    }
                }
            }

            2 -> {
                item {
                    ForgeCard {
                        Text("Company Profile & Selection Process", style = MaterialTheme.typography.titleMedium, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = companyName, onValueChange = { companyName = it }, label = { Text("Organization Name") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = compIndustry, onValueChange = { compIndustry = it }, label = { Text("Industry") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = compProducts, onValueChange = { compProducts = it }, label = { Text("Products & Services") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = compLocations, onValueChange = { compLocations = it }, label = { Text("Office Locations") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = compStages, onValueChange = { compStages = it }, label = { Text("Published Selection Stages") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))
                        Button(
                            onClick = {
                                onUpsertCompany(
                                    CompanyProfileEntity(
                                        id = existingCompany?.id ?: "comp_${UUID.randomUUID().toString().take(6)}",
                                        name = companyName,
                                        industry = compIndustry,
                                        productsServices = compProducts.ifBlank { "Enterprise Software & Digital Platforms" },
                                        locations = compLocations,
                                        roleQualifications = existingCompany?.roleQualifications ?: eligibility,
                                        requiredSkills = reqSkills,
                                        toolsTech = existingCompany?.toolsTech ?: "Kotlin, SQL, Cloud",
                                        certifications = existingCompany?.certifications ?: "Role-relevant portfolio",
                                        recruitmentStages = compStages,
                                        fresherInfo = existingCompany?.fresherInfo ?: "Annual Graduate Cohort",
                                        internshipInfo = existingCompany?.internshipInfo ?: "Summer & Winter Internships",
                                        prepChecklist = existingCompany?.prepChecklist ?: "Review DSA & core projects",
                                        salaryBandSummary = compensation,
                                        sourceType = "OFFICIAL_EMPLOYER",
                                        lastUpdated = "2026-11-18",
                                        verified = user.verificationStatus == "VERIFIED"
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = EtherealMist, contentColor = MidnightIndigo)
                        ) { Text("Save Official Company Profile") }
                    }
                }
                items(customerVoice, key = { it.id }) { cv ->
                    val draft = cvResponseDrafts[cv.id] ?: cv.companyResponse
                    ForgeCard {
                        Text("Customer Voice • ${cv.companyName} (${cv.rating}/5)", style = MaterialTheme.typography.titleSmall, color = GlacierBlueGlow)
                        Text(cv.summary, style = MaterialTheme.typography.bodySmall, color = LuminousPearl)
                        Spacer(Modifier.height(6.dp))
                        OutlinedTextField(value = draft, onValueChange = { cvResponseDrafts = cvResponseDrafts + (cv.id to it) }, label = { Text("Official Company Response") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(6.dp))
                        OutlinedButton(onClick = { onRespondCustomerVoice(cv, draft) }) { Text("Update Response") }
                    }
                }
            }
        }
    }

    managingApp?.let { app ->
        var status by remember { mutableStateOf(app.status) }
        var scoreText by remember { mutableStateOf(app.assessmentScore?.toString() ?: "") }
        var interviewDate by remember { mutableStateOf(app.interviewDate) }
        var feedback by remember { mutableStateOf(app.recruiterFeedback) }
        AlertDialog(
            onDismissRequest = { managingApp = null },
            containerColor = MidnightIndigo,
            title = { Text("Manage: ${app.applicantName}", color = LuminousPearl) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("SHORTLISTED", "ASSESSMENT_SENT", "INTERVIEW_SCHEDULED", "ACCEPTED", "REJECTED").forEach { st ->
                            ForgePillChip(st.replace("_", " "), status == st, { status = st })
                        }
                    }
                    OutlinedTextField(value = scoreText, onValueChange = { scoreText = it.filter { c -> c.isDigit() } }, label = { Text("Assessment Score (0-100)") })
                    OutlinedTextField(value = interviewDate, onValueChange = { interviewDate = it }, label = { Text("Interview Date & Time") })
                    OutlinedTextField(value = feedback, onValueChange = { feedback = it }, label = { Text("Applicant Feedback") }, minLines = 2)
                }
            },
            confirmButton = {
                Button(onClick = {
                    onUpdateApplication(app, status, scoreText.toIntOrNull(), interviewDate, feedback)
                    managingApp = null
                }) { Text("Save Update") }
            },
            dismissButton = { TextButton(onClick = { managingApp = null }) { Text("Cancel") } }
        )
    }
}
