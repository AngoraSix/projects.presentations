package com.angorasix.projects.presentation.presentation.handler

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.commons.infrastructure.constants.AngoraSixInfrastructure
import com.angorasix.projects.presentation.application.ProjectsPresentationService
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.Route
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.RoutesConfigs
import com.angorasix.projects.presentation.infrastructure.queryfilters.ListProjectPresentationsFilter
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.angorasix.projects.presentation.utils.mockPresentation
import com.angorasix.projects.presentation.utils.mockPresentationDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.reactor.mono
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.mediatype.problem.Problem
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.mock.http.server.reactive.MockServerHttpRequest
import org.springframework.mock.web.reactive.function.server.MockServerRequest
import org.springframework.mock.web.server.MockServerWebExchange
import org.springframework.web.reactive.function.server.EntityResponse
import org.springframework.web.reactive.function.server.ServerRequest

@ExtendWith(MockKExtension::class)
@ExperimentalCoroutinesApi
class ProjectsPresentationHandlerUnitTest {

    private lateinit var handler: ProjectsPresentationHandler

    @MockK
    private lateinit var service: ProjectsPresentationService

    @MockK
    private lateinit var apiConfigs: ApiConfigs

    private var routeConfigs: RoutesConfigs = RoutesConfigs(
        "",
        "/{id}",
        Route("mocked-create", listOf("mocked-base1"), HttpMethod.POST, ""),
        Route("mocked-update", listOf("mocked-base1"), HttpMethod.PUT, "/{id}"),
        Route("mocked-get-single", listOf("mocked-base1"), HttpMethod.GET, "/{id}"),
        Route("mocked-list-project", listOf("mocked-base1"), HttpMethod.GET, ""),
    )

    @BeforeEach
    fun init() {
        every { apiConfigs.routes } returns routeConfigs
        handler = ProjectsPresentationHandler(service, apiConfigs)
    }

