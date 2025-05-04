package com.angorasix.projects.presentation.presentation.router

import com.angorasix.commons.reactive.presentation.filter.extractRequestingContributor
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.presentation.handler.ProjectsPresentationHandler
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.coRouter

/**
 * Router for all ProjectPresentation related endpoints.
 *
 * @author rozagerardo
 */
class ProjectsPresentationRouter(
    private val handler: ProjectsPresentationHandler,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Main RouterFunction configuration for all endpoints related to ProjectPresentations.
     *
     * @return the [RouterFunction] with all the routes for ProjectPresentations
     */
    fun projectPresentationRouterFunction() =
        coRouter {
            filter { request, next ->
                extractRequestingContributor(
                    request,
                    next,
                )
            }
            apiConfigs.basePaths.projectsPresentation.nest {
                apiConfigs.basePaths.baseByIdCrudRoute.nest {
                    method(apiConfigs.routes.updateProjectPresentation.method).nest {
                        method(
                            apiConfigs.routes.updateProjectPresentation.method,
                            handler::updateProjectPresentation,
                        )
                    }
                    method(apiConfigs.routes.getProjectPresentation.method).nest {
                        method(
                            apiConfigs.routes.getProjectPresentation.method,
                            handler::getProjectPresentation,
                        )
                    }
                }
                apiConfigs.basePaths.baseListCrudRoute.nest {
                    method(apiConfigs.routes.createProjectPresentation.method).nest {
                        method(
                            apiConfigs.routes.createProjectPresentation.method,
                            handler::createProjectPresentation,
                        )
                    }
                    method(apiConfigs.routes.listProjectPresentations.method).nest {
                        method(
                            apiConfigs.routes.listProjectPresentations.method,
                            handler::listProjectPresentations,
                        )
                    }
                }
            }
        }
}
