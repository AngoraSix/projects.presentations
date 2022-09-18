package com.angorasix.projects.presentation.domain.projectpresentation

import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceConstructor

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation @PersistenceConstructor private constructor(
    @field:Id val id: String?,
    val projectId: String,
    var referenceName: String,
    var sections: Collection<PresentationSection>?,
) {
    constructor(
        projectId: String,
        referenceName: String,
        sections: Collection<PresentationSection>?,
    ) : this(
        null,
        projectId,
        referenceName,
        sections,
    )
}
