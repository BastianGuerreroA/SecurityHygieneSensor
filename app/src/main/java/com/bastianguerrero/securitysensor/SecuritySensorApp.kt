package com.bastianguerrero.securitysensor

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.util.Log
import com.bastianguerrero.securitysensor.telemetry.PerformanceTracker

class SecuritySensorApp : Application(), Application.ActivityLifecycleCallbacks {

    private var startedActivitiesCount = 0

    override fun onCreate() {
        super.onCreate()
        Log.i("SecuritySensorApp", "Inicializando aplicación SecurityHygieneSensor")
        // PerformanceTracker.onAppCreated() // Telemetría deshabilitada
        registerActivityLifecycleCallbacks(this)
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        if (startedActivitiesCount == 0) {
            // Log.d("SecuritySensorApp", "La aplicación entró a primer plano. Iniciando sesión de monitoreo.")
            // PerformanceTracker.startSession(applicationContext) // Telemetría deshabilitada
        }
        startedActivitiesCount++
    }

    override fun onActivityResumed(activity: Activity) {
        // PerformanceTracker.attachToActivity(activity) // Telemetría deshabilitada
    }

    override fun onActivityPaused(activity: Activity) {
        // PerformanceTracker.detachFromActivity(activity) // Telemetría deshabilitada
    }

    override fun onActivityStopped(activity: Activity) {
        startedActivitiesCount--
        if (startedActivitiesCount <= 0) {
            startedActivitiesCount = 0
            // Log.d("SecuritySensorApp", "La aplicación pasó a segundo plano o se cerró. Exportando métricas CSV...")
            // PerformanceTracker.stopAndExportSession(applicationContext) // Telemetría deshabilitada
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}
}
