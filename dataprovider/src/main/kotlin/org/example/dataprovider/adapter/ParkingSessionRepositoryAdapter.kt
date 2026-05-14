package org.example.dataprovider.adapter

import org.example.core.domain.ParkingSession
import org.example.core.port.ParkingSessionRepository
import org.example.dataprovider.jpa.entity.ParkingSessionEntity
import org.example.dataprovider.jpa.repository.ParkingSessionJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneOffset

@Component
class ParkingSessionRepositoryAdapter(
    private val jpa: ParkingSessionJpaRepository,
) : ParkingSessionRepository {

    @Transactional
    override fun save(session: ParkingSession): ParkingSession =
        jpa.save(session.toEntity()).toDomain()

    @Transactional(readOnly = true)
    override fun findOpenByPlate(plate: String): ParkingSession? =
        jpa.findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(plate)?.toDomain()

    @Transactional
    override fun update(session: ParkingSession): ParkingSession {
        val id = session.id ?: error("ParkingSession sem id não pode ser atualizada")
        val entity = jpa.findById(id).orElseThrow { IllegalStateException("Sessão $id não encontrada") }
        entity.licensePlate = session.licensePlate
        entity.entryTime = session.entryTime
        entity.priceMultiplier = session.priceMultiplier
        entity.sectorName = session.sectorName
        entity.spotId = session.spotId
        entity.basePrice = session.basePrice
        entity.parkedTime = session.parkedTime
        entity.exitTime = session.exitTime
        entity.amountCharged = session.amountCharged
        return jpa.save(entity).toDomain()
    }

    @Transactional(readOnly = true)
    override fun sumRevenueBySectorAndExitDate(sector: String, date: LocalDate): Double {
        val start = date.atStartOfDay().toInstant(ZoneOffset.UTC)
        val end = date.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC)
        return jpa.sumRevenue(sector, start, end)
    }

    private fun ParkingSession.toEntity() = ParkingSessionEntity(
        id = id,
        licensePlate = licensePlate,
        entryTime = entryTime,
        priceMultiplier = priceMultiplier,
        sectorName = sectorName,
        spotId = spotId,
        basePrice = basePrice,
        parkedTime = parkedTime,
        exitTime = exitTime,
        amountCharged = amountCharged,
    )

    private fun ParkingSessionEntity.toDomain() = ParkingSession(
        id = id,
        licensePlate = licensePlate,
        entryTime = entryTime,
        priceMultiplier = priceMultiplier,
        sectorName = sectorName,
        spotId = spotId,
        basePrice = basePrice,
        parkedTime = parkedTime,
        exitTime = exitTime,
        amountCharged = amountCharged,
    )
}

