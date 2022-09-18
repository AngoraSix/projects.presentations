package com.angorasix.projects.presentation.domain.projectpresentation

import com.angorasix.projects.presentation.infrastructure.persistence.repository.ProjectPresentationFilterRepository
import org.springframework.data.repository.kotlin.CoroutineSortingRepository

/**
 *
 *
 * @author rozagerardo
 */
interface ProjectPresentationRepository :
    CoroutineSortingRepository<ProjectPresentation, String>,
    ProjectPresentationFilterRepository
