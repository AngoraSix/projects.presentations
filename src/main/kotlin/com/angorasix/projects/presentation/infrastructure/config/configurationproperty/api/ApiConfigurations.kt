package com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty
import org.springframework.http.HttpMethod

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
)

data class BasePathConfigs constructor(val projectsPresentation: String)

data class RoutesConfigs constructor(
    val baseListCrudRoute: String,
    val baseByIdCrudRoute: String,
    val createProjectPresentation: Route,
    val updateProjectPresentation: Route,
    val getProjectPresentation: Route,
    val listProjectPresentations: Route,
)

data class Route(
    val name: String,
    val basePaths: List<String>,
    val method: HttpMethod,
    val path: String,
) {
    fun resolvePath(): String = basePaths.joinToString("").plus(path)
}
