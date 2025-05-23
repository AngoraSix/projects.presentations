package com.angorasix.projects.presentation.infrastructure.persistence.repository

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import kotlinx.coroutines.flow.Flow

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
interface ProjectPresentationFilterRepository {
    fun findUsingFilter(filter: ListProjectPresentationsFilter): Flow<ProjectPresentation>

    suspend fun findForContributorUsingFilter(
        filter: ListProjectPresentationsFilter,
        requestingContributor: A6Contributor?,
    ): ProjectPresentation?
}
