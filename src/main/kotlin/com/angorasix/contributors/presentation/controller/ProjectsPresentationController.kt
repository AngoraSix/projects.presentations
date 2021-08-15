package com.angorasix.contributors.presentation.controller

import com.angorasix.contributors.application.ProjectsPresentationService
import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.contributors.presentation.dto.ProjectPresentationDto
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
            ?.transform { convertProjectPresentationToDto(it) }
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
            ?.transform { convertProjectPresentationToDto(it) }!!
    }

    companion object {
        private fun convertProjectPresentationToDto(projectPresentation: ProjectPresentation): ProjectPresentationDto {
            return ProjectPresentationDto(
                projectPresentation.id?.toHexString(),
                projectPresentation.projectId,
                projectPresentation.description,
                projectPresentation.images
            )
        }

        private fun convertProjectPresentationToDomainObject(dto: ProjectPresentationDto): ProjectPresentation {
            return ProjectPresentation(
                dto.projectId,
                dto.description,
                dto.images
            )
        }
    }
}
