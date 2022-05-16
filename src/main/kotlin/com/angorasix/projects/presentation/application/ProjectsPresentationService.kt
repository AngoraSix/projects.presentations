package com.angorasix.projects.presentation.application

import com.angorasix.contributors.domain.contributor.ProjectPresentationRepository
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import org.bson.types.ObjectId
import org.jboss.logging.Logger
import javax.enterprise.context.ApplicationScoped
import javax.validation.Valid

/**
 *
 *
 * @author rozagerardo
 */
@ApplicationScoped
class ProjectsPresentationService(private val repository: ProjectPresentationRepository) {

    private val LOG: Logger = Logger.getLogger(ProjectsPresentationService::class.java)

    fun findSingleProjectPresentation(id: String): Uni<ProjectPresentation> {
        return if (ObjectId.isValid(id)) {
            repository.findById(ObjectId(id))
        } else {
            Uni.createFrom().nullItem()
        }
    }

    fun findProjectPresentations(filter: ListProjectPresentationsFilter): Multi<ProjectPresentation> {
        return filter.projectIds?.let {
            repository.stream("projectId in ?1", it)
        } ?: repository.streamAll()
    }

    fun createProjectPresentations(@Valid projectPresentation: ProjectPresentation): Uni<ProjectPresentation> {
        return repository.persist(projectPresentation)
    }

    fun updateProjectPresentation(id: String, updateData: ProjectPresentation): Uni<ProjectPresentation> {
        return if (ObjectId.isValid(id)) {
            repository.findById(ObjectId(id))
                    .onItem()
                    .ifNotNull()
                    .transformToUni { p ->
                        if (p.projectId != updateData.projectId) {
                            LOG.error("Trying to modify ProjectPresentation presenting incorrect 'projectId' field reference");
                            Uni.createFrom()
                                    .failure(IllegalArgumentException("Provided 'projectId' doesn't match the one assigned to the Project Presentation entity"))
                        } else {
                            repository.update(p.updateWithData(updateData))
                        }
                    }
        } else {
            Uni.createFrom().nullItem()
        }
    }

    private fun ProjectPresentation.updateWithData(other: ProjectPresentation): ProjectPresentation {
        this.referenceName = other.referenceName;
        this.sections = other.sections;
        return this;
    }
}
