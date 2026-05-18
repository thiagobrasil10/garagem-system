package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.core.port.SectorRepository
import org.example.core.port.SpotRepository
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Garage", description = "Configuração da garagem persistida a partir do simulador")
@RestController
@RequestMapping("/garage")
class GarageController(
    private val sectorRepository: SectorRepository,
    private val spotRepository: SpotRepository,
) {

    @Operation(summary = "Configuração da garagem", description = "Retorna todos os setores e vagas carregados do simulador no startup.")
    @ApiResponse(responseCode = "200", description = "Configuração retornada com sucesso")
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

