package org.example.app.web

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.core.usecase.GetRevenueUseCase
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDate

@Tag(name = "Revenue", description = "Faturamento por setor e data")
@RestController
@RequestMapping("/revenue")
class RevenueController(
    private val getRevenueUseCase: GetRevenueUseCase,
) {

    @Operation(
        summary = "Faturamento por setor e data",
        description = "Retorna a soma de todos os valores cobrados em saídas (EXIT) no setor e data informados.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [ExampleObject(value = """{"date":"2026-05-18","sector":"A"}""")]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Faturamento calculado"),
        ApiResponse(responseCode = "400", description = "Parâmetros ausentes"),
    )
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

