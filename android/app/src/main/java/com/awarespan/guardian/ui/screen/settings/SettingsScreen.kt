package com.awarespan.guardian.ui.screen.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var showSignOutDialog by remember { mutableStateOf(false) }
    var showDiagnostics by remember { mutableStateOf(false) }

    LaunchedEffect(state.loggedOut) {
        if (state.loggedOut) onLoggedOut()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Column {
                Text(
                    text = "SETTINGS & SYSTEM",
                    fontFamily = FontFamilyDisplay,
                    fontSize = 18.sp,
                    color = BoneWhite,
                )
                Text(
                    text = "OPERATOR PROFILE // NOTIFICATIONS // STORAGE",
                    fontFamily = FontFamilyMono,
                    fontSize = 10.sp,
                    letterSpacing = 0.8.sp,
                    color = SteelMid,
                )
            }

            // Operator Profile Card
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "AUTHENTICATED OPERATOR",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = state.user?.username ?: "ACTIVE_OPERATOR",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 18.sp,
                                color = BoneWhite,
                            )
                            Text(
                                text = "CONSOLE ACCESS AUTHORIZED",
                                fontFamily = FontFamilyMono,
                                fontSize = 10.sp,
                                letterSpacing = 0.5.sp,
                                color = SteelMid,
                            )
                        }

                        val roleName = state.user?.role?.name ?: "OPERATOR"
                        AwarespanBadge(
                            text = roleName,
                            color = if (roleName == "ADMIN") EmberOrange else StatusSafe
                        )
                    }
                }
            }

            // Notification Preferences Card
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "NOTIFICATIONS",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Threat Alerts & Incidents",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 14.sp,
                                color = BoneWhite,
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = "Deliver high-priority alerts for verified threats and active incidents.",
                                fontFamily = FontFamilyDisplay,
                                fontSize = 12.sp,
                                color = FogLight,
                            )
                        }
                        Switch(
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = BoneWhite,
                                checkedTrackColor = EmberOrange,
                                checkedBorderColor = EmberOrange,
                                uncheckedThumbColor = SteelMid,
                                uncheckedTrackColor = Carbon,
                                uncheckedBorderColor = GraphiteBorder
                            )
                        )
                    }
                }
            }

            // Local Storage & Room Cache
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "LOCAL STORAGE & CACHE",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${state.cachedIncidentCount} Incidents Persisted",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 15.sp,
                        color = BoneWhite,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Incident and telemetry records cached locally for offline operations when network connectivity is lost.",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 12.sp,
                        color = FogLight,
                    )
                    Spacer(Modifier.height(12.dp))
                    AwarespanButton(
                        text = if (state.cacheCleared) "Cache Cleared" else "Clear Cached Data",
                        onClick = { viewModel.clearCache() },
                        variant = ButtonVariant.OUTLINE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // Optional Collapsed Diagnostics Section
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDiagnostics = !showDiagnostics },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "SYSTEM DIAGNOSTICS",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = SteelMid,
                        )
                        Text(
                            text = if (showDiagnostics) "HIDE [-]" else "SHOW [+]",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = EmberOrange,
                        )
                    }

                    if (showDiagnostics) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "GATEWAY ENDPOINT",
                            fontFamily = FontFamilyMono,
                            fontSize = 10.sp,
                            color = SteelMid,
                        )
                        Text(
                            text = state.endpointUrl,
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = BoneWhite,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "SECURITY & PROTOCOL",
                            fontFamily = FontFamilyMono,
                            fontSize = 10.sp,
                            color = SteelMid,
                        )
                        Text(
                            text = "HTTPS // WSS SECURE TLS VERIFIED",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = StatusSafe,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "DATABASE SCHEMA",
                            fontFamily = FontFamilyMono,
                            fontSize = 10.sp,
                            color = SteelMid,
                        )
                        Text(
                            text = "Room SQLite v3 (Migration 2 -> 3 Verified)",
                            fontFamily = FontFamilyMono,
                            fontSize = 11.sp,
                            color = BoneWhite,
                        )
                    }
                }
            }

            // About AWARESPAN
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "ABOUT AWARESPAN",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "AWARESPAN",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 16.sp,
                        color = BoneWhite,
                    )
                    Text(
                        text = "Awareness Without Boundaries.",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 13.sp,
                        color = EmberOrange,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "VERSION: ${state.appVersion}",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = FogLight,
                    )
                }
            }

            // Sign Out Button
            Spacer(Modifier.height(8.dp))
            AwarespanButton(
                text = "Sign Out",
                onClick = { showSignOutDialog = true },
                variant = ButtonVariant.CRITICAL,
                modifier = Modifier.fillMaxWidth(),
            )

            // Extra padding to ensure full clearance of gesture bar and bottom navigation
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            shape = RectangleShape,
            containerColor = Carbon,
            title = {
                Text(
                    text = "CONFIRM SIGN OUT",
                    fontFamily = FontFamilyDisplay,
                    fontSize = 16.sp,
                    color = BoneWhite,
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to sign out of the AWARESPAN console? Active background synchronization will be suspended until your next sign in.",
                    fontFamily = FontFamilyDisplay,
                    fontSize = 13.sp,
                    color = FogLight,
                )
            },
            confirmButton = {
                AwarespanButton(
                    text = "SIGN OUT",
                    onClick = {
                        showSignOutDialog = false
                        viewModel.logout()
                    },
                    variant = ButtonVariant.CRITICAL,
                )
            },
            dismissButton = {
                AwarespanButton(
                    text = "CANCEL",
                    onClick = { showSignOutDialog = false },
                    variant = ButtonVariant.OUTLINE,
                )
            }
        )
    }
}
