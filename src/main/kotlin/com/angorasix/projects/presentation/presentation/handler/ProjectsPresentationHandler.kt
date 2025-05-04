package com.angorasix.projects.presentation.presentation.handler

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
import com.angorasix.commons.reactive.presentation.mappings.addLink
import com.angorasix.commons.reactive.presentation.mappings.addSelfLink
import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.PresentationSectionDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationQueryParams
import kotlinx.coroutines.flow.map
import org.springframework.hateoas.IanaLinkRelations
import org.springframework.hateoas.MediaTypes
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import java.net.URI

/**
 * ProjectPresentation Handler (Controller) containing all handler functions related to ProjectPresentation endpoints.
 *
 * @author rozagerardo
 */
class ProjectsPresentationHandler(
    private val service: ProjectsPresentationService,
    private val apiConfigs: ApiConfigs,
) {
    /**
     * Handler for the List ProjectPresentations endpoint,
     * retrieving a Flux including all persisted ProjectPresentations.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun listProjectPresentations(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        return service
            .findProjectPresentations(request.queryParams().toQueryFilter())
            .map {
                it.convertToDto(requestingContributor as? A6Contributor, apiConfigs, request)
            }.let {
                ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyAndAwait(it)
            }
    }

    /**
     * Handler for the Get Single ProjectPresentation endpoint,
     * retrieving a Mono with the requested ProjectPresentation.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun getProjectPresentation(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]
        val projectPresentationId = request.pathVariable("id")
        return service.findSingleProjectPresentation(projectPresentationId)?.let {
            val outputProjectPresentation =
                it.convertToDto(
                    requestingContributor as? A6Contributor,
                    apiConfigs,
                    request,
                )
            ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProjectPresentation)
        } ?: resolveNotFound("Can't find Project Presentation", "Project Presentation")
    }

    /**
     * Handler for the Create ProjectPresentations endpoint, to create a new ProjectPresentation entity.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun createProjectPresentation(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        return if (requestingContributor !is A6Contributor) {
            resolveBadRequest("Invalid Contributor Token", "Contributor Token")
        } else {
            val project =
                try {
                    request.awaitBody<ProjectPresentationDto>().convertToDomain(
                        setOf(
                            A6Contributor(
                                requestingContributor.contributorId,
                            ),
                        ),
                    )
                } catch (e: IllegalArgumentException) {
                    return resolveBadRequest(
                        e.message ?: "Incorrect Project Presentation body",
                        "Project Presentation",
                    )
                }

            val outputProjectPresentation =
                service
                    .createProjectPresentation(project)
                    .convertToDto(requestingContributor, apiConfigs, request)

            val selfLink =
                outputProjectPresentation.links.getRequiredLink(IanaLinkRelations.SELF).href

            created(URI.create(selfLink))
                .contentType(MediaTypes.HAL_FORMS_JSON)
                .bodyValueAndAwait(outputProjectPresentation)
        }
    }

    /**
     * Handler for the Update ProjectPresentation endpoint, retrieving a Mono with the updated ProjectPresentation.
     *
     * @param request - HTTP `ServerRequest` object
     * @return the `ServerResponse`
     */
    suspend fun updateProjectPresentation(request: ServerRequest): ServerResponse {
        val requestingContributor =
            request.attributes()[AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY]

        return if (requestingContributor !is A6Contributor) {
            resolveBadRequest("Invalid Contributor Token", "Contributor Token")
        } else {
            val projectId = request.pathVariable("id")

            val updateProjectPresentationData =
                try {
                    request
                        .awaitBody<ProjectPresentationDto>()
                        .let { it.convertToDomain(it.admins ?: emptySet()) }
                } catch (e: IllegalArgumentException) {
                    return resolveBadRequest(
                        e.message ?: "Incorrect Project Presentation body",
                        "Project Presentation",
                    )
                }

            service
                .updateProjectPresentation(
                    projectId,
                    updateProjectPresentationData,
                    requestingContributor,
                )?.let {
                    val outputProjectPresentation =
                        it.convertToDto(
                            requestingContributor,
                            apiConfigs,
                            request,
                        )

                    ok()
                        .contentType(MediaTypes.HAL_FORMS_JSON)
                        .bodyValueAndAwait(outputProjectPresentation)
                } ?: resolveNotFound("Can't update this project presentation", "Project Presentation")
        }
    }
}

private fun ProjectPresentation.convertToDto(): ProjectPresentationDto =
    ProjectPresentationDto(
        projectId,
        admins,
        referenceName,
        sections?.map { it.convertToDto() },
        id,
    )

private fun ProjectPresentation.convertToDto(
    requestingContributor: A6Contributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectPresentationDto = convertToDto().resolveHypermedia(requestingContributor, apiConfigs, request)

private fun ProjectPresentationDto.convertToDomain(admins: Set<A6Contributor>): ProjectPresentation {
    if (projectId == null || referenceName == null) {
        throw IllegalArgumentException("Invalid ProjectPresentationDto: $this")
    }
    return ProjectPresentation(
        projectId,
        admins,
        referenceName,
        sections?.map { it.convertToDomain() }?.toMutableSet(),
    )
}

private fun PresentationSection.convertToDto(): PresentationSectionDto =
    PresentationSectionDto(
        title,
        description,
        media.map { it.convertToDto() },
        mainMedia.convertToDto(),
    )

private fun PresentationSectionDto.convertToDomain(): PresentationSection =
    PresentationSection(
        title,
        description,
        media?.map { it.convertToDomain() } ?: emptyList(),
        mainMedia.convertToDomain(),
    )

private fun PresentationMedia.convertToDto(): PresentationMediaDto =
    PresentationMediaDto(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )

private fun PresentationMediaDto.convertToDomain(): PresentationMedia =
    PresentationMedia(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )

private fun ProjectPresentationDto.resolveHypermedia(
    requestingContributor: A6Contributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectPresentationDto {
    // self
    addSelfLink(apiConfigs.routes.getProjectPresentation, request, listOf(id ?: "undefinedId"))

    // edit ProjectPresentation
    if (requestingContributor != null && admins != null) {
        if (admins?.map { it.contributorId }?.contains(
                requestingContributor.contributorId,
            ) == true
        ) {
            addLink(
                apiConfigs.routes.updateProjectPresentation,
                apiConfigs.projectPresentationActions.updateProjectPresentation,
                request,
                listOf(id ?: "undefinedId"),
            )
        }
    }
    return this
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListProjectPresentationsFilter =
    ListProjectPresentationsFilter(
        projectIds = get(ProjectPresentationQueryParams.PROJECT_IDS.param)?.flatMap { it.split(",") },
        text = get(ProjectPresentationQueryParams.TEXT.param)?.firstOrNull(),
    )
