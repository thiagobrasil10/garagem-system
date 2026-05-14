package org.example.dataprovider.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(
    name = "parking_sessions",
    indexes = [
        Index(name = "ix_sessions_plate_open", columnList = "license_plate,exit_time"),
        Index(name = "ix_sessions_sector_exit", columnList = "sector_name,exit_time"),
    ],
)
class ParkingSessionEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "license_plate", nullable = false)
    var licensePlate: String = "",

    @Column(name = "entry_time", nullable = false)
    var entryTime: Instant = Instant.EPOCH,

    @Column(name = "price_multiplier", nullable = false)
    var priceMultiplier: Double = 1.0,

    @Column(name = "sector_name")
    var sectorName: String? = null,

    @Column(name = "spot_id")
    var spotId: Long? = null,

    @Column(name = "base_price")
    var basePrice: Double? = null,

    @Column(name = "parked_time")
    var parkedTime: Instant? = null,

    @Column(name = "exit_time")
    var exitTime: Instant? = null,

    @Column(name = "amount_charged")
    var amountCharged: Double? = null,
)

