package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * Expõe a configuração atual da garagem (setores e vagas)
 * persistida no banco a partir do simulador.
 *
 * GET /garage
 */
@RestController
@RequestMapping("/garage")
class GarageController(
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository,
) {

    @GetMapping
    fun getGarage(): ResponseEntity<GarageConfigResponse> {
        val sectors = sectorRepository.findAll().map {
            SectorResponse(
                sector = it.name,
                basePrice = it.basePrice,
                maxCapacity = it.maxCapacity,
            )
        }

        val spots = spotRepository.findAll().map {
            SpotResponse(
                id = it.id,
                sector = it.sector,
                lat = it.lat,
                lng = it.lng,
            )
        }

        return ResponseEntity.ok(GarageConfigResponse(garage = sectors, spots = spots))
    }
}

data class GarageConfigResponse(
    val garage: List<SectorResponse>,
    val spots: List<SpotResponse>,
)

data class SectorResponse(
    val sector: String,
    @JsonProperty("base_price")
    val basePrice: Double,
    @JsonProperty("max_capacity")
    val maxCapacity: Int,
)

data class SpotResponse(
    val id: Long,
    val sector: String,
    val lat: Double,
    val lng: Double,
)

