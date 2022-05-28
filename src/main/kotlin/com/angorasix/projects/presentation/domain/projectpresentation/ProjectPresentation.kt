package com.angorasix.projects.presentation.domain.projectpresentation

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity
import javax.validation.constraints.NotEmpty

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation(
        val projectId: String,
        var referenceName: String,
        @field:NotEmpty var sections: Collection<PresentationSection>
) : ReactivePanacheMongoEntity()
