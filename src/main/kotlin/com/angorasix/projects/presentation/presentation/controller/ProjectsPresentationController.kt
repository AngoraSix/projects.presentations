package com.angorasix.projects.presentation.presentation.controller

import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/projects-presentation")
class ProjectsPresentationController(private val service: ProjectsPresentationService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    fun getProjectPresentation(@PathParam("id") id: String): Uni<Response> {
        return service.findSingleProjectPresentation(id)
            ?.onItem()
            ?.transform { it.convertToDto() }
            ?.onItem()
            ?.transform {
                Response.ok(it)
                    .build()
            } ?: Uni.createFrom()
            .item(
                Response.status(Response.Status.BAD_REQUEST)
                    .build()
            )
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    fun getProjectPresentations(): Multi<ProjectPresentationDto> {
        return service.findProjectPresentations()
            ?.onItem()
            ?.transform { it.convertToDto() }!!
    }
}

private fun ProjectPresentation.convertToDto(): ProjectPresentationDto {
    return ProjectPresentationDto(id?.toHexString(),
        projectId,
        objective,
        media.map { it.convertToDto() })
}

private fun ProjectPresentationDto.convertToDomainObject(): ProjectPresentation {
    return ProjectPresentation(projectId,
        objective,
        media.map { it.convertToDomain() })
}

private fun PresentationMedia.convertToDto(): PresentationMediaDto {
    return PresentationMediaDto(
        type,
        url
    )
}

private fun PresentationMediaDto.convertToDomain(): PresentationMedia {
    return PresentationMedia(
        type,
        url
    )
}
