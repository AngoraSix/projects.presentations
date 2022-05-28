package com.angorasix.projects.presentation.presentation.dto

import javax.validation.constraints.NotEmpty

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentationDto(
    val projectId: String,
    val referenceName: String,
    @field:NotEmpty val sections: Collection<PresentationSectionDto>,
    val id: String? = null,
)

data class PresentationMediaDto(
    val mediaType: String,
    val url: String,
    val thumbnailUrl: String,
    val resourceId: String
)

data class PresentationSectionDto(
    val title: String,
    val description: String?,
    val media: Collection<PresentationMediaDto> = emptyList(),
    val mainMedia: PresentationMediaDto
)
