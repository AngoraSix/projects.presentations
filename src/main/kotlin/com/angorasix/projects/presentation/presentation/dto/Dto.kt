package com.angorasix.projects.presentation.presentation.dto

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentationDto(
    val id: String? = null,
    val projectId: String,
    val objective: String,
    val media: Collection<PresentationMediaDto> = emptyList(),
)

data class PresentationMediaDto(
    val type: String,
    val url: String,
)
