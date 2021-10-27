package com.angorasix.projects.presentation.presentation.dto

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentationDto(
    val projectId: String,
    val objective: String,
    val media: Collection<PresentationMediaDto> = emptyList(),
    val id: String? = null,
)

data class PresentationMediaDto(
    val type: String,
    val url: String,
)
