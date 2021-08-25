package com.angorasix.projects.presentation.application

import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import io.smallrye.mutiny.Multi
import io.smallrye.mutiny.Uni
import javax.enterprise.context.ApplicationScoped

/**
 *
 *
 * @author rozagerardo
 */
@ApplicationScoped
class ProjectsPresentationService {

    fun findSingleProjectPresentation(id: String): Uni<ProjectPresentation>? {
        return Uni.createFrom()
            .item(id)
            .onItem()
            .transform { i: String? ->
                ProjectPresentation(
                    "newName",
                    "ProjectPresentation-Description",
                    listOf(
                        PresentationMedia(
                            "image",
                            "https://upload.wikimedia.org/wikipedia/commons/d/d2/Chojin_%283521733976%29.jpg"
                        )
                    )
                )
            }
    }

    fun findProjectPresentations(): Multi<ProjectPresentation>? {
        return Multi.createFrom()
            .items(
                ProjectPresentation(
                    "newName",
                    "project presentation-multi description",
                    listOf(
                        PresentationMedia(
                            "image",
                            "https://upload.wikimedia.org/wikipedia/commons/d/d2/Chojin_%283521733976%29.jpg"
                        )
                    )
                ),
                ProjectPresentation(
                    "otherProject",
                    "Another Project presentation description",
                    listOf(
                        PresentationMedia(
                            "image",
                            ("https://upload.wikimedia.org/wikipedia/commons/3/3a/Bob-Marley-in-Concert_Zurich_05-30-80.jpg")
                        )
                    )
                )
            )
    }
}
