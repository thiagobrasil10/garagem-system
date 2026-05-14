package org.example.dataprovider.jpa.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.Table

@Entity
@Table(
    name = "spots",
    indexes = [
        Index(name = "ix_spots_sector", columnList = "sector"),
        Index(name = "ix_spots_plate", columnList = "license_plate"),
        Index(name = "ix_spots_location", columnList = "lat,lng"),
    ],
)
class SpotEntity(
    @Id
    var id: Long = 0,
    var sector: String = "",
    var lat: Double = 0.0,
    var lng: Double = 0.0,
    var occupied: Boolean = false,
    @Column(name = "license_plate")
    var licensePlate: String? = null,
)

