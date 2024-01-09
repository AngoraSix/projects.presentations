package com.angorasix.projects.presentation.application

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentationRepository
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import kotlinx.coroutines.flow.Flow

/**
 *
 *
 * @author rozagerardo
 */
class ProjectsPresentationService(private val repository: ProjectPresentationRepository) {

    suspend fun findSingleProjectPresentation(id: String): ProjectPresentation? =
        repository.findById(id)

    fun findProjectPresentations(filter: ListProjectPresentationsFilter): Flow<ProjectPresentation> =
        repository.findUsingFilter(filter)

    suspend fun createProjectPresentation(projectPresentation: ProjectPresentation): ProjectPresentation =
        repository.save(projectPresentation)

    suspend fun updateProjectPresentation(
            id: String,
            updateData: ProjectPresentation,
            requestingContributor: SimpleContributor
    ): ProjectPresentation? {

        val projectPresentationToUpdate = repository.findByIdForContributor(
                ListProjectPresentationsFilter(
                        listOf(updateData.projectId),
                        null,
                        listOf(requestingContributor.contributorId),
                        listOf(id)
                ),
                requestingContributor,
        ) //?: throw IllegalArgumentException("Query didn't match any Project Presentation")

        return projectPresentationToUpdate?.updateWithData(updateData)?.let { repository.save(it) }

    }

    private fun ProjectPresentation.updateWithData(other: ProjectPresentation): ProjectPresentation {
        this.referenceName = other.referenceName
        this.sections = other.sections
        return this
    }
}
