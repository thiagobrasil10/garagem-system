package org.example.app.web

import org.example.core.usecase.GetRevenueUseCase
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate

@RestController
@RequestMapping("/revenue")
class RevenueController(
    private val getRevenueUseCase: GetRevenueUseCase,
) {

    @GetMapping
    fun revenue(
        @RequestParam("sector") sector: String,
        @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) date: LocalDate,
    ): ResponseEntity<RevenueResponse> {
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

data class RevenueResponse(
    val amount: Double,
    val currency: String,
    val timestamp: Instant,
)

