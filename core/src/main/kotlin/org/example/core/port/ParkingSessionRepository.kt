package org.example.core.port

import org.example.core.domain.ParkingSession
import java.time.LocalDate

interface ParkingSessionRepository {
    fun save(session: ParkingSession): ParkingSession
    fun findOpenByPlate(plate: String): ParkingSession?
    fun update(session: ParkingSession): ParkingSession
    fun sumRevenueBySectorAndExitDate(sector: String, date: LocalDate): Double
}

