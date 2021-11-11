package com.angorasix.contributors.domain.contributor

import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation(
    val projectId: String,
    val title: String,
    val objective: String,
    val media: Collection<PresentationMedia> = emptyList()
) : ReactivePanacheMongoEntity()
