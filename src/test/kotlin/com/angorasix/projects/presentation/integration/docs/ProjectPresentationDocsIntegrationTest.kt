package com.angorasix.projects.presentation.integration.docs

import com.angorasix.projects.presentation.ProjectsPresentationApplication
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.integration.utils.IntegrationProperties
import com.angorasix.projects.presentation.integration.utils.initializeMongodb
import com.angorasix.projects.presentation.utils.mockPresentationDto
import com.angorasix.projects.presentation.utils.mockRequestingContributorHeader
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.hateoas.MediaTypes
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.restdocs.RestDocumentationContextProvider
import org.springframework.restdocs.RestDocumentationExtension
import org.springframework.restdocs.headers.HeaderDocumentation.headerWithName
import org.springframework.restdocs.headers.HeaderDocumentation.responseHeaders
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.halLinks
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.linkWithRel
import org.springframework.restdocs.hypermedia.HypermediaDocumentation.links
import org.springframework.restdocs.operation.preprocess.Preprocessors.preprocessResponse
import org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint
import org.springframework.restdocs.payload.FieldDescriptor
import org.springframework.restdocs.payload.JsonFieldType
import org.springframework.restdocs.payload.PayloadDocumentation.beneathPath
import org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath
import org.springframework.restdocs.payload.PayloadDocumentation.requestFields
import org.springframework.restdocs.payload.PayloadDocumentation.responseFields
import org.springframework.restdocs.payload.PayloadDocumentation.subsectionWithPath
import org.springframework.restdocs.request.RequestDocumentation.parameterWithName
import org.springframework.restdocs.request.RequestDocumentation.pathParameters
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.document
import org.springframework.restdocs.webtestclient.WebTestClientRestDocumentation.documentationConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.body
import org.springframework.web.reactive.function.client.ExchangeFilterFunction
import reactor.core.publisher.Mono
import java.time.Duration

