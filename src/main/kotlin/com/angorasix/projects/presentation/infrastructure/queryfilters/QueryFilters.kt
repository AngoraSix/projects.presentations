package com.angorasix.projects.presentation.infrastructure.queryfilters

/**
 * <p>
 *     Classes containing different Request Query Filters.
 * </p>
 *
 * @author rozagerardo
 */
data class ListProjectPresentationsFilter(
        val projectIds: Collection<String>? = null,
)