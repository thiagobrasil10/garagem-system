package org.example.dataprovider.jpa.repository

import jakarta.persistence.LockModeType
import org.example.dataprovider.jpa.entity.SpotEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SpotJpaRepository : JpaRepository<SpotEntity, Long> {

    fun findByLatAndLng(lat: Double, lng: Double): SpotEntity?

    fun findByLicensePlate(plate: String): SpotEntity?

    fun countBySectorAndOccupiedTrue(sector: String): Int

    @Query("select count(s) from SpotEntity s where s.occupied = true")
    fun countOccupied(): Int

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SpotEntity s where s.id = :id")
    fun lockById(@Param("id") id: Long): SpotEntity?
}

