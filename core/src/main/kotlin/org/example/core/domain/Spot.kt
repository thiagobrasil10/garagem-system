package org.example.core.domain

data class Spot(
    val id: Long,
    val sector: String,
    val lat: Double,
    val lng: Double,
    val occupied: Boolean = false,
    val licensePlate: String? = null,
)

