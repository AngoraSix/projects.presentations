package com.angorasix.projects.presentation.application

import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.contributors.domain.contributor.ProjectPresentationRepository
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.bson.types.ObjectId
import javax.enterprise.context.ApplicationScoped

/**
 *
 *
 * @author rozagerardo
 */
@ApplicationScoped
class ProjectsPresentationService(private val repository: ProjectPresentationRepository) {

    fun findSingleProjectPresentation(id: String): Uni<ProjectPresentation>? {
        return repository.findById(ObjectId(id))
    }

    fun findProjectPresentations(): Multi<ProjectPresentation>? {
        return repository.streamAll()
    }

    fun createProjectPresentations(projectPresentation: ProjectPresentation): Uni<ProjectPresentation>? {
        return repository.persist(projectPresentation)
    }
}
