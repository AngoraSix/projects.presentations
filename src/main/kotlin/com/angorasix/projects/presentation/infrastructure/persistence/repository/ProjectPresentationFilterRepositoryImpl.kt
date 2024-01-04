package com.angorasix.projects.presentation.infrastructure.persistence.repository

import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ProjectPresentationFilterRepositoryImpl(val mongoOps: ReactiveMongoOperations) :
    ProjectPresentationFilterRepository {

    override fun findUsingFilter(filter: ListProjectPresentationsFilter): Flow<ProjectPresentation> {
        return mongoOps.find(filter.toQuery(), ProjectPresentation::class.java).asFlow()
    }
}

private fun ListProjectPresentationsFilter.toQuery(): Query {
    val query = Query()
    projectIds?.let { query.addCriteria(where("projectId").`in`(it)) }
    text?.let {
        val titleCriteria = where("sections.title").regex(it, "i")
        val descriptionCriteria = where("sections.description").regex(it, "i")
        val titleOrDescriptionCriteria = Criteria().orOperator(titleCriteria, descriptionCriteria)
        query.addCriteria(titleOrDescriptionCriteria)
    }
    return query
}
