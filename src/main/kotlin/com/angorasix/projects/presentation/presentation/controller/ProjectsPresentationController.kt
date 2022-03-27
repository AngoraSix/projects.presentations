package com.angorasix.projects.presentation.presentation.controller

import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.presentation.dto.PresentationSectionDto
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import javax.validation.Valid
import javax.ws.rs.GET
import javax.ws.rs.NotFoundException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType

@Path("/projects-presentation")
class ProjectsPresentationController(private val service: ProjectsPresentationService) {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    fun getProjectPresentation(@PathParam("id") id: String): Uni<ProjectPresentationDto> {
        return service.findSingleProjectPresentation(id)
                .onItem()
                .ifNotNull()
                .transform { it.convertToDto() }
                // if not found
                .onItem()
                .ifNull()
                .failWith(NotFoundException("Project Presentation not found"))
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    fun getProjectPresentations(): Multi<ProjectPresentationDto> {
        return service.findProjectPresentations()
                .onItem()
                .transform { it.convertToDto() }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Path("")
    fun createProjectPresentation(@Valid newProject: @Valid ProjectPresentationDto): Uni<ProjectPresentationDto> {
        return service.createProjectPresentations(newProject.convertToDomainObject())
                .onItem()
                .transform { it.convertToDto() }!!
    }
}

private fun ProjectPresentation.convertToDto(): ProjectPresentationDto {
    return ProjectPresentationDto(
            projectId,
            sections.map { it.convertToDto() },
            id?.toString()
    )
}

private fun ProjectPresentationDto.convertToDomainObject(): ProjectPresentation {
    return ProjectPresentation(
            projectId,
            sections.map { it.convertToDomain() }
    )
}

private fun PresentationSection.convertToDto(): PresentationSectionDto {
    return PresentationSectionDto(
            title,
            description,
            media.map { it.convertToDto() },
            mainMedia?.convertToDto()
    )
}

private fun PresentationSectionDto.convertToDomain(): PresentationSection {
    return PresentationSection(
            title,
            description,
            media.map { it.convertToDomain() },
            mainMedia?.convertToDomain()
    )
}

private fun PresentationMedia.convertToDto(): PresentationMediaDto {
    return PresentationMediaDto(
            mediaType,
            url,
            thumbnailUrl,
            resourceId
    )
}

private fun PresentationMediaDto.convertToDomain(): PresentationMedia {
    return PresentationMedia(
            mediaType,
            url,
            thumbnailUrl,
            resourceId
    )
}
