package com.angorasix.projects.presentation.presentation.dto

import com.angorasix.commons.domain.A6Contributor
import org.springframework.hateoas.RepresentationModel

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentationDto(
    val projectId: String? = null,
    var admins: Set<A6Contributor>? = mutableSetOf(),
    val referenceName: String? = null,
    val sections: Collection<PresentationSectionDto>? = null,
    val id: String? = null,
) : RepresentationModel<ProjectPresentationDto>()

data class PresentationMediaDto(
    val mediaType: String,
    val url: String,
    val thumbnailUrl: String,
    val resourceId: String,
)

data class PresentationSectionDto(
    val title: String,
    val description: String? = null,
    val media: Collection<PresentationMediaDto>? = emptyList(),
    val mainMedia: PresentationMediaDto,
)