@ExtendWith(RestDocumentationExtension::class)
@SpringBootTest(
    classes = [ProjectsPresentationApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectPresentationDocsIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val apiConfigs: ApiConfigs,
) {

    private lateinit var webTestClient: WebTestClient

    var mediaDescription = arrayOf<FieldDescriptor>(
        fieldWithPath("mediaType").description("The type of media (image, youtube.video, etc)"),
        fieldWithPath("url").description("URL of the media"),
        fieldWithPath("thumbnailUrl").description("URL for a thumbnail of the media"),
        fieldWithPath("resourceId").description("Identifier of the resource"),
    )

    var sectionDescription = arrayOf<FieldDescriptor>(
        fieldWithPath("title").description("Title of the presentation section"),
        fieldWithPath("description").description("Description of the presentation section"),
        subsectionWithPath("mainMedia").type(PresentationMedia::class.simpleName)
            .description("A main media for the section"),
        subsectionWithPath("media[]").type(ArrayOfFieldType(PresentationMedia::class.simpleName))
            .description("Array of the media associated to the section"),
    )

    var projectDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("referenceName").description("Internal reference name for the presentation"),
        fieldWithPath("id").description("Project presentation identifier"),
        fieldWithPath("projectId").description("Identifier of the associated Project Id"),
        subsectionWithPath("sections[]").type(ArrayOfFieldType(PresentationSection::class.simpleName))
            .description("Array of the sections that form the project presentation"),
        subsectionWithPath("links").optional().description("HATEOAS links")
            .type(JsonFieldType.ARRAY),// until we resolve and unify the list and single response links, all will be marked as optional
        subsectionWithPath("_links").optional().description("HATEOAS links")
            .type(JsonFieldType.OBJECT),
        subsectionWithPath("_templates").optional()
            .description("HATEOAS HAL-FORM links template info").type(
                JsonFieldType.OBJECT,
            ),
    )

    var projectPostBodyDescriptor = arrayOf<FieldDescriptor>(
        fieldWithPath("referenceName").description("Reference Name of the project"),
        fieldWithPath("id").ignored(),
        fieldWithPath("projectId").description("The identifier of the associated Project"),
        subsectionWithPath("sections[]").type(ArrayOfFieldType(PresentationSection::class.simpleName))
            .description("Array of the sections that form the project presentation"),
        fieldWithPath("links[]").ignored(),
    )

    @BeforeAll
    fun setUpDb() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper,
        )
    }

    @BeforeEach
    fun setUpWebClient(
        applicationContext: ApplicationContext,
        restDocumentation: RestDocumentationContextProvider,
    ) = runBlocking {
        webTestClient = WebTestClient.bindToApplicationContext(applicationContext)
            .configureClient()
            .responseTimeout(Duration.ofMillis(30000))
            .filter(
                documentationConfiguration(restDocumentation),
            )
            .filter(
                ExchangeFilterFunction.ofRequestProcessor { clientRequest ->
                    println(
                        "Request: ${clientRequest.method()} ${clientRequest.url()}",
                    )
                    clientRequest.headers()
                        .forEach { name, values ->
                            values.forEach { value ->
                                println(
                                    "$name=$value",
                                )
                            }
                        }
                    Mono.just(clientRequest)
                },
            )
            .build()
    }

    @Test
    fun `Given persisted projects - When execute and document requests - Then everything documented`() {
        executeAndDocumentGetListProjectsRequest()
        executeAndDocumentGetSingleProjectRequest()
        executeAndDocumentPostCreateProjectRequest()
    }

    private fun executeAndDocumentPostCreateProjectRequest() {
        val newProjectPresentation = mockPresentationDto()
        webTestClient.post()
            .uri(
                "/projects-presentation/",
            )
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .header(apiConfigs.headers.contributor, mockRequestingContributorHeader(true))
            .body(Mono.just(newProjectPresentation))
            .exchange()
            .expectStatus().isCreated.expectBody()
            .consumeWith(
                document(
                    "project-create",
                    preprocessResponse(prettyPrint()),
                    requestFields(*projectPostBodyDescriptor),
                    responseHeaders(
                        headerWithName(HttpHeaders.LOCATION).description("URL of the newly created project"),
                    ),
                    links(
                        halLinks(),
                        linkWithRel("self").description("The self link"),
                        linkWithRel("updateProjectPresentation").description("The link for the edit presentation operation"),
                    ),
                    responseFields(*projectDescriptor),
                ),
            )
    }

    private fun executeAndDocumentGetSingleProjectRequest() {
        val initElementQuery = Query()
        initElementQuery.addCriteria(
            Criteria.where("referenceName")
                .`is`("Project Presentation aimed to devs"),
        )
        val elementId =
            mongoTemplate.findOne(initElementQuery, ProjectPresentation::class.java).block()?.id

        webTestClient.get()
            .uri(
                "/projects-presentation/{projectPresentationId}",
                elementId,
            )
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .consumeWith(
                document(
                    "project-single",
                    preprocessResponse(prettyPrint()),
                    pathParameters(parameterWithName("projectPresentationId").description("The Project Presntation id")),
                    responseFields(*projectDescriptor),
                ),
            )
    }

    private fun executeAndDocumentGetListProjectsRequest() {
        webTestClient.get()
            .uri("/projects-presentation/")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .consumeWith(
                document(
                    "project-list",
                    preprocessResponse(prettyPrint()),
                    responseFields(
                        fieldWithPath("[]").type(ArrayOfFieldType(ProjectPresentation::class.simpleName))
                            .description("An array of projects"),
                    ).andWithPrefix(
                        "[].",
                        *projectDescriptor,
                    ),
                    responseFields(
                        beneathPath("[].sections[]").withSubsectionId("section"),
                    ).andWithPrefix(
                        "[].",
                        *sectionDescription,
                    ),
                    responseFields(
                        beneathPath("[].sections[].mainMedia").withSubsectionId("media"),
                        *mediaDescription,
                    ),
                ),
            )
    }

    private class ArrayOfFieldType(private val field: String?) {
        override fun toString(): String = "Array of $field"
    }
}
