package org.example.core.usecase

import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SpotRepository
import org.example.core.service.PricingService
import org.slf4j.LoggerFactory
import java.time.Instant

class HandleExitUseCase(
    private val sessionRepository: ParkingSessionRepository,
    private val spotRepository: SpotRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Processa um evento EXIT e retorna o resultado tipado.
     */
    fun execute(licensePlate: String, exitTime: Instant): ExitResult {
        val session = sessionRepository.findOpenByPlate(licensePlate)
        if (session == null) {
            log.warn("EXIT sem sessão aberta para placa={}", licensePlate)
            return ExitResult.NoOpenSession
        }

        val basePrice = session.basePrice ?: 0.0
        val amount = PricingService.calculateCharge(
            entry = session.entryTime,
            exit = exitTime,
            basePricePerHour = basePrice,
            multiplier = session.priceMultiplier,
        )

        sessionRepository.update(
            session.copy(exitTime = exitTime, amountCharged = amount)
        )
        session.spotId?.let { spotRepository.markFree(it) }
        return ExitResult.Charged(amount)
    }
}

