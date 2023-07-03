package com.angorasix.projects.presentation.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.commons.reactive.presentation.error.resolveBadRequest
import com.angorasix.commons.reactive.presentation.error.resolveNotFound
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
import org.springframework.hateoas.Link
import org.springframework.hateoas.MediaTypes
import org.springframework.hateoas.mediatype.Affordances
import org.springframework.http.HttpMethod
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.ServerResponse.created
import org.springframework.web.reactive.function.server.ServerResponse.ok
import org.springframework.web.reactive.function.server.awaitBody
import org.springframework.web.reactive.function.server.bodyAndAwait
import org.springframework.web.reactive.function.server.bodyValueAndAwait
import org.springframework.web.util.UriComponentsBuilder
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
        return service.findProjectPresentations(request.queryParams().toQueryFilter()).map {
            it.convertToDto(requestingContributor as? SimpleContributor, apiConfigs, request)
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
                    requestingContributor as? SimpleContributor,
                    apiConfigs,
                    request,
                )
            ok().contentType(MediaTypes.HAL_FORMS_JSON)
                .bodyValueAndAwait(outputProjectPresentation)
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
        return if (requestingContributor is SimpleContributor) {
            val project = try {
                request.awaitBody<ProjectPresentationDto>()
                    .convertToDomain()
            } catch (e: IllegalArgumentException) {
                return resolveBadRequest(
                    e.message ?: "Incorrect Project Presentation body",
                    "Project Presentation",
                )
            }
            val outputProjectPresentation = service.createProjectPresentation(project)
                .convertToDto(requestingContributor, apiConfigs, request)
            val selfLink =
                outputProjectPresentation.links.getRequiredLink(IanaLinkRelations.SELF).href
            created(URI.create(selfLink)).contentType(MediaTypes.HAL_FORMS_JSON)
                .bodyValueAndAwait(outputProjectPresentation)
        } else {
            resolveBadRequest("Invalid Contributor Header", "Contributor Header")
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
        val projectId = request.pathVariable("id")
        val updateProjectPresentationData = try {
            request.awaitBody<ProjectPresentationDto>()
                .let { it.convertToDomain() }
        } catch (e: IllegalArgumentException) {
            return resolveBadRequest(
                e.message ?: "Incorrect Project Presentation body",
                "Project Presentation",
            )
        }
        return service.updateProjectPresentation(projectId, updateProjectPresentationData)?.let {
            val outputProjectPresentation =
                it.convertToDto(
                    requestingContributor as? SimpleContributor,
                    apiConfigs,
                    request,
                )
            ok().contentType(MediaTypes.HAL_FORMS_JSON).bodyValueAndAwait(outputProjectPresentation)
        } ?: resolveNotFound("Can't update this project presentation", "Project Presentation")
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
    simpleContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectPresentationDto =
    convertToDto().resolveHypermedia(simpleContributor, apiConfigs, request)

private fun ProjectPresentationDto.convertToDomain(): ProjectPresentation {
    if (projectId == null || admins == null || referenceName == null) {
        throw IllegalArgumentException("Invalid ProjectPresentationDto: $this")
    }
    return ProjectPresentation(
        projectId,
        admins ?: throw IllegalArgumentException("Invalid ProjectPresentationDto: $this"),
        referenceName,
        sections?.map { it.convertToDomain() }?.toMutableSet(),
    )
}

private fun PresentationSection.convertToDto(): PresentationSectionDto {
    return PresentationSectionDto(
        title,
        description,
        media.map { it.convertToDto() },
        mainMedia.convertToDto(),
    )
}

private fun PresentationSectionDto.convertToDomain(): PresentationSection {
    return PresentationSection(
        title,
        description,
        media?.map { it.convertToDomain() } ?: emptyList(),
        mainMedia.convertToDomain(),
    )
}

private fun PresentationMedia.convertToDto(): PresentationMediaDto {
    return PresentationMediaDto(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )
}

private fun PresentationMediaDto.convertToDomain(): PresentationMedia {
    return PresentationMedia(
        mediaType,
        url,
        thumbnailUrl,
        resourceId,
    )
}

private fun ProjectPresentationDto.resolveHypermedia(
    simpleContributor: SimpleContributor?,
    apiConfigs: ApiConfigs,
    request: ServerRequest,
): ProjectPresentationDto {
    val getSingleRoute = apiConfigs.routes.getProjectPresentation
    // self
    val selfLink =
        Link.of(uriBuilder(request).path(getSingleRoute.resolvePath()).build().toUriString())
            .withRel(getSingleRoute.name).expand(id).withSelfRel()
    val selfLinkWithDefaultAffordance =
        Affordances.of(selfLink).afford(HttpMethod.OPTIONS).withName("default").toLink()
    add(selfLinkWithDefaultAffordance)

    // edit ProjectPresentation
    if (simpleContributor != null && admins != null) {
        if (admins?.map { it.id }?.contains(simpleContributor.id) == true) {
            val editProjectPresentationRoute = apiConfigs.routes.updateProjectPresentation
            val editProjectPresentationLink =
                Link.of(
                    uriBuilder(request).path(editProjectPresentationRoute.resolvePath())
                        .build().toUriString(),
                ).withTitle(editProjectPresentationRoute.name)
                    .withName(editProjectPresentationRoute.name)
                    .withRel(editProjectPresentationRoute.name).expand(id)
            val editProjectPresentationAffordanceLink =
                Affordances.of(editProjectPresentationLink).afford(HttpMethod.PUT)
                    .withName(editProjectPresentationRoute.name).toLink()
            add(editProjectPresentationAffordanceLink)
        }
    }
    return this
}

private fun uriBuilder(request: ServerRequest) = request.requestPath().contextPath().let {
    UriComponentsBuilder.fromHttpRequest(request.exchange().request).replacePath(it.toString()) //
        .replaceQuery("")
}

private fun MultiValueMap<String, String>.toQueryFilter(): ListProjectPresentationsFilter {
    return ListProjectPresentationsFilter(
        get(ProjectPresentationQueryParams.PROJECT_IDS.param)?.flatMap { it.split(",") },
    )
}
