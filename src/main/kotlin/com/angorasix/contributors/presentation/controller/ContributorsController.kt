package com.angorasix.contributors.presentation.controller

import com.angorasix.contributors.application.ContributorsService
import com.angorasix.contributors.domain.contributor.Contributor
import com.angorasix.contributors.presentation.dto.ContributorDto
import io.smallrye.mutiny.Uni
import javax.enterprise.inject.Default
import javax.inject.Inject
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

@Path("/contributors")
class ContributorsController {

    @Inject
    @field: Default
    lateinit var service: ContributorsService

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    fun getContributor(@PathParam("id") id: String): Uni<Response> {
        return service.findSingleContributor(id)?.onItem()?.transform { convertContributorToDto(it) }
            ?.onItem()?.transform{ Response.ok(it).build() } ?: Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST).build())
    }

    companion object {
        private fun convertContributorToDto(contributor: Contributor): ContributorDto {
            return ContributorDto(
                contributor.id,
                contributor.name
            )
        }

        private fun convertContributorToDomainObject(dto: ContributorDto): Contributor {
            return Contributor(
                dto.id,
                dto.name
            )
        }
    }
}
