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
    val text: String? = null, // search by text
    val adminId: Set<String>? = null,
    val ids: Collection<String>? = null, // presentation ids
)
