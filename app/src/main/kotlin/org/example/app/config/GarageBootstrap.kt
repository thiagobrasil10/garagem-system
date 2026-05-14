package org.example.app.config

import org.example.core.usecase.LoadGarageUseCase
import org.slf4j.LoggerFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * No startup, busca a configuração da garagem no simulador
 * e persiste setores + vagas no banco.
 */
@Component
class GarageBootstrap(
    private val loadGarageUseCase: LoadGarageUseCase,
) : ApplicationRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(args: ApplicationArguments?) {
        try {
            loadGarageUseCase.execute()
            log.info("Configuração da garagem carregada com sucesso")
        } catch (ex: Exception) {
            log.error("Falha ao carregar configuração inicial da garagem: {}", ex.message, ex)
        }
    }
}

