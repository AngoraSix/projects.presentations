package com.angorasix.projects.presentation.infrastructure.service

import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentationRepository
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.presentation.handler.ProjectsPresentationHandler
import com.angorasix.projects.presentation.presentation.router.ProjectsPresentationRouter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServiceConfiguration {
    @Bean
    fun projectsPresentationService(repository: ProjectPresentationRepository) = ProjectsPresentationService(repository)

    @Bean
    fun projectsPresentationHandler(
        service: ProjectsPresentationService,
        apiConfigs: ApiConfigs,
    ) = ProjectsPresentationHandler(service, apiConfigs)

    @Bean
    fun projectsPresentationRouter(
        handler: ProjectsPresentationHandler,
        apiConfigs: ApiConfigs,
    ) = ProjectsPresentationRouter(handler, apiConfigs).projectPresentationRouterFunction()
}
