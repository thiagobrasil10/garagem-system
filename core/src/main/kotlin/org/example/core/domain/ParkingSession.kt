package org.example.core.domain

import java.time.Instant

/**
 * Sessão de estacionamento de um veículo. Vai do ENTRY ao EXIT.
 *
 *  - Ao receber ENTRY: cria a sessão com [entryTime] e o [priceMultiplier]
 *    calculado a partir da lotação total da garagem.
 *  - Ao receber PARKED: vincula [sectorName], [spotId], [basePrice] e [parkedTime].
 *  - Ao receber EXIT: preenche [exitTime] e [amountCharged].
 */
data class ParkingSession(
    val id: Long? = null,
    val licensePlate: String,
    val entryTime: Instant,
    val priceMultiplier: Double,
    val sectorName: String? = null,
    val spotId: Long? = null,
    val basePrice: Double? = null,
    val parkedTime: Instant? = null,
    val exitTime: Instant? = null,
    val amountCharged: Double? = null,
)

