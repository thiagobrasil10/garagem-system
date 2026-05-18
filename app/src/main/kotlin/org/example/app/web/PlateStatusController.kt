package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.core.usecase.GetPlateStatusUseCase
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Plate Status", description = "Status atual de um veículo por placa")
@RestController
@RequestMapping("/plate-status")
class PlateStatusController(
    private val useCase: GetPlateStatusUseCase,
) {
    @Operation(
        summary = "Status de uma placa",
        description = "Retorna o valor acumulado até agora e os horários de entrada/estacionamento para uma placa com sessão aberta.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [ExampleObject(value = """{"license_plate":"ZUL0001"}""")]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Status retornado com sucesso"),
        ApiResponse(responseCode = "400", description = "Placa não informada"),
        ApiResponse(responseCode = "404", description = "Nenhuma sessão aberta para a placa"),
    )
    @PostMapping
    fun status(@RequestBody req: PlateStatusRequest): ResponseEntity<PlateStatusResponse> {
        val plate = req.licensePlate?.trim().orEmpty()
        if (plate.isBlank()) return ResponseEntity.badRequest().build()
        val status = useCase.execute(plate) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(
            PlateStatusResponse(
                licensePlate = status.licensePlate,
                priceUntilNow = status.priceUntilNow,
                entryTime = status.entryTime,
                timeParked = status.timeParked,
            )
        )
    }
}

data class PlateStatusRequest(
    @JsonProperty("license_plate") val licensePlate: String? = null,
)

data class PlateStatusResponse(
    @JsonProperty("license_plate") val licensePlate: String,
    @JsonProperty("price_until_now") val priceUntilNow: Double,
    @JsonProperty("entry_time") val entryTime: Instant,
    @JsonProperty("time_parked") val timeParked: Instant?,
)

