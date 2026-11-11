package com.awarespan.guardian.ui.screen.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.awarespan.guardian.ui.components.AwarespanButton
import com.awarespan.guardian.ui.components.AwarespanTextField
import com.awarespan.guardian.ui.theme.BoneWhite
import com.awarespan.guardian.ui.theme.Carbon
import com.awarespan.guardian.ui.theme.EmberOrange
import com.awarespan.guardian.ui.theme.FontFamilyDisplay
import com.awarespan.guardian.ui.theme.FontFamilyMono
import com.awarespan.guardian.ui.theme.GraphiteBorder
import com.awarespan.guardian.ui.theme.SteelMid
import com.awarespan.guardian.ui.theme.VoidBlack

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsState()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(state.loggedInUser) {
        if (state.loggedInUser != null) onLoggedIn()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(VoidBlack)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start,
        ) {
            // Brand Wordmark & Visual Anchor
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .height(28.dp)
                        .background(EmberOrange, RectangleShape)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "AWARESPAN",
                    fontFamily = FontFamilyDisplay,
                    fontWeight = FontWeight.Light,
                    fontSize = 28.sp,
                    letterSpacing = 1.sp,
                    color = BoneWhite,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = "AWARENESS WITHOUT BOUNDARIES",
                fontFamily = FontFamilyMono,
                fontSize = 11.sp,
                letterSpacing = 1.2.sp,
                color = SteelMid,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "TACTICAL OPERATIONS CONSOLE",
                fontFamily = FontFamilyMono,
                fontSize = 10.sp,
                letterSpacing = 0.8.sp,
                color = EmberOrange,
            )

            Spacer(Modifier.height(40.dp))

            // Inputs in sharp Carbon panels
            AwarespanTextField(
                value = username,
                onValueChange = { username = it },
                label = "OPERATOR IDENTIFIER",
                placeholder = "username or operator ID",
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            AwarespanTextField(
                value = password,
                onValueChange = { password = it },
                label = "AUTHENTICATION KEY",
                placeholder = "password",
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
            )

            // Diagnostic safe error readout
            state.error?.let { err ->
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Carbon, RectangleShape)
                        .border(1.dp, EmberOrange, RectangleShape)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "AUTHENTICATION FAILURE",
                            fontFamily = FontFamilyMono,
                            fontSize = 10.sp,
                            letterSpacing = 0.6.sp,
                            color = EmberOrange,
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = err,
                            fontFamily = FontFamilyDisplay,
                            fontSize = 12.sp,
                            color = BoneWhite,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            AwarespanButton(
                text = if (state.loading) "AUTHENTICATING…" else "SIGN IN",
                onClick = { viewModel.login(username, password) },
                loading = state.loading,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(48.dp))

            // Operational endpoint readout
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, GraphiteBorder, RectangleShape)
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = "TARGET PRODUCTION BACKEND",
                        fontFamily = FontFamilyMono,
                        fontSize = 9.sp,
                        letterSpacing = 0.6.sp,
                        color = SteelMid,
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = state.endpointUrl,
                        fontFamily = FontFamilyMono,
                        fontSize = 11.sp,
                        color = BoneWhite,
                    )
                }
            }
        }
    }
}
