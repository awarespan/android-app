package com.awarespan.guardian.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.awarespan.guardian.data.local.dao.IncidentDao
import com.awarespan.guardian.data.mapper.IncidentMapper
import com.awarespan.guardian.data.remote.api.SipApiService
import com.awarespan.guardian.domain.repository.AuthRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * Periodic background synchronization worker that refreshes telemetry and
 * generates dismissible notifications for any newly detected threats.
 * Operates without maintaining a persistent foreground service.
 */
class IncidentSyncWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SyncEntryPoint {
        fun api(): SipApiService
        fun incidentDao(): IncidentDao
        fun incidentMapper(): IncidentMapper
        fun authRepository(): AuthRepository
        fun diagnosticLogger(): com.awarespan.guardian.data.logging.DiagnosticLogger
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            SyncEntryPoint::class.java
        )

        val logger = entryPoint.diagnosticLogger()
        val auth = entryPoint.authRepository()
        if (auth.currentUser() == null) {
            return@withContext Result.success()
        }

        val start = System.currentTimeMillis()
        try {
            val response = entryPoint.api().getIncidents(0, 10, null, null, null, null, null).execute()
            if (response.isSuccessful && response.body() != null) {
                val dtos = response.body()?.content ?: emptyList()
                logger.logSyncEvent("IncidentSyncWorker", dtos.size, System.currentTimeMillis() - start)
                if (dtos.isNotEmpty()) {
                    val entities = entryPoint.incidentMapper().toEntities(dtos)
                    entryPoint.incidentDao().upsertAll(entities)

                    val prefs = applicationContext.getSharedPreferences("awarespan_sync", Context.MODE_PRIVATE)
                    val lastNotifiedMs = prefs.getLong("last_notified_epoch_ms", 0L)
                    var maxSeenMs = lastNotifiedMs

                    for (dto in dtos) {
                        val createdMs = IncidentMapper.toEpochMs(dto.createdAt)
                        val isPending = "PENDING_VERIFICATION".equals(dto.state, true) ||
                                "DETECTED".equals(dto.state, true)
                        if (isPending && createdMs > lastNotifiedMs) {
                            val threatType = dto.threat?.type ?: dto.threatType ?: "UNKNOWN"
                            val description = dto.threat?.description ?: dto.threatDescription ?: ""
                            NotificationService.notifyNewIncident(
                                applicationContext,
                                dto.id,
                                threatType,
                                description
                            )
                            if (createdMs > maxSeenMs) {
                                maxSeenMs = createdMs
                            }
                        }
                    }

                    if (maxSeenMs > lastNotifiedMs) {
                        prefs.edit().putLong("last_notified_epoch_ms", maxSeenMs).apply()
                    }
                }
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_WORK_NAME = "AwarespanIncidentSync"

        fun schedule(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<IncidentSyncWorker>(15, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
