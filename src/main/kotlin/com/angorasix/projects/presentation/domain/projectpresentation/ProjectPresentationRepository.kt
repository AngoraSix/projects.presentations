package com.angorasix.contributors.domain.contributor

import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoRepository
import javax.enterprise.context.ApplicationScoped

/**
 *
 *
 * @author rozagerardo
 */
@ApplicationScoped
class ProjectPresentationRepository : ReactivePanacheMongoRepository<ProjectPresentation>
