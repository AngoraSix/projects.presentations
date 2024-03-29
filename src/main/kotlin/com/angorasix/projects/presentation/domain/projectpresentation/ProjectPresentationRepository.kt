package com.angorasix.projects.presentation.domain.projectpresentation

import com.angorasix.projects.presentation.infrastructure.persistence.repository.ProjectPresentationFilterRepository
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 *
 *
 * @author rozagerardo
 */
interface ProjectPresentationRepository :
    CoroutineCrudRepository<ProjectPresentation, String>,
    CoroutineSortingRepository<ProjectPresentation, String>,
    ProjectPresentationFilterRepository
