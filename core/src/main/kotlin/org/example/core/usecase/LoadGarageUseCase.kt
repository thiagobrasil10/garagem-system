package org.example.core.usecase

import org.example.core.port.GarageConfigProvider
import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository

/**
 * Carga inicial da garagem a partir do provider externo (simulador).
 *
 * Na inicialização:
 *  1. Remove sessões abertas de execuções anteriores (evita rejeição de ENTRY duplicado).
 *  2. Persiste setores e vagas com occupied=false (estado limpo).
 */
class LoadGarageUseCase(
    private val provider: GarageConfigProvider,
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository,
    private val sessionRepository: ParkingSessionRepository,
) {
    fun execute() {
        // Limpa estado residual de execuções anteriores
        sessionRepository.deleteAllOpen()

        val config = provider.fetchConfig()
        sectorRepository.saveAll(config.sectors)
        spotRepository.saveAll(config.spots)   // spots sempre com occupied=false
    }
}

