package org.example.core.port

import org.example.core.domain.Sector

interface SectorRepository {
    fun saveAll(sectors: List<Sector>)
    fun findByName(name: String): Sector?
    fun findAll(): List<Sector>
}

