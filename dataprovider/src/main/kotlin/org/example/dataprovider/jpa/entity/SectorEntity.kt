package org.example.dataprovider.jpa.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "sectors")
class SectorEntity(
    @Id
    var name: String = "",
    var basePrice: Double = 0.0,
    var maxCapacity: Int = 0,
)

