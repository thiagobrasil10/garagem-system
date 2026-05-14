package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.core.usecase.GetPlateStatusUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * POST /plate-status
 *
 * Request:  { "license_plate": "ZUL0001" }
 * Response: { license_plate, price_until_now, entry_time, time_parked }
 *
 * Retorna 404 quando não existe sessão aberta para a placa.
 */
@RestController
@RequestMapping("/plate-status")
class PlateStatusController(
    private val useCase: GetPlateStatusUseCase,
) {
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

