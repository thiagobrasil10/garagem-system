package org.example.core.service

import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

/**
 * Regras de precificação dinâmica e cobrança.
 *
 *  - Multiplicador calculado **na entrada**, conforme lotação total da garagem:
 *      < 25%  -> 0.90
 *      < 50%  -> 1.00
 *      < 75%  -> 1.10
 *      <100%  -> 1.25
 *      =100%  -> setor fechado, entrada recusada.
 *  - Os primeiros 30 minutos são gratuitos (spec V1.4).
 *  - Após 30 minutos, cobra-se por hora cheia (ceil), inclusive a primeira hora.
 *  - Duração 0 (ou negativa) resulta em 0.
 */
object PricingService {

    fun multiplierForOccupancy(occupancyRatio: Double): Double = when {
        occupancyRatio < 0.25 -> 0.90
        occupancyRatio < 0.50 -> 1.00
        occupancyRatio < 0.75 -> 1.10
        occupancyRatio < 1.00 -> 1.25
        else -> 1.25
    }

    fun calculateCharge(
        entry: Instant,
        exit: Instant,
        basePricePerHour: Double,
        multiplier: Double,
    ): Double {
        val minutes = Duration.between(entry, exit).toMinutes()
        if (minutes <= 30) return 0.0                         // primeiros 30 min grátis
        val hours = ceil(minutes / 60.0).toInt()
        val raw = hours * basePricePerHour * multiplier
        return Math.round(raw * 100.0) / 100.0
    }
}

