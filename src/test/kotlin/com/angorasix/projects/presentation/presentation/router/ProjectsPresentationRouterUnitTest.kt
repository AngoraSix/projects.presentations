package com.angorasix.projects.presentation.presentation.router

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.BasePathConfigs
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.Route
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.angorasix.projects.presentation.presentation.handler.ProjectsPresentationHandler
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.http.HttpMethod
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest.builder
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.EntityResponse
import java.net.URI

@ExtendWith(MockKExtension::class)
class ProjectsPresentationRouterUnitTest {

    private lateinit var router: ProjectsPresentationRouter

    @MockK
    private lateinit var apiConfigs: ApiConfigs

    @MockK
    private lateinit var handler: ProjectsPresentationHandler

    private var routeConfigs: RoutesConfigs = RoutesConfigs(
        "",
        "/{id}",
        Route("mocked-create", listOf("mocked-base1"), HttpMethod.POST, ""),
        Route("mocked-update", listOf("mocked-base1"), HttpMethod.PUT, "/{id}"),
        Route("mocked-get-single", listOf("mocked-base1"), HttpMethod.GET, "/{id}"),
        Route("mocked-list-project", listOf("mocked-base1"), HttpMethod.GET, ""),
    )
    private var basePathsConfigs: BasePathConfigs = BasePathConfigs("/projects-presentation")

    @BeforeEach
    fun init() {
        every { apiConfigs.routes } returns routeConfigs
        every { apiConfigs.basePaths } returns basePathsConfigs
        router = ProjectsPresentationRouter(handler, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given Project router - When expected APIs requested - Then router routes correctly`() =
        runTest {
            val outputRouter = router.projectRouterFunction()
            val mockedRequest = MockServerHttpRequest.get("/mocked")
            val mockedExchange = MockServerWebExchange.builder(mockedRequest)
                .build()
            val getAllProjectsRequest = builder().uri(URI("/projects-presentation/"))
                .exchange(mockedExchange)
                .build()
            val getSingleProjectRequest = builder().uri(URI("/projects-presentation/1"))
                .exchange(mockedExchange)
                .build()
            val getCreateProjectRequest = builder().method(HttpMethod.POST)
                .uri(URI("/projects-presentation/"))
                .exchange(mockedExchange)
                .body(
                    ProjectPresentationDto(
                        "testProjectId",
                        setOf(SimpleContributor("1", emptySet())),
                        "testProjectPresentationName",
                        emptyList(),
                    ),
                )

            val getUpdateProjectRequest = builder().method(HttpMethod.PUT)
                .uri(URI("/projects-presentation/1"))
                .exchange(mockedExchange)
                .body(
                    ProjectPresentationDto(
                        "testProjectId",
                        setOf(SimpleContributor("1", emptySet())),
                        "testProjectPresentationName",
                        emptyList(),
                    ),
                )
            val invalidRequest = builder().uri(URI("/invalid-path"))
                .exchange(mockedExchange)
                .build()
            val mockedResponse = EntityResponse.fromObject("any").build().awaitSingle()
            coEvery { handler.listProjectPresentations(getAllProjectsRequest) } returns mockedResponse
            coEvery { handler.getProjectPresentation(getSingleProjectRequest) } returns mockedResponse
            coEvery { handler.createProjectPresentation(getCreateProjectRequest) } returns mockedResponse
            coEvery { handler.updateProjectPresentation(getUpdateProjectRequest) } returns mockedResponse

            // if routes don't match, they will throw an exception as with the invalid Route no need to assert anything
            outputRouter.route(getAllProjectsRequest)
                .awaitSingle().handle(getAllProjectsRequest)
                .awaitSingle()
            outputRouter.route(getSingleProjectRequest)
                .awaitSingle().handle(getSingleProjectRequest)
                .awaitSingle()
            outputRouter.route(getCreateProjectRequest)
                .awaitSingle().handle(getCreateProjectRequest)
                .awaitSingle()
            outputRouter.route(getUpdateProjectRequest)
                .awaitSingle().handle(getUpdateProjectRequest)
                .awaitSingle()
            // disabled until junit-jupiter 5.7.0 is released and included to starter dependency
            assertThrows<NoSuchElementException> {
                outputRouter.route(invalidRequest)
                    .awaitSingle()
            }
        }
}
