package com.bastianguerrero.securitysensor.data.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

/**
 * Carga útil para la ingestión de datos a través del webhook de sensores de LSG.
 *
 * @property player_id Identificador del jugador.
 * @property sensor_endpoint_id Identificador del tipo de endpoint (ej. 3).
 * @property players_sensor_endpoint_id Identificador de vinculación dinámica entre el jugador y el endpoint.
 * @property raw_payload Diccionario con los detalles técnicos específicos del dispositivo recolectados durante el escaneo.
 * @property parsed_value Valor numérico principal procesado (puntaje obtenido).
 * @property status Estado reportado del evento (ej. "OK").
 * @property error_message Mensaje de error en caso de fallo durante el procesamiento en el cliente, o null.
 * @property occurred_at Marca de tiempo ISO-8601 del momento en que ocurrió la captura.
 */
@Keep
data class SensorIngestRequest(
    @SerializedName("player_id") val player_id: Int,
    @SerializedName("sensor_endpoint_id") val sensor_endpoint_id: Int,
    @SerializedName("players_sensor_endpoint_id") val players_sensor_endpoint_id: Int,
    @SerializedName("raw_payload") val raw_payload: Map<String, Any>,
    @SerializedName("parsed_value") val parsed_value: Double,
    @SerializedName("status") val status: String,
    @SerializedName("error_message") val error_message: String?,
    @SerializedName("occurred_at") val occurred_at: String
)