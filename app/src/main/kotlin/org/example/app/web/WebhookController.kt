package org.example.app.web

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.example.core.usecase.EntryResult
import org.example.core.usecase.ExitResult
import org.example.core.usecase.HandleEntryUseCase
import org.example.core.usecase.HandleExitUseCase
import org.example.core.usecase.HandleParkedUseCase
import org.example.core.usecase.ParkedResult
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant

@Tag(name = "Webhook", description = "Recebe eventos do simulador: ENTRY, PARKED e EXIT")
@RestController
@RequestMapping("/webhook")
class WebhookController(
    private val handleEntry: HandleEntryUseCase,
    private val handleParked: HandleParkedUseCase,
    private val handleExit: HandleExitUseCase,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Operation(
        summary = "Receber evento do simulador",
        description = "Aceita eventos do tipo ENTRY, PARKED e EXIT. Sempre retorna 200 para o simulador.",
        requestBody = io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = [Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = [
                    ExampleObject(name = "ENTRY", value = """{"license_plate":"ZUL0001","entry_time":"2026-05-18T12:00:00Z","event_type":"ENTRY"}"""),
                    ExampleObject(name = "PARKED", value = """{"license_plate":"ZUL0001","lat":-23.561684,"lng":-46.655981,"event_type":"PARKED"}"""),
                    ExampleObject(name = "EXIT", value = """{"license_plate":"ZUL0001","exit_time":"2026-05-18T14:30:00Z","event_type":"EXIT"}"""),
                ]
            )]
        )
    )
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "Evento processado com sucesso"),
        ApiResponse(responseCode = "400", description = "Campo obrigatório ausente ou event_type inválido"),
        ApiResponse(responseCode = "404", description = "Vaga não encontrada (PARKED)"),
        ApiResponse(responseCode = "409", description = "Regra de negócio violada (duplicado, setor cheio, etc.)"),
    )
    @PostMapping
    fun receive(@RequestBody event: WebhookEvent): ResponseEntity<WebhookResponse> {
        log.info("Webhook recebido: {}", event)
        val eventType = event.eventType.uppercase()
        return when (eventType) {
            "ENTRY" -> handleEntryEvent(event, eventType)
            "PARKED" -> handleParkedEvent(event, eventType)
            "EXIT" -> handleExitEvent(event, eventType)
            else -> {
                log.warn("Evento desconhecido: {}", event.eventType)
                badRequest(eventType, "Tipo de evento desconhecido: '${event.eventType}'")
            }
        }
    }

    private fun handleEntryEvent(event: WebhookEvent, eventType: String): ResponseEntity<WebhookResponse> {
        val plate = event.licensePlate
            ?: return badRequest(eventType, "Campo 'license_plate' é obrigatório para ENTRY")
        val entry = event.entryTime ?: Instant.now()
        return when (val result = handleEntry.execute(plate, entry)) {
            is EntryResult.Created -> ok(eventType, "Entrada registrada para placa $plate")
            is EntryResult.AlreadyOpen -> conflict(
                eventType,
                "Já existe sessão aberta para placa $plate (entrada em ${result.session.entryTime}), ENTRY duplicado ignorado"
            )
            is EntryResult.GarageFull -> conflict(
                eventType,
                "Garagem lotada — entrada recusada para placa $plate"
            )
        }
    }

    private fun handleParkedEvent(event: WebhookEvent, eventType: String): ResponseEntity<WebhookResponse> {
        val plate = event.licensePlate
            ?: return badRequest(eventType, "Campo 'license_plate' é obrigatório para PARKED")
        val lat = event.lat ?: return badRequest(eventType, "Campo 'lat' é obrigatório para PARKED")
        val lng = event.lng ?: return badRequest(eventType, "Campo 'lng' é obrigatório para PARKED")
        return when (val result = handleParked.execute(plate, lat, lng)) {
            is ParkedResult.Parked -> ok(
                eventType,
                "Veículo $plate estacionado na vaga ${result.spotId} (setor ${result.sector})"
            )
            is ParkedResult.NoOpenSession -> conflict(
                eventType,
                "PARKED recebido para placa $plate sem ENTRY prévio"
            )
            is ParkedResult.SpotNotFound -> notFound(
                eventType,
                "Nenhuma vaga encontrada para lat=${result.lat}, lng=${result.lng}"
            )
            is ParkedResult.SpotAlreadyOccupied -> conflict(
                eventType,
                "Vaga ${result.spotId} já está ocupada"
            )
            is ParkedResult.SectorFull -> conflict(
                eventType,
                "Setor ${result.sector} lotado — PARKED recusado para placa $plate"
            )
        }
    }

    private fun handleExitEvent(event: WebhookEvent, eventType: String): ResponseEntity<WebhookResponse> {
        val plate = event.licensePlate
            ?: return badRequest(eventType, "Campo 'license_plate' é obrigatório para EXIT")
        val exit = event.exitTime ?: Instant.now()
        return when (val result = handleExit.execute(plate, exit)) {
            is ExitResult.Charged -> ok(
                eventType,
                "Saída registrada para placa $plate — valor cobrado: R$ %.2f".format(result.amount)
            )
            is ExitResult.NoOpenSession -> conflict(
                eventType,
                "EXIT recebido para placa $plate sem sessão aberta"
            )
        }
    }

    private fun ok(eventType: String, message: String) =
        ResponseEntity.ok(WebhookResponse.success(eventType, message))

    private fun badRequest(eventType: String, message: String) =
        ResponseEntity.badRequest().body(WebhookResponse.error(eventType, message))

    private fun conflict(eventType: String, message: String) =
        ResponseEntity.status(HttpStatus.CONFLICT).body(WebhookResponse.error(eventType, message))

    private fun notFound(eventType: String, message: String) =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(WebhookResponse.error(eventType, message))
}

data class WebhookEvent(
    @JsonProperty("license_plate") val licensePlate: String? = null,
    @JsonProperty("event_type") val eventType: String = "",
    @JsonProperty("entry_time") val entryTime: Instant? = null,
    @JsonProperty("exit_time") val exitTime: Instant? = null,
    val lat: Double? = null,
    val lng: Double? = null,
)

data class WebhookResponse(
    val status: String,
    @JsonProperty("event_type") val eventType: String,
    val message: String,
    val timestamp: Instant = Instant.now(),
) {
    companion object {
        fun success(eventType: String, message: String) =
            WebhookResponse(status = "SUCCESS", eventType = eventType, message = message)

        fun error(eventType: String, message: String) =
            WebhookResponse(status = "ERROR", eventType = eventType, message = message)
    }
}

