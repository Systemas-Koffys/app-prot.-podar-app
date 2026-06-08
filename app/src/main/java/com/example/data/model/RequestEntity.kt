package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "pruning_requests")
data class RequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val nombreSolicitante: String,
    val telefonoSolicitante: String,
    val distrito: String,
    val barrio: String,
    val especie: String,
    val urgencia: String, // "Baja", "Media", "Alta", "Crítica"
    val tecnicoAsignado: String,
    val estado: String, // "Pendiente", "En Proceso", "Terminado"
    val requerimientos: String, // e.g. "Camión elevador", "Ficha técnica", etc.
    val latitud: Double,
    val longitud: Double,
    val detalles: String,
    val fechaAgendada: String, // YYYY-MM-DD
    val fotos: String, // Comma-separated dummy values or URIs
    val fechaRegistro: Long = System.currentTimeMillis()
) : Serializable
