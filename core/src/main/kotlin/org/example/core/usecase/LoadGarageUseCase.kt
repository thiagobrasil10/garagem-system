package org.example.core.usecase

import org.example.core.port.GarageConfigProvider
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository

/**
 * Carga inicial da garagem a partir do provider externo (simulador).
 */
class LoadGarageUseCase(
    private val provider: GarageConfigProvider,
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository,
) {
    fun execute() {
        val config = provider.fetchConfig()
        sectorRepository.saveAll(config.sectors)
        spotRepository.saveAll(config.spots)
    }
}

