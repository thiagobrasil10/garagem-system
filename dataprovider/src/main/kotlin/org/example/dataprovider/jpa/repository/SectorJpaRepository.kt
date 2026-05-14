package org.example.dataprovider.jpa.repository

import org.example.dataprovider.jpa.entity.SectorEntity
import org.springframework.data.jpa.repository.JpaRepository

interface SectorJpaRepository : JpaRepository<SectorEntity, String>

