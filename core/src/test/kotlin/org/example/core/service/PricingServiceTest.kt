package org.example.core.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant

class PricingServiceTest {

    @Test
    fun `multiplier follows occupancy tiers`() {
        assertEquals(0.90, PricingService.multiplierForOccupancy(0.10))
        assertEquals(1.00, PricingService.multiplierForOccupancy(0.25))
        assertEquals(1.00, PricingService.multiplierForOccupancy(0.49))
        assertEquals(1.10, PricingService.multiplierForOccupancy(0.50))
        assertEquals(1.10, PricingService.multiplierForOccupancy(0.74))
        assertEquals(1.25, PricingService.multiplierForOccupancy(0.75))
        assertEquals(1.25, PricingService.multiplierForOccupancy(0.99))
        assertEquals(1.25, PricingService.multiplierForOccupancy(1.00))
    }

    @Test
    fun `first 30 minutes are free`() {
        val entry = Instant.parse("2025-01-01T12:00:00Z")
        // exatamente 0 min -> grátis
        assertEquals(0.0, PricingService.calculateCharge(entry, entry, 10.0, 1.0))
        // exatamente 30 min -> grátis
        val exit30 = Instant.parse("2025-01-01T12:30:00Z")
        assertEquals(0.0, PricingService.calculateCharge(entry, exit30, 10.0, 1.0))
    }

    @Test
    fun `charge rounds hours up and applies multiplier`() {
        val entry = Instant.parse("2025-01-01T12:00:00Z")
        // 31 minutos -> 1 hora cheia (após carência de 30 min)
        val exit31 = Instant.parse("2025-01-01T12:31:00Z")
        assertEquals(10.0, PricingService.calculateCharge(entry, exit31, 10.0, 1.0))

        // 1h05 -> 2 horas cheias
        val exit65 = Instant.parse("2025-01-01T13:05:00Z")
        assertEquals(20.0, PricingService.calculateCharge(entry, exit65, 10.0, 1.0))

        // 1h00 com multiplicador 1.25 -> 12.50
        val exit60 = Instant.parse("2025-01-01T13:00:00Z")
        assertEquals(12.50, PricingService.calculateCharge(entry, exit60, 10.0, 1.25))
    }
}

