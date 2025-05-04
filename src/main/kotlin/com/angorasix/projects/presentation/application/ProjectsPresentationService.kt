package com.angorasix.projects.presentation.application

import com.angorasix.commons.domain.A6Contributor
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentationRepository
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsPresentationService(
    private val repository: ProjectPresentationRepository,
) {
    suspend fun findSingleProjectPresentation(id: String): ProjectPresentation? = repository.findById(id)

    fun findProjectPresentations(filter: ListProjectPresentationsFilter): Flow<ProjectPresentation> = repository.findUsingFilter(filter)

    suspend fun createProjectPresentation(projectPresentation: ProjectPresentation): ProjectPresentation =
        repository.save(projectPresentation)

    suspend fun updateProjectPresentation(
        id: String,
        updateData: ProjectPresentation,
        requestingContributor: A6Contributor,
    ): ProjectPresentation? {
        val projectPresentationToUpdate =
            repository.findForContributorUsingFilter(
                ListProjectPresentationsFilter(
                    listOf(updateData.projectId),
                    null,
                    setOf(requestingContributor.contributorId),
                    listOf(id),
                ),
                requestingContributor,
            )

        return projectPresentationToUpdate?.updateWithData(updateData)?.let { repository.save(it) }
    }

    private fun ProjectPresentation.updateWithData(other: ProjectPresentation): ProjectPresentation {
        this.referenceName = other.referenceName
        this.sections = other.sections
        return this
    }
}
