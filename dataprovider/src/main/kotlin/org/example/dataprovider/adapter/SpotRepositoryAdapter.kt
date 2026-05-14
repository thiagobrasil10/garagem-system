package org.example.dataprovider.adapter

import org.example.core.domain.Spot
import org.example.core.port.SpotRepository
import org.example.dataprovider.jpa.entity.SpotEntity
import org.example.dataprovider.jpa.repository.SpotJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SpotRepositoryAdapter(
    private val jpa: SpotJpaRepository,
) : SpotRepository {

    @Transactional
    override fun saveAll(spots: List<Spot>) {
        jpa.saveAll(
            spots.map {
                SpotEntity(
                    id = it.id,
                    sector = it.sector,
                    lat = it.lat,
                    lng = it.lng,
                    occupied = it.occupied,
                    licensePlate = it.licensePlate,
                )
            }
        )
    }

    @Transactional(readOnly = true)
    override fun findById(id: Long): Spot? = jpa.findById(id).orElse(null)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAll(): List<Spot> = jpa.findAll().map { it.toDomain() }

    @Transactional(readOnly = true)
    override fun findByLocation(lat: Double, lng: Double): Spot? =
        jpa.findByLatAndLng(lat, lng)?.toDomain()

    @Transactional(readOnly = true)
    override fun findByLicensePlate(plate: String): Spot? =
        jpa.findByLicensePlate(plate)?.toDomain()

    @Transactional(readOnly = true)
    override fun countOccupiedBySector(sector: String): Int =
        jpa.countBySectorAndOccupiedTrue(sector)

    @Transactional(readOnly = true)
    override fun countTotal(): Int = jpa.count().toInt()

    @Transactional(readOnly = true)
    override fun countOccupiedTotal(): Int = jpa.countOccupied()

    @Transactional
    override fun markOccupied(spotId: Long, plate: String) {
        val spot = jpa.lockById(spotId) ?: error("Vaga $spotId não encontrada")
        spot.occupied = true
        spot.licensePlate = plate
        jpa.save(spot)
    }

    @Transactional
    override fun markFree(spotId: Long) {
        val spot = jpa.lockById(spotId) ?: return
        spot.occupied = false
        spot.licensePlate = null
        jpa.save(spot)
    }

    private fun SpotEntity.toDomain() = Spot(id, sector, lat, lng, occupied, licensePlate)
}