    @Test
    @Throws(Exception::class)
    fun `Given existing project presentations - When list presentations - Then handler retrieves Ok Response`() =
        runTest {
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.listProjectPresentations.path).build(),
            )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange).build()
            val mockedProjectPresentation =
                mockPresentation()
            val retrievedProjectPresentation = flowOf(mockedProjectPresentation)
            coEvery { service.findProjectPresentations(ListProjectPresentationsFilter()) } returns retrievedProjectPresentation

            val outputResponse = handler.listProjectPresentations(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<Flow<ProjectPresentationDto>>
            val responseBody = response.entity()
            responseBody.collect {
                assertThat(it.referenceName).isEqualTo("mockedReferenceName")
                assertThat(it.projectId).isEqualTo("mockedProjectId")
                assertThat(it.sections?.size).isGreaterThanOrEqualTo(1)
            }
            coVerify { service.findProjectPresentations(ListProjectPresentationsFilter()) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When create project - Then handler retrieves Created`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectPresentationDto = mockPresentationDto()
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.createProjectPresentation.path).build(),
            )
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(
                    AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                    mockedSimpleContributor,
                )
                .exchange(mockedExchange).body(mono { mockedProjectPresentationDto })
            val mockedProjectPresentation = mockPresentation()
            coEvery { service.createProjectPresentation(ofType(ProjectPresentation::class)) } returns mockedProjectPresentation

            val outputResponse = handler.createProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.CREATED)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ProjectPresentationDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectPresentationDto)
            assertThat(responseBody.projectId).isEqualTo("mockedProjectId")
            assertThat(responseBody.referenceName).isEqualTo("mockedReferenceName")
            assertThat(responseBody.sections?.size).isGreaterThanOrEqualTo(1)
            coVerify { service.createProjectPresentation(ofType(ProjectPresentation::class)) }
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and no RequestingContributor - When create project - Then handler retrieves Bad Request`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectPresentationDto = mockPresentationDto()
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get(routeConfigs.createProjectPresentation.path).build(),
            )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange)
                    .body(mono { mockedProjectPresentationDto })
            val mockedProject = mockPresentation()
            coEvery { service.createProjectPresentation(ofType(ProjectPresentation::class)) } returns mockedProject

            val outputResponse = handler.createProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<EntityModel<Problem.ExtendedProblem<Any>>>
            val responseBody = response.entity()
            assertThat(responseBody.content?.status).isEqualTo(HttpStatus.BAD_REQUEST)
            var properties = responseBody.content?.properties as Map<String, Any>?
            assertThat(properties?.get("errorCode") as String).isEqualTo("CONTRIBUTOR_HEADER_INVALID")
            Unit
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with invalid project presentation - When update project presentation - Then handler retrieves Bad Request`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectPresentationDto =
                ProjectPresentationDto(
                    "mockedProjectId",
                    setOf(SimpleContributor("1", emptySet())),
                    null,
                    emptyList(),
                )
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/id1-mocked").build(),
            )
            val mockedRequest: ServerRequest =
                MockServerRequest.builder().exchange(mockedExchange)
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    )
                    .pathVariable("id", "id1")
                    .body(mono { mockedProjectPresentationDto })
            val outputResponse = handler.createProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<EntityModel<Problem.ExtendedProblem<Any>>>
            val responseBody = response.entity()
            assertThat(responseBody.content?.status).isEqualTo(HttpStatus.BAD_REQUEST)
            var properties = responseBody.content?.properties as Map<String, Any>?
            assertThat(properties?.get("errorCode") as String).isEqualTo("PROJECT_PRESENTATION_INVALID")
            Unit
        }

    @Test
    @Throws(Exception::class)
    fun `Given request with project and RequestingContributor - When update project - Then handler retrieves Updated`() =
        runBlocking { // = runBlockingTest { // until we resolve why service.createProject is hanging https://github.com/Kotlin/kotlinx.coroutines/issues/1204
            val mockedProjectPresentationDto = mockPresentationDto()
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest = MockServerRequest.builder()
                .attribute(
                    AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                    mockedSimpleContributor,
                )
                .pathVariable("id", "id1").exchange(mockedExchange)
                .body(mono { mockedProjectPresentationDto })
            val mockedProjectPresentation = mockPresentation("Updated")
            coEvery {
                service.updateProjectPresentation(
                    "id1",
                    ofType(ProjectPresentation::class),
                )
            } returns mockedProjectPresentation

            val outputResponse = handler.updateProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val response = @Suppress("UNCHECKED_CAST")
            outputResponse as EntityResponse<ProjectPresentationDto>
            val responseBody = response.entity()
            assertThat(responseBody).isNotSameAs(mockedProjectPresentationDto)
            assertThat(responseBody.referenceName).isEqualTo("mockedReferenceNameUpdated")
            assertThat(responseBody.projectId).isEqualTo("mockedProjectIdUpdated")
            coVerify {
                service.updateProjectPresentation(
                    "id1",
                    ofType(ProjectPresentation::class),
                )
            }
        }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given existing projects - When get project for non Admin contributor - Then handler retrieves Ok Response without Edit link`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedId")
            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest.builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    )
                    .pathVariable("id", projectId).exchange(mockedExchange).build()
            val mockedProjectPresentation =
                mockPresentation()
            coEvery { service.findSingleProjectPresentation(projectId) } returns mockedProjectPresentation

            val outputResponse = handler.getProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectPresentationDto>).entity()
            assertThat(responseBody.referenceName).isEqualTo("mockedReferenceName")
            assertThat(responseBody.links.hasSize(1)).isTrue
            assertThat(responseBody.links.getLink("updateProject")).isEmpty
            coVerify { service.findSingleProjectPresentation(projectId) }
        }

    @Test
    @Throws(Exception::class)
    @kotlinx.coroutines.ExperimentalCoroutinesApi
    fun `Given existing projects - When get project for Admin Contributor - Then handler retrieves Ok Response with Edit link`() =
        runTest {
            val projectId = "projectId"
            val mockedSimpleContributor = SimpleContributor("mockedContributorId", emptySet())

            val mockedExchange =
                MockServerWebExchange.from(MockServerHttpRequest.get("/id1-mocked").build())
            val mockedRequest: ServerRequest =
                MockServerRequest.builder()
                    .attribute(
                        AngoraSixInfrastructure.REQUEST_ATTRIBUTE_CONTRIBUTOR_KEY,
                        mockedSimpleContributor,
                    )
                    .pathVariable("id", projectId).exchange(mockedExchange).build()
            val mockedProjectPresentation =
                mockPresentation()
            coEvery { service.findSingleProjectPresentation(projectId) } returns mockedProjectPresentation

            val outputResponse = handler.getProjectPresentation(mockedRequest)

            assertThat(outputResponse.statusCode()).isEqualTo(HttpStatus.OK)
            val responseBody =
                @Suppress("UNCHECKED_CAST")
                (outputResponse as EntityResponse<ProjectPresentationDto>).entity()
            assertThat(responseBody.referenceName).isEqualTo("mockedReferenceName")
            assertThat(responseBody.links.hasSize(2)).isTrue()
            assertThat(responseBody.links.getLink("updateProject")).isNotNull
            coVerify { service.findSingleProjectPresentation(projectId) }
        }
}
