package com.angorasix.projects.presentation.domain.projectpresentation

import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntity
import javax.validation.constraints.NotEmpty

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation(
        val projectId: String,
        @field:NotEmpty val sections: Collection<PresentationSection>
) : ReactivePanacheMongoEntity()
