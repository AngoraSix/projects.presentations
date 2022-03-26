package com.angorasix.projects.presentation.domain.projectpresentation

/**
 * <p>
 *     Describing a particular section/facet of the Project Presentation,
 *     e.g. "Overview", "Our vision", "Our objectives", "What is an Angorasix Project?"
 * </p>
 *
 * @author rozagerardo
 */
class PresentationSection(
        val title: String,
        val description: String?,
        val media: Collection<PresentationMedia> = emptyList(),
        val mainMedia: PresentationMedia?,
)