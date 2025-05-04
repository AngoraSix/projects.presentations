package com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api

import com.angorasix.commons.infrastructure.config.configurationproperty.api.Route
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

/**
 * <p>
 *  Base file containing all Service configurations.
 * </p>
 *
 * @author rozagerardo
 */
@ConfigurationProperties(prefix = "configs.api")
data class ApiConfigs(
    @NestedConfigurationProperty
    var routes: RoutesConfigs,
    @NestedConfigurationProperty
    var basePaths: BasePathConfigs,
    @NestedConfigurationProperty
    var projectPresentationActions: ProjectPresentationActions,
)

data class BasePathConfigs(
    val projectsPresentation: String,
    val baseListCrudRoute: String,
    val baseByIdCrudRoute: String,
)

data class RoutesConfigs(
    val createProjectPresentation: Route,
    val updateProjectPresentation: Route,
    val getProjectPresentation: Route,
    val listProjectPresentations: Route,
)

data class ProjectPresentationActions(
    val updateProjectPresentation: String,
)
