package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.core.usecase.GetSpotStatusUseCase
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Spot Status", description = "Status atual de uma vaga por coordenadas")
@RestController
@RequestMapping("/spot-status")
class SpotStatusController(
    private val useCase: GetSpotStatusUseCase,
) {
    @Operation(
        summary = "Status de uma vaga",
        description = "Retorna se a vaga está ocupada e os horários de entrada/estacionamento, localizada pelas coordenadas (lat, lng).",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [ExampleObject(value = """{"lat":-23.561684,"lng":-46.655981}""")]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
        ApiResponse(responseCode = "400", description = "lat ou lng não informados"),
        ApiResponse(responseCode = "404", description = "Nenhuma vaga encontrada nas coordenadas"),
    )
    @PostMapping
    fun status(@RequestBody req: SpotStatusRequest): ResponseEntity<SpotStatusResponse> {
        val lat = req.lat ?: return ResponseEntity.badRequest().build()
        val lng = req.lng ?: return ResponseEntity.badRequest().build()
        val status = useCase.execute(lat, lng) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            SpotStatusResponse(
                occupied = status.occupied,
                entryTime = status.entryTime,
                timeParked = status.timeParked,
            )
        )
    }
}

data class SpotStatusRequest(
    val lat: Double? = null,
    val lng: Double? = null,
)

data class SpotStatusResponse(
    val occupied: Boolean,
    @JsonProperty("entry_time") val entryTime: Instant?,
    @JsonProperty("time_parked") val timeParked: Instant?,
)

