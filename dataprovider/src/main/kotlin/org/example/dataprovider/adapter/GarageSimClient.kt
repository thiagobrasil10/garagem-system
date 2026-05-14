package org.example.dataprovider.adapter

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.core.domain.GarageConfig
import org.example.core.domain.Sector
import org.example.core.domain.Spot
import org.example.core.port.GarageConfigProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import java.time.Duration

@Component
class GarageSimClient(
    @Value("\${garage.sim.base-url:http://localhost:3000}") private val baseUrl: String,
) : GarageConfigProvider {

    private val log = LoggerFactory.getLogger(javaClass)

    private val webClient: WebClient = WebClient.builder()
        .baseUrl(baseUrl)
        .build()

    override fun fetchConfig(): GarageConfig {
        log.info("Buscando configuração da garagem em {}/garage", baseUrl)
        val response = webClient.get()
            .uri("/garage")
            .retrieve()
            .bodyToMono(GarageResponse::class.java)
            .block(Duration.ofSeconds(10))
            ?: error("Resposta vazia de /garage")

        return GarageConfig(
            sectors = response.garage.map {
                Sector(name = it.sector, basePrice = it.basePrice, maxCapacity = it.maxCapacity)
            },
            spots = response.spots.map {
                Spot(id = it.id, sector = it.sector, lat = it.lat, lng = it.lng)
            },
        )
    }

    private data class GarageResponse(
        val garage: List<SectorPayload> = emptyList(),
        val spots: List<SpotPayload> = emptyList(),
    )

    private data class SectorPayload(
        val sector: String = "",
        @JsonProperty("base_price") val basePrice: Double = 0.0,
        @JsonProperty("max_capacity") val maxCapacity: Int = 0,
    )

    private data class SpotPayload(
        val id: Long = 0,
        val sector: String = "",
        val lat: Double = 0.0,
        val lng: Double = 0.0,
    )
}

