package org.example.core.usecase

import org.example.core.port.ParkingSessionRepository
import org.example.core.service.PricingService
import java.time.Clock
import java.time.Instant

/**
 * Status atual de uma placa que está dentro da garagem.
 */
data class PlateStatus(
    val licensePlate: String,
    val priceUntilNow: Double,
    val entryTime: Instant,
    val timeParked: Instant?,
)

class GetPlateStatusUseCase(
    private val sessionRepository: ParkingSessionRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    fun execute(licensePlate: String): PlateStatus? {
        val session = sessionRepository.findOpenByPlate(licensePlate) ?: return null
        val now = Instant.now(clock)
        val price = if (session.basePrice != null) {
            PricingService.calculateCharge(
                entry = session.entryTime,
                exit = now,
                basePricePerHour = session.basePrice,
                multiplier = session.priceMultiplier,
            )
        } else 0.0
        return PlateStatus(
            licensePlate = session.licensePlate,
            priceUntilNow = price,
            entryTime = session.entryTime,
            timeParked = session.parkedTime,
        )
    }
}

