package org.example.dataprovider.adapter

import org.example.core.domain.Sector
import org.example.core.port.SectorRepository
import org.example.dataprovider.jpa.entity.SectorEntity
import org.example.dataprovider.jpa.repository.SectorJpaRepository
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class SectorRepositoryAdapter(
    private val jpa: SectorJpaRepository,
) : SectorRepository {

    @Transactional
    override fun saveAll(sectors: List<Sector>) {
        jpa.saveAll(sectors.map { SectorEntity(it.name, it.basePrice, it.maxCapacity) })
    }

    @Transactional(readOnly = true)
    override fun findByName(name: String): Sector? =
        jpa.findById(name).orElse(null)?.toDomain()

    @Transactional(readOnly = true)
    override fun findAll(): List<Sector> = jpa.findAll().map { it.toDomain() }

    private fun SectorEntity.toDomain() = Sector(name, basePrice, maxCapacity)
}

