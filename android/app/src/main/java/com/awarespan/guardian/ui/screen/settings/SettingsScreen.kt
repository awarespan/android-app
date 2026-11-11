package com.awarespan.guardian.ui.screen.settings

import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.ui.components.AwarespanBadge
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanCard
import com.awarespan.guardian.ui.components.ButtonVariant
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FogLight
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.StatusCritical
import com.awarespan.guardian.ui.theme.StatusSafe
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun SettingsScreen(
    onLoggedOut: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()

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
                    text = "OPERATOR PROFILE // CONFIGURATION // CACHE",
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
                            state.user?.id?.let {
                                Text(
                                    text = "OPERATOR ID: $it",
                                    fontFamily = FontFamilyMono,
                                    fontSize = 10.sp,
                                    color = SteelMid,
                                )
                            }
                        }

                        val roleName = state.user?.role?.name ?: "OPERATOR"
                        AwarespanBadge(
                            text = roleName,
                            color = if (roleName == "ADMIN") EmberOrange else StatusSafe
                        )
                    }
                }
            }

            // Production Gateway Details
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "CONNECTED BACKEND GATEWAY",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = state.endpointUrl,
                        fontFamily = FontFamilyMono,
                        fontSize = 12.sp,
                        color = BoneWhite,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "PROTOCOL: HTTPS // WSS SECURE TLS VERIFIED",
                        fontFamily = FontFamilyMono,
                        fontSize = 10.sp,
                        color = StatusSafe,
                    )
                }
            }

            // Room Cache Management
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "OFFLINE STORAGE (ROOM)",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${state.cachedIncidentCount} INCIDENTS PERSISTED",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 15.sp,
                        color = BoneWhite,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Local SQLite Room database caches incident and node state for offline accessibility during network dropouts.",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 12.sp,
                        color = FogLight,
                    )
                    Spacer(Modifier.height(12.dp))
                    AwarespanButton(
                        text = "Purge Local Cache",
                        onClick = { viewModel.clearCache() },
                        variant = ButtonVariant.OUTLINE,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // App Identity & Build Metadata
            AwarespanCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    Text(
                        text = "AWARESPAN APPLICATION",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Awareness Without Boundaries.",
                        fontFamily = FontFamilyDisplay,
                        fontSize = 14.sp,
                        color = BoneWhite,
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "VERSION: ${state.appVersion}",
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = FogLight,
                    )
                    Text(
                        text = "PACKAGE: com.awarespan.guardian",
                        fontFamily = FontFamilyMono,
                        fontSize = 10.sp,
                        color = SteelMid,
                    )
                }
            }

            // Sign Out Button
            Spacer(Modifier.height(8.dp))
            AwarespanButton(
                text = "SIGN OUT OPERATOR",
                onClick = { viewModel.logout() },
                variant = ButtonVariant.CRITICAL,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(24.dp))
        }
    }
}
