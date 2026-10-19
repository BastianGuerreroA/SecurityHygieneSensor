package com.bastianguerrero.securitysensor.telemetry

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.util.Locale

/**
 * Estructura de datos que consolida las métricas de rendimiento recolectadas durante una sesión.
 *
 * @property timestamp Fecha y hora de captura de la sesión.
 * @property initialLoadTimeSec Tiempo transcurrido hasta completar la carga inicial de la aplicación.
 * @property cpuAvgPercent Porcentaje promedio de uso de CPU durante la sesión.
 * @property fpsAvg Promedio de cuadros por segundo (FPS) registrados.
 * @property fpsMin Valor mínimo de FPS observado.
 * @property fpsMax Valor máximo de FPS observado.
 * @property memAvgMb Consumo promedio de memoria RAM en megabytes (Heap + Native).
 * @property memMaxMb Pico máximo de memoria RAM consumida en megabytes.
 * @property batteryConsumedMah Estimación de consumo de batería en miliamperios-hora (mAh).
 * @property batteryStartPct Porcentaje de batería al inicio de la sesión.
 * @property batteryEndPct Porcentaje de batería al finalizar la sesión.
 * @property sessionDurationSec Duración total de la sesión en segundos.
 */
data class PerformanceSummary(
    val timestamp: String,
    val initialLoadTimeSec: Float,
    val cpuAvgPercent: Float,
    val fpsAvg: Float,
    val fpsMin: Int,
    val fpsMax: Int,
    val memAvgMb: Float,
    val memMaxMb: Float,
    val batteryConsumedMah: Float,
    val batteryStartPct: Float,
    val batteryEndPct: Float,
    val sessionDurationSec: Float
)

/**
 * Utilidad responsable de persistir las sesiones de telemetría de rendimiento en formato CSV
 * en el almacenamiento interno de la aplicación.
 */
object CsvExporter {
    private const val TAG = "CsvExporter"
    private const val CSV_FILENAME = "performance_metrics.csv"

    /**
     * Escribe un bloque con el resumen de métricas de la sesión al final del archivo CSV.
     *
     * @param context Contexto de la aplicación para acceder al directorio interno de archivos.
     * @param summary Resumen con los datos recolectados por [PerformanceTracker].
     * @return El archivo [File] generado o modificado, o `null` si ocurrió un error de entrada/salida.
     */
    fun saveSessionSummary(context: Context, summary: PerformanceSummary): File? {
        return try {
            val docsDir = context.filesDir
            if (!docsDir.exists()) {
                docsDir.mkdirs()
            }

            val csvFile = File(docsDir, CSV_FILENAME)

            FileWriter(csvFile, true).use { writer ->
                writer.append("--- SESION DE RENDIMIENTO ---\n")
                writer.append(String.format(Locale.US, "Fecha_Hora,%s\n", summary.timestamp))
                writer.append(String.format(Locale.US, "Carga_Inicial_s,%.3f\n", summary.initialLoadTimeSec))
                writer.append(String.format(Locale.US, "CPU_Prom_Pct,%.2f%%\n", summary.cpuAvgPercent))
                writer.append(String.format(Locale.US, "FPS_Prom,%.1f\n", summary.fpsAvg))
                writer.append(String.format(Locale.US, "FPS_Min,%d\n", summary.fpsMin))
                writer.append(String.format(Locale.US, "FPS_Max,%d\n", summary.fpsMax))
                writer.append(String.format(Locale.US, "Mem_Prom_MB,%.2f MB\n", summary.memAvgMb))
                writer.append(String.format(Locale.US, "Mem_Max_MB,%.2f MB\n", summary.memMaxMb))
                writer.append(String.format(Locale.US, "Consumo_Bateria_mAh,%.3f mAh\n", summary.batteryConsumedMah))
                writer.append(String.format(Locale.US, "Bateria_Inicial_Pct,%.1f%%\n", summary.batteryStartPct))
                writer.append(String.format(Locale.US, "Bateria_Final_Pct,%.1f%%\n", summary.batteryEndPct))
                writer.append(String.format(Locale.US, "Duracion_Sesion_s,%.2f s\n\n", summary.sessionDurationSec))
            }

            Log.i(TAG, "Métricas de rendimiento guardadas exitosamente en: ${csvFile.absolutePath}")
            csvFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar reporte CSV de rendimiento", e)
            null
        }
    }

    /**
     * Retorna la referencia al archivo CSV de métricas en el almacenamiento interno de la aplicación.
     *
     * @param context Contexto de la aplicación.
     * @return Objeto [File] correspondiente al archivo CSV.
     */
    fun getCsvFile(context: Context): File {
        val docsDir = context.filesDir
        return File(docsDir, CSV_FILENAME)
    }
}
