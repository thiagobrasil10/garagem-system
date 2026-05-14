package org.example.core.usecase

import org.example.core.port.ParkingSessionRepository
import java.time.LocalDate

data class RevenueResult(
    val amount: Double,
    val currency: String = "BRL",
)

class GetRevenueUseCase(
    private val sessionRepository: ParkingSessionRepository,
) {
    fun execute(sector: String, date: LocalDate): RevenueResult {
        val amount = sessionRepository.sumRevenueBySectorAndExitDate(sector, date)
        return RevenueResult(amount = amount)
    }
}

