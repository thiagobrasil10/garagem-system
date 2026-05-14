package org.example.app.config

import org.example.core.port.GarageConfigProvider
import org.example.core.port.ParkingSessionRepository
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository
import org.example.core.usecase.GetRevenueUseCase
import org.example.core.usecase.HandleEntryUseCase
import org.example.core.usecase.HandleExitUseCase
import org.example.core.usecase.HandleParkedUseCase
import org.example.core.usecase.LoadGarageUseCase
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * Wiring dos use cases do core como beans Spring.
 * O core não conhece Spring — os adapters do dataprovider são injetados aqui.
 */
@Configuration
class UseCaseConfig {

    @Bean
    fun loadGarageUseCase(
        provider: GarageConfigProvider,
        sectorRepository: SectorRepository,
        spotRepository: SpotRepository,
    ) = LoadGarageUseCase(provider, sectorRepository, spotRepository)

    @Bean
    fun handleEntryUseCase(
        sessionRepository: ParkingSessionRepository,
        spotRepository: SpotRepository,
    ) = HandleEntryUseCase(sessionRepository, spotRepository)

    @Bean
    fun handleParkedUseCase(
        sessionRepository: ParkingSessionRepository,
        spotRepository: SpotRepository,
        sectorRepository: SectorRepository,
    ) = HandleParkedUseCase(sessionRepository, spotRepository, sectorRepository)

    @Bean
    fun handleExitUseCase(
        sessionRepository: ParkingSessionRepository,
        spotRepository: SpotRepository,
    ) = HandleExitUseCase(sessionRepository, spotRepository)

    @Bean
    fun getRevenueUseCase(
        sessionRepository: ParkingSessionRepository,
    ) = GetRevenueUseCase(sessionRepository)
}

