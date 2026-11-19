package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.local.UserRole
import com.example.ui.components.ForgeBrandMark
import com.example.ui.components.ForgeCard
import com.example.ui.components.ForgePillChip
import com.example.ui.theme.*

@Composable
fun AuthScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onLogin: (email: String, password: String) -> Unit,
    onRegister: (name: String, email: String, password: String, role: UserRole, headline: String, inviteCode: String) -> Unit,
    onClearError: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(UserRole.STUDENT) }
    var headlineOrOrg by remember { mutableStateOf("") }
    var adminInviteCode by remember { mutableStateOf("") }

    ForgeAtmosphericBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 560.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(145.dp)
                            .clip(RoundedCornerShape(22.dp))
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_forge_hero_banner),
                            contentDescription = "Future Forge Academic Futurism Observatory",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ForgeBrandMark(size = 54.dp)
                            Column {
                                Text(
                                    text = "FUTURE FORGE",
                                    style = MaterialTheme.typography.displayMedium,
                                    color = LuminousPearl
                                )
                                Text(
                                    text = stringResource(id = R.string.app_tagline),
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = EtherealMist
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ForgePillChip(
                            text = "Sign In",
                            selected = !isRegisterMode,
                            onClick = { isRegisterMode = false; onClearError() },
                            modifier = Modifier.weight(1f).testTag("auth_mode_login")
                        )
                        ForgePillChip(
                            text = "Create Account",
                            selected = isRegisterMode,
                            onClick = { isRegisterMode = true; onClearError() },
                            modifier = Modifier.weight(1f).testTag("auth_mode_register")
                        )
                    }

                    Spacer(Modifier.height(14.dp))

                    ForgeCard {
                        Text(
                            text = if (isRegisterMode) "Forge Your Account" else "Welcome Back, Scholar",
                            style = MaterialTheme.typography.titleLarge,
                            color = LuminousPearl
                        )
                        Text(
                            text = stringResource(id = R.string.app_motto),
                            style = MaterialTheme.typography.bodySmall,
                            color = DustyLavender
                        )
                        Spacer(Modifier.height(14.dp))

                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Full Name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("register_name_input")
                            )
                            Spacer(Modifier.height(10.dp))
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )
                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (min 6 chars)") },
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        contentDescription = "Toggle password visibility"
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                        )

                        if (isRegisterMode) {
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Select Account Category (RBAC)",
                                style = MaterialTheme.typography.labelLarge,
                                color = LuminousPearl
                            )
                            Spacer(Modifier.height(8.dp))
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                UserRole.entries.chunked(2).forEach { rowRoles ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        rowRoles.forEach { role ->
                                            ForgePillChip(
                                                text = role.label,
                                                selected = selectedRole == role,
                                                onClick = { selectedRole = role },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = headlineOrOrg,
                                onValueChange = { headlineOrOrg = it },
                                label = {
                                    Text(
                                        when (selectedRole) {
                                            UserRole.RECRUITER -> "Company / Organization Name"
                                            UserRole.PROFESSIONAL -> "Current Role & Company"
                                            else -> "Headline / Field of Study"
                                        }
                                    )
                                },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (selectedRole == UserRole.ADMIN) {
                                Spacer(Modifier.height(10.dp))
                                OutlinedTextField(
                                    value = adminInviteCode,
                                    onValueChange = { adminInviteCode = it },
                                    label = { Text("Admin Invite Code (FF-ADMIN-2026)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Spacer(Modifier.height(10.dp))
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = AlertRose.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, AlertRose.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = LuminousPearl,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = {
                                if (isRegisterMode) {
                                    onRegister(name, email, password, selectedRole, headlineOrOrg, adminInviteCode)
                                } else {
                                    onLogin(email, password)
                                }
                            },
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("auth_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EtherealMist,
                                contentColor = MidnightIndigo
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MidnightIndigo,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = if (isRegisterMode) Icons.Filled.PersonAdd else Icons.Filled.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(if (isRegisterMode) "Register & Enter Portal" else "Sign In to Future Forge")
                            }
                        }
                    }

                    Spacer(Modifier.height(14.dp))

                    ForgeCard(
                        containerColor = MidnightIndigo.copy(alpha = 0.85f),
                        borderColor = SmokyViolet
                    ) {
                        Text(
                            text = "Quick Role Access (Pre-Seeded Local Accounts)",
                            style = MaterialTheme.typography.titleSmall,
                            color = EtherealMist
                        )
                        Text(
                            text = "Tap any role below to fill verified local credentials (Password: Forge@2026) and explore role-based portals.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MoonlitSilver
                        )
                        Spacer(Modifier.height(10.dp))
                        val presets = listOf(
                            "Student" to "student@futureforge.app",
                            "Mentor" to "mentor@futureforge.app",
                            "Recruiter" to "recruiter@futureforge.app",
                            "Admin" to "admin@futureforge.app"
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presets.forEach { (label, presetEmail) ->
                                OutlinedButton(
                                    onClick = {
                                        isRegisterMode = false
                                        email = presetEmail
                                        password = "Forge@2026"
                                        onLogin(presetEmail, "Forge@2026")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("quick_login_${label.lowercase()}"),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                    border = BorderStroke(1.dp, DustyLavender.copy(alpha = 0.45f))
                                ) {
                                    Text(label, style = MaterialTheme.typography.labelSmall, color = LuminousPearl)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
