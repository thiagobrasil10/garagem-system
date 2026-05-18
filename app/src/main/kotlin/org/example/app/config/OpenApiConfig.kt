package org.example.app.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.tags.Tag
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun openAPI(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("Garagem System API")
                .description(
                    """
                    Backend de gerenciamento de estacionamento.
                    
                    Controla entrada/saída de veículos, ocupação de vagas, 
                    precificação dinâmica por lotação e faturamento por setor.
                    """.trimIndent()
                )
                .version("1.0.0")
                .contact(
                    Contact()
                        .name("Garagem System")
                )
        )
        .tags(
            listOf(
                Tag().name("Webhook").description("Eventos do simulador: ENTRY, PARKED, EXIT"),
                Tag().name("Garage").description("Configuração da garagem (setores e vagas)"),
                Tag().name("Revenue").description("Faturamento por setor e data"),
                Tag().name("Plate Status").description("Status atual de uma placa (sessão aberta)"),
                Tag().name("Spot Status").description("Status atual de uma vaga por coordenadas"),
            )
        )
}

