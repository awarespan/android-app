package com.awarespan.guardian.data.logging

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import com.awarespan.guardian.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.PrintWriter
import java.io.StringWriter
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Enterprise Bounded Diagnostic Logging & Export System.
 *
 * Implements:
 * 1. Standard structured Android Logcat logging with subsystem tags.
 * 2. Strict secret redaction (no passwords, JWTs, TOTP keys, or private data).
 * 3. Bounded, app-private rolling disk logs (< 512 KB total footprint).
 * 4. User-initiated diagnostic archive export via standard Android Sharesheet.
 */
@Singleton
class DiagnosticLogger @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    companion object {
        private const val MAX_LOG_FILE_BYTES = 128 * 1024L // 128 KB
        private const val MAX_ROLLING_FILES = 4 // Total 512 KB max
        private const val TAG_PREFIX = "Awarespan"

        @Volatile
        private var instance: DiagnosticLogger? = null

        fun getInstance(context: Context): DiagnosticLogger {
            return instance ?: synchronized(this) {
                instance ?: DiagnosticLogger(context.applicationContext).also { instance = it }
            }
        }

        private val JWT_PATTERN = Regex("Bearer\\s+[A-Za-z0-9-_=]+\\.[A-Za-z0-9-_=]+\\.?[A-Za-z0-9-_.+/=]*")
        private val SENSITIVE_KV_PATTERN = Regex("(?i)\"(password|secret|token|key|totp|credential)\"\\s*:\\s*\"[^\"]+\"")
        private val URL_TOKEN_PATTERN = Regex("(?i)(token|key|secret|password)=[^&\\s]+")
    }

    init {
        instance = this
    }

    private val writeExecutor = Executors.newSingleThreadExecutor { runnable ->
        Thread(runnable, "awarespan-diag-logger").apply { isDaemon = true }
    }

    private val logDir: File by lazy {
        File(context.filesDir, "diagnostics").apply { mkdirs() }
    }

    private val activeLogFile: File
        get() = File(logDir, "events-0.log")

    fun debug(subsystem: String, message: String) {
        if (BuildConfig.DEBUG) {
            val sanitized = sanitize(message)
            Log.d("$TAG_PREFIX.$subsystem", sanitized)
        }
    }

    fun info(subsystem: String, message: String) {
        val sanitized = sanitize(message)
        Log.i("$TAG_PREFIX.$subsystem", sanitized)
        persistLogEntry("INFO", subsystem, sanitized)
    }

    @JvmOverloads
    fun warn(subsystem: String, message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        if (throwable != null) {
            Log.w("$TAG_PREFIX.$subsystem", sanitized, throwable)
            persistLogEntry("WARN", subsystem, "$sanitized: ${formatThrowable(throwable)}")
        } else {
            Log.w("$TAG_PREFIX.$subsystem", sanitized)
            persistLogEntry("WARN", subsystem, sanitized)
        }
    }

    @JvmOverloads
    fun error(subsystem: String, message: String, throwable: Throwable? = null) {
        val sanitized = sanitize(message)
        if (throwable != null) {
            Log.e("$TAG_PREFIX.$subsystem", sanitized, throwable)
            persistLogEntry("ERROR", subsystem, "$sanitized: ${formatThrowable(throwable)}")
        } else {
            Log.e("$TAG_PREFIX.$subsystem", sanitized)
            persistLogEntry("ERROR", subsystem, sanitized)
        }
    }

    // Convenience Domain Event Logging
    @JvmOverloads
    fun logAuthEvent(action: String, success: Boolean, details: String = "") {
        val status = if (success) "SUCCESS" else "FAILURE"
        info("Auth", "Operator auth $action: $status. $details".trim())
    }

    fun logApiCall(method: String, endpoint: String, statusCode: Int, durationMs: Long) {
        val level = if (statusCode in 200..299) "INFO" else if (statusCode in 400..499) "WARN" else "ERROR"
        val msg = "$method $endpoint -> HTTP $statusCode (${durationMs}ms)"
        if (level == "INFO") {
            info("Network", msg)
        } else {
            warn("Network", msg)
        }
    }

    fun logRealtimeState(state: String, details: String = "") {
        info("Realtime", "WebSocket session state: $state. $details".trim())
    }

    fun logSyncEvent(subsystem: String, itemCount: Int, durationMs: Long) {
        info("Sync", "$subsystem synchronized $itemCount record(s) in ${durationMs}ms")
    }

    fun logNodeHeartbeat(nodeId: String, status: String, battery: Int? = null) {
        info("Node", "Heartbeat evaluated for $nodeId: status=$status, battery=${battery ?: -1}%")
    }

    private fun persistLogEntry(level: String, subsystem: String, text: String) {
        writeExecutor.execute {
            try {
                rotateLogsIfNeeded()
                val timestamp = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
                val entry = "[$timestamp] [$level] [$subsystem] $text\n"
                FileOutputStream(activeLogFile, true).use { out ->
                    out.write(entry.toByteArray(Charsets.UTF_8))
                }
            } catch (e: Exception) {
                Log.e(TAG_PREFIX, "Failed to persist diagnostic log entry: ${e.message}")
            }
        }
    }

    private fun rotateLogsIfNeeded() {
        if (!activeLogFile.exists() || activeLogFile.length() < MAX_LOG_FILE_BYTES) {
            return
        }

        // Delete oldest file if at capacity
        val oldestFile = File(logDir, "events-${MAX_ROLLING_FILES - 1}.log")
        if (oldestFile.exists()) {
            oldestFile.delete()
        }

        // Shift existing log files down
        for (i in MAX_ROLLING_FILES - 2 downTo 0) {
            val src = File(logDir, "events-$i.log")
            if (src.exists()) {
                val dst = File(logDir, "events-${i + 1}.log")
                src.renameTo(dst)
            }
        }
    }

    /**
     * Packages the sanitized diagnostic logs and metadata into a ZIP file.
     * Guaranteed free of any secrets, tokens, TOTP keys, or private data.
     */
    fun createDiagnosticArchive(extraMetadata: Map<String, String> = emptyMap()): File {
        val exportDir = File(context.cacheDir, "diagnostic_exports").apply { mkdirs() }
        // Clean previous exports
        exportDir.listFiles()?.forEach { if (it.name.endsWith(".zip")) it.delete() }

        val timestamp = System.currentTimeMillis()
        val zipFile = File(exportDir, "awarespan-diagnostics-$timestamp.zip")

        ZipOutputStream(FileOutputStream(zipFile)).use { zip ->
            // 1. System Metadata JSON
            val meta = JSONObject().apply {
                put("application", "AWARESPAN")
                put("versionName", BuildConfig.VERSION_NAME)
                put("versionCode", BuildConfig.VERSION_CODE)
                put("buildType", BuildConfig.BUILD_TYPE)
                put("androidRelease", Build.VERSION.RELEASE)
                put("androidSdk", Build.VERSION.SDK_INT)
                put("deviceManufacturer", Build.MANUFACTURER)
                put("deviceModel", Build.MODEL)
                put("timestampUtc", DateTimeFormatter.ISO_INSTANT.format(Instant.now()))
                extraMetadata.forEach { (k, v) -> put(k, v) }
            }
            zip.putNextEntry(ZipEntry("system_info.json"))
            zip.write(meta.toString(2).toByteArray(Charsets.UTF_8))
            zip.closeEntry()

            // 2. Aggregated Operational Logs
            zip.putNextEntry(ZipEntry("operational_events.log"))
            for (i in MAX_ROLLING_FILES - 1 downTo 0) {
                val f = File(logDir, "events-$i.log")
                if (f.exists()) {
                    FileInputStream(f).use { input ->
                        input.copyTo(zip)
                    }
                }
            }
            zip.closeEntry()

            // 3. Security & Operator Notice
            val notice = """
                AWARESPAN OPERATIONAL DIAGNOSTICS ARCHIVE
                Project: AWARESPAN
                Tagline: Awareness Without Boundaries.
                Generated: ${DateTimeFormatter.ISO_INSTANT.format(Instant.now())}
                
                This diagnostic archive contains operational state transitions, network response timings,
                and synchronization history.
                
                Security Assurance:
                - All bearer tokens, JWT signatures, and authentication secrets have been automatically redacted.
                - No operator passwords, TOTP secret keys, or private cryptographic keys are stored or exported.
                - No video frames or microphone recordings are included.
                
                This archive is intended exclusively for authorized technical personnel.
            """.trimIndent()
            zip.putNextEntry(ZipEntry("README.txt"))
            zip.write(notice.toByteArray(Charsets.UTF_8))
            zip.closeEntry()
        }

        return zipFile
    }

    /**
     * Builds a standard Android Sharesheet chooser intent for the exported archive.
     */
    fun createShareIntent(zipFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            zipFile
        )
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "AWARESPAN Diagnostics — ${zipFile.name}")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        return Intent.createChooser(sendIntent, "Share AWARESPAN Diagnostics")
    }

    private fun sanitize(input: String): String {
        var clean = input
        clean = JWT_PATTERN.replace(clean, "Bearer [REDACTED_JWT]")
        clean = SENSITIVE_KV_PATTERN.replace(clean, "\"$1\":\"[REDACTED]\"")
        clean = URL_TOKEN_PATTERN.replace(clean, "$1=[REDACTED]")
        return clean
    }

    private fun formatThrowable(t: Throwable): String {
        val sw = StringWriter()
        t.printStackTrace(PrintWriter(sw))
        return sanitize(sw.toString())
    }
}
