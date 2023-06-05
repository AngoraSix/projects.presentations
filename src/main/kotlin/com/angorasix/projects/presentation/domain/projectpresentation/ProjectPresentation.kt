package com.angorasix.projects.presentation.domain.projectpresentation

import com.angorasix.commons.domain.SimpleContributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation @PersistenceCreator private constructor(
    @field:Id val id: String?,
    val projectId: String,
    val admins: Set<SimpleContributor> = emptySet(),
    var referenceName: String,
    var sections: Collection<PresentationSection>?,
) {
    constructor(
        projectId: String,
        admins: Set<SimpleContributor>,
        referenceName: String,
        sections: Collection<PresentationSection>?,
    ) : this(
        null,
        projectId,
        admins,
        referenceName,
        sections,
    )
}
