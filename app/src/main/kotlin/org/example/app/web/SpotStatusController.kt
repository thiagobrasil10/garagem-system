package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import org.example.core.usecase.GetSpotStatusUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

/**
 * POST /spot-status
 *
 * Request:  { "lat": -23.561684, "lng": -46.655981 }
 * Response: { occupied, entry_time, time_parked }
 *
 * Retorna 404 quando não existe vaga nas coordenadas informadas.
 */
@RestController
@RequestMapping("/spot-status")
class SpotStatusController(
    private val useCase: GetSpotStatusUseCase,
) {
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

