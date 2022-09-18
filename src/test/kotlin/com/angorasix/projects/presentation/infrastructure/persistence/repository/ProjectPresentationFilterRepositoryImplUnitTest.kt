package com.angorasix.projects.presentation.infrastructure.persistence.repository

import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.test.runBlockingTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.data.mongodb.core.ReactiveMongoOperations
import org.springframework.data.mongodb.core.query.Query
import reactor.core.publisher.Flux

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
@ExtendWith(MockKExtension::class)
class ProjectPresentationFilterRepositoryImplUnitTest {

    private lateinit var filterRepoImpl: ProjectPresentationFilterRepository

    @MockK
    private lateinit var mongoOps: ReactiveMongoOperations

    val slot = slot<Query>()

    @BeforeEach
    fun init() {
        filterRepoImpl = ProjectPresentationFilterRepositoryImpl(mongoOps)
    }

    @Test
    @Throws(Exception::class)
    fun `Given empty ProjectFilter - When findUsingFilter - Then find repo operation with empty query`() =
        runBlockingTest {
            val filter = ListProjectPresentationsFilter()
            val mockedFlux = mockk<Flux<ProjectPresentation>>()
            every {
                mongoOps.find(
                    capture(slot),
                    ProjectPresentation::class.java,
                )
            } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, ProjectPresentation::class.java) }
            assertThat(capturedQuery.queryObject).isEmpty()
        }

    @Test
    @Throws(Exception::class)
    fun `Given populated ProjectFilter - When findUsingFilter - Then find repo operation with populated query`() =
        runBlockingTest {
            val filter = ListProjectPresentationsFilter(listOf("1", "2"))
            val mockedFlux = mockk<Flux<ProjectPresentation>>()
            every {
                mongoOps.find(
                    capture(slot),
                    ProjectPresentation::class.java,
                )
            } returns mockedFlux

            filterRepoImpl.findUsingFilter(filter)

            val capturedQuery = slot.captured

            verify { mongoOps.find(capturedQuery, ProjectPresentation::class.java) }
            assertThat(capturedQuery.queryObject).containsKey("projectId")
        }
}
