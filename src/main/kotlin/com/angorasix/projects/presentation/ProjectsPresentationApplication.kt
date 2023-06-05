package com.angorasix.projects.presentation

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.hateoas.config.EnableHypermediaSupport
import org.springframework.hateoas.support.WebStack

/**
 * Spring Boot main class for Projects Presentation.
 *
 * @author rozagerardo
 */
@SpringBootApplication
@EnableHypermediaSupport(
    type = [EnableHypermediaSupport.HypermediaType.HAL_FORMS],
    stacks = [WebStack.WEBFLUX],
)
@ConfigurationPropertiesScan("com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api")
class ProjectsPresentationApplication

/**
 * Main application method.
 *
 * @param args java args
 */
fun main(args: Array<String>) {
    runApplication<ProjectsPresentationApplication>(args = args)
}
