package org.example.core.usecase

import org.example.core.domain.ParkingSession
import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SpotRepository
import org.example.core.service.PricingService
import org.slf4j.LoggerFactory
import java.time.Instant

class HandleEntryUseCase(
    private val sessionRepository: ParkingSessionRepository,
    private val spotRepository: SpotRepository,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * Processa um evento ENTRY e retorna o resultado tipado.
     */
    fun execute(licensePlate: String, entryTime: Instant): EntryResult {
        val total = spotRepository.countTotal()
        val occupied = spotRepository.countOccupiedTotal()

        if (total > 0 && occupied >= total) {
            log.warn("Garagem lotada — entrada recusada para placa={}", licensePlate)
            return EntryResult.GarageFull
        }

        // Já existe sessão aberta? Ignora reentrada duplicada.
        sessionRepository.findOpenByPlate(licensePlate)?.let {
            log.warn("Já existe sessão aberta para placa={}, ignorando ENTRY duplicado", licensePlate)
            return EntryResult.AlreadyOpen(it)
        }

        val ratio = if (total == 0) 0.0 else occupied.toDouble() / total
        val multiplier = PricingService.multiplierForOccupancy(ratio)

        val session = sessionRepository.save(
            ParkingSession(
                licensePlate = licensePlate,
                entryTime = entryTime,
                priceMultiplier = multiplier,
            )
        )
        return EntryResult.Created(session)
    }
}

