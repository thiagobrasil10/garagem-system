package org.example.core.port

import org.example.core.domain.Spot

interface SpotRepository {
    fun saveAll(spots: List<Spot>)
    fun findAll(): List<Spot>
    fun findById(id: Long): Spot?
    fun findByLocation(lat: Double, lng: Double): Spot?
    fun findByLicensePlate(plate: String): Spot?
    fun countOccupiedBySector(sector: String): Int
    fun countTotal(): Int
    fun countOccupiedTotal(): Int
    fun markOccupied(spotId: Long, plate: String)
    fun markFree(spotId: Long)
}

