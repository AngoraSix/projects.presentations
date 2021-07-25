package com.angorasix.contributors.presentation.dto

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentationDto constructor(
    val id: String?,
    val projectId: String,
    val description: String,
    val images: MutableCollection<String> = mutableListOf<String>(),
)
