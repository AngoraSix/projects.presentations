package com.angorasix.projects.presentation.domain.projectpresentation

import com.angorasix.commons.domain.A6Contributor
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.PersistenceCreator

/**
 *
 *
 * @author rozagerardo
 */
data class ProjectPresentation
    @PersistenceCreator
    private constructor(
        @field:Id val id: String?,
        val projectId: String,
        val admins: Set<A6Contributor> = emptySet(),
        var referenceName: String,
        var sections: Collection<PresentationSection>? = emptyList(),
    ) {
        constructor(
            projectId: String,
            admins: Set<A6Contributor>,
            referenceName: String,
            sections: Collection<PresentationSection>? = emptyList(),
        ) : this(
            null,
            projectId,
            admins,
            referenceName,
            sections,
        )
    }
