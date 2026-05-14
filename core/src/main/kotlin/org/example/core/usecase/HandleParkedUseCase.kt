package org.example.core.usecase

import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository
import org.slf4j.LoggerFactory
import java.time.Clock
import java.time.Instant

class HandleParkedUseCase(
    private val sessionRepository: ParkingSessionRepository,
    private val spotRepository: SpotRepository,
    private val sectorRepository: SectorRepository,
    private val clock: Clock = Clock.systemUTC(),
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun execute(licensePlate: String, lat: Double, lng: Double): ParkedResult {
        val session = sessionRepository.findOpenByPlate(licensePlate)
        if (session == null) {
            log.warn("PARKED sem ENTRY prévio para placa={}", licensePlate)
            return ParkedResult.NoOpenSession
        }

        val spot = spotRepository.findByLocation(lat, lng)
        if (spot == null) {
            log.warn("Vaga não encontrada para lat={}, lng={}", lat, lng)
            return ParkedResult.SpotNotFound(lat, lng)
        }
        if (spot.occupied) {
            log.warn("Vaga {} já ocupada", spot.id)
            return ParkedResult.SpotAlreadyOccupied(spot.id)
        }

        val sector = sectorRepository.findByName(spot.sector)
            ?: error("Setor ${spot.sector} não encontrado")

        // Lotação 100% no setor? Recusa.
        val occupiedInSector = spotRepository.countOccupiedBySector(sector.name)
        if (occupiedInSector >= sector.maxCapacity) {
            log.warn("Setor {} lotado, recusando PARKED de {}", sector.name, licensePlate)
            return ParkedResult.SectorFull(sector.name)
        }

        spotRepository.markOccupied(spot.id, licensePlate)

        sessionRepository.update(
            session.copy(
                sectorName = sector.name,
                spotId = spot.id,
                basePrice = sector.basePrice,
                parkedTime = Instant.now(clock),
            )
        )
        return ParkedResult.Parked(spotId = spot.id, sector = sector.name)
    }
}

