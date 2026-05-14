package org.example.core.port

import org.example.core.domain.GarageConfig

/**
 * Porta de saída para consultar a configuração externa da garagem
 * (no MVP, o simulador HTTP).
 */
interface GarageConfigProvider {
    fun fetchConfig(): GarageConfig
}

