package com.angorasix.projects.presentation.application

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
    ): ProjectPresentation? {
        val projectPresentationToUpdate =
            repository.findById(id).takeIf { it?.projectId == updateData.projectId }
                ?: throw IllegalArgumentException("Provided 'projectId' doesn't match the Project Presentation one")
        return projectPresentationToUpdate.updateWithData(updateData).let { repository.save(it) }
    }

    private fun ProjectPresentation.updateWithData(other: ProjectPresentation): ProjectPresentation {
        this.referenceName = other.referenceName
        this.sections = other.sections
        return this
    }
}
