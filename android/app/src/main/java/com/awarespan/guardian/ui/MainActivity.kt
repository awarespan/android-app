package com.awarespan.guardian.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.rememberNavController
import com.awarespan.guardian.data.remote.websocket.RealtimeEventManager
import com.awarespan.guardian.domain.repository.AuthRepository
import com.awarespan.guardian.service.IncidentSyncWorker
import com.awarespan.guardian.service.NotificationService
import com.awarespan.guardian.ui.navigation.AwarespanNavDestination
import com.awarespan.guardian.ui.navigation.SipNavGraph
import com.awarespan.guardian.ui.theme.AwarespanTheme
import com.awarespan.guardian.ui.theme.VoidBlack
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var authRepository: AuthRepository
    @Inject lateinit var realtimeEventManager: RealtimeEventManager

    private var pendingIncidentId by mutableStateOf<String?>(null)

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                NotificationService.ensureChannels(this)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        NotificationService.ensureChannels(this)

        val authenticated = authRepository.currentUser() != null
        val start = if (authenticated) {
            IncidentSyncWorker.schedule(this)
            AwarespanNavDestination.Dashboard.route
        } else {
            AwarespanNavDestination.Login.route
        }

        setContent {
            AwarespanTheme {
                Surface(color = VoidBlack) {
                    val navController = rememberNavController()

                    LaunchedEffect(authenticated) {
                        if (authenticated) {
                            checkNotificationPermission()
                            realtimeEventManager.startForegroundSession()
                        }
                    }
                    LaunchedEffect(authenticated, pendingIncidentId) {
                        if (authenticated) {
                            pendingIncidentId?.let { id ->
                                navController.navigate(AwarespanNavDestination.IncidentDetail.createRoute(id)) {
                                    launchSingleTop = true
                                }
                                pendingIncidentId = null
                            }
                        }
                    }

                    SipNavGraph(
                        navController = navController,
                        startDestination = start,
                        onAuthenticated = {
                            checkNotificationPermission()
                            realtimeEventManager.startForegroundSession()
                            IncidentSyncWorker.schedule(this@MainActivity)
                            pendingIncidentId?.let { id ->
                                navController.navigate(AwarespanNavDestination.IncidentDetail.createRoute(id)) {
                                    launchSingleTop = true
                                }
                                pendingIncidentId = null
                            }
                        },
                    )
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        if (authRepository.currentUser() != null) {
            realtimeEventManager.startForegroundSession()
        }
    }

    override fun onStop() {
        super.onStop()
        // Disconnect real-time WebSocket when operator leaves the app to prevent resource drain
        realtimeEventManager.stopForegroundSession()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingIncidentId = intent.getStringExtra(NotificationService.EXTRA_INCIDENT_ID)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
