package com.bastianguerrero.securitysensor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.bastianguerrero.securitysensor.telemetry.PerformanceTracker

/**
 * Clase principal de aplicación de SecurityHygieneSensor.
 *
 * Implementa [Application.ActivityLifecycleCallbacks] para monitorear las transiciones
 * de la aplicación entre primer y segundo plano mediante el conteo de actividades activas.
 * Permite además la conexión modular con el subsistema de telemetría [PerformanceTracker].
 */
class SecuritySensorApp : Application(), Application.ActivityLifecycleCallbacks {

    private var startedActivitiesCount = 0

    override fun onCreate() {
        super.onCreate()
        Log.i("SecuritySensorApp", "Inicializando aplicación SecurityHygieneSensor")
        // PerformanceTracker.onAppCreated() // Telemetría deshabilitada temporalmente
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (startedActivitiesCount == 0) {
            // PerformanceTracker.startSession(applicationContext) // Telemetría deshabilitada temporalmente
        }
        startedActivitiesCount++
    }

    override fun onActivityResumed(activity: Activity) {
        // PerformanceTracker.attachToActivity(activity) // Telemetría deshabilitada temporalmente
    }

    override fun onActivityPaused(activity: Activity) {
        // PerformanceTracker.detachFromActivity(activity) // Telemetría deshabilitada temporalmente
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivitiesCount--
        if (startedActivitiesCount <= 0) {
            startedActivitiesCount = 0
            // PerformanceTracker.stopAndExportSession(applicationContext) // Telemetría deshabilitada temporalmente
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
