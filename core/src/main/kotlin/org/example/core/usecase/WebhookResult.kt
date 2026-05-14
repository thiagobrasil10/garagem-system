package org.example.core.usecase

import org.example.core.domain.ParkingSession

/**
 * Resultado de processar um evento ENTRY.
 */
sealed class EntryResult {
    data class Created(val session: ParkingSession) : EntryResult()
    data class AlreadyOpen(val session: ParkingSession) : EntryResult()
    object GarageFull : EntryResult()
}

/**
 * Resultado de processar um evento PARKED.
 */
sealed class ParkedResult {
    data class Parked(val spotId: Long, val sector: String) : ParkedResult()
    object NoOpenSession : ParkedResult()
    data class SpotNotFound(val lat: Double, val lng: Double) : ParkedResult()
    data class SpotAlreadyOccupied(val spotId: Long) : ParkedResult()
    data class SectorFull(val sector: String) : ParkedResult()
}

/**
 * Resultado de processar um evento EXIT.
 */
sealed class ExitResult {
    data class Charged(val amount: Double) : ExitResult()
    object NoOpenSession : ExitResult()
}

