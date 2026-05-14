package org.example.dataprovider.jpa.repository

import org.example.dataprovider.jpa.entity.ParkingSessionEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant

interface ParkingSessionJpaRepository : JpaRepository<ParkingSessionEntity, Long> {

    fun findFirstByLicensePlateAndExitTimeIsNullOrderByEntryTimeDesc(
        plate: String,
    ): ParkingSessionEntity?

    fun deleteAllByExitTimeIsNull()

    @Query(
        """
        select coalesce(sum(p.amountCharged), 0.0)
          from ParkingSessionEntity p
         where p.sectorName = :sector
           and p.exitTime >= :start
           and p.exitTime <  :end
        """
    )
    fun sumRevenue(
        @Param("sector") sector: String,
        @Param("start") start: Instant,
        @Param("end") end: Instant,
    ): Double
}

