package org.example.dataprovider.config

import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

/**
 * Mantém o wiring de JPA dentro do próprio módulo dataprovider,
 * para que o app não precise conhecer detalhes de persistência.
 */
@Configuration
@EntityScan("org.example.dataprovider.jpa.entity")
@EnableJpaRepositories("org.example.dataprovider.jpa.repository")
class DataProviderJpaConfig

