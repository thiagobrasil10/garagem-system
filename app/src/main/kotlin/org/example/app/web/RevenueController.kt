package org.example.app.web

import com.fasterxml.jackson.annotation.JsonFormat
import org.example.core.usecase.GetRevenueUseCase
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate

/**
 * GET /revenue  (spec V1.4 — GET com body JSON)
 *
 * Request:  { "date": "2025-01-01", "sector": "A" }
 * Response: { amount, currency, timestamp }
 */
@RestController
@RequestMapping("/revenue")
class RevenueController(
    private val getRevenueUseCase: GetRevenueUseCase,
) {

    @GetMapping
    fun revenue(@RequestBody req: RevenueRequest): ResponseEntity<RevenueResponse> {
        val date = req.date ?: return ResponseEntity.badRequest().build()
        val sector = req.sector?.takeIf { it.isNotBlank() } ?: return ResponseEntity.badRequest().build()
        val result = getRevenueUseCase.execute(sector, date)
        return ResponseEntity.ok(
            RevenueResponse(
                amount = result.amount,
                currency = result.currency,
                timestamp = Instant.now(),
            )
        )
    }
}

data class RevenueRequest(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    val date: LocalDate? = null,
    val sector: String? = null,
)

data class RevenueResponse(
    val amount: Double,
    val currency: String,
    val timestamp: Instant,
)

