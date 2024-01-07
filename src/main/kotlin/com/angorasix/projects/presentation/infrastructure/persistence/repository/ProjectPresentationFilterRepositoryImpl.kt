package com.angorasix.projects.presentation.infrastructure.persistence.repository

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitFirstOrNull
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

    override suspend fun findByIdForContributor(
            filter: ListProjectPresentationsFilter,
            simpleContributor: SimpleContributor?,
    ): ProjectPresentation? {
        return mongoOps.find(filter.toQuery(simpleContributor), ProjectPresentation::class.java)
                .awaitFirstOrNull()
    }
}

private fun ListProjectPresentationsFilter.toQuery(simpleContributor: SimpleContributor? = null): Query {
    val query = Query()

    val requestingOthers = adminId == null || !adminId.contains(simpleContributor?.contributorId)
    val requestingOwn =
            simpleContributor != null && (adminId.isNullOrEmpty() || adminId.contains(simpleContributor.contributorId))


    ids?.let { query.addCriteria(where("_id").`in`(it)) }

    projectIds?.let { query.addCriteria(where("projectId").`in`(it)) }

    text?.let {
        val presentationNameCriteria = where("referenceName").regex(it, "i")
        val titleCriteria = where("sections.title").regex(it, "i")
        val descriptionCriteria = where("sections.description").regex(it, "i")
        val titleOrDescriptionOrNameCriteria = Criteria().orOperator(titleCriteria, descriptionCriteria, presentationNameCriteria)
        query.addCriteria(titleOrDescriptionOrNameCriteria)
    }

    if (requestingOthers) {
        return query
    }

    if (requestingOwn) {
        query.addCriteria(where("admins").elemMatch(where("contributorId").`is`(simpleContributor?.contributorId)))
    }

    return query
}
