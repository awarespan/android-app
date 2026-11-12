package com.awarespan.guardian;

import android.app.Application;

import com.awarespan.guardian.data.logging.DiagnosticLogger;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class AwarespanApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DiagnosticLogger logger = DiagnosticLogger.Companion.getInstance(this);
        logger.info("Lifecycle", "AWARESPAN Application initialized (version " +
                BuildConfig.VERSION_NAME + ", build " + BuildConfig.VERSION_CODE + ")");

        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.error("Crash", "Uncaught exception on thread " + thread.getName(), throwable);
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable);
            }
        });
    }
}
