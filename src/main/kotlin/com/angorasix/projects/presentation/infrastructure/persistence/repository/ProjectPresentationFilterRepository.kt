package com.angorasix.projects.presentation.infrastructure.persistence.repository

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
}
