package com.angorasix.contributors.domain.contributor

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation(
    // val id: String,
    val projectId: String,
    val description: String,
    val images: MutableCollection<String> = mutableListOf<String>(),
) : ReactivePanacheMongoEntity()
