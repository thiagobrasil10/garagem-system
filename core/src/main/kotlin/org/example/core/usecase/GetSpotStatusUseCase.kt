package org.example.core.usecase

import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SpotRepository
import java.time.Instant

/**
 * Status atual de uma vaga consultada por (lat, lng).
 */
data class SpotStatus(
    val occupied: Boolean,
    val entryTime: Instant?,
    val timeParked: Instant?,
)

class GetSpotStatusUseCase(
    private val spotRepository: SpotRepository,
    private val sessionRepository: ParkingSessionRepository,
) {
    fun execute(lat: Double, lng: Double): SpotStatus? {
        val spot = spotRepository.findByLocation(lat, lng) ?: return null
        if (!spot.occupied || spot.licensePlate == null) {
            return SpotStatus(occupied = false, entryTime = null, timeParked = null)
        }
        val session = sessionRepository.findOpenByPlate(spot.licensePlate)
        return SpotStatus(
            occupied = true,
            entryTime = session?.entryTime,
            timeParked = session?.parkedTime,
        )
    }
}

