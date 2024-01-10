package com.angorasix.projects.presentation.integration

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.presentation.ProjectsPresentationApplication
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.infrastructure.config.configurationproperty.api.ApiConfigs
import com.angorasix.projects.presentation.integration.utils.IntegrationProperties
import com.angorasix.projects.presentation.integration.utils.initializeMongodb
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.PresentationSectionDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationQueryParams
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.fail
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.hateoas.MediaTypes
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import reactor.core.publisher.Mono

@SpringBootTest(
    classes = [ProjectsPresentationApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
)
@TestPropertySource(locations = ["classpath:integration-application.properties"])
@EnableConfigurationProperties(IntegrationProperties::class)
class ProjectsPresentationIntegrationTest(
    @Autowired val mongoTemplate: ReactiveMongoTemplate,
    @Autowired val mapper: ObjectMapper,
    @Autowired val properties: IntegrationProperties,
    @Autowired val webTestClient: WebTestClient,
    @Autowired val apiConfigs: ApiConfigs,
) {

    @BeforeAll
    fun setUp() = runBlocking {
        initializeMongodb(
            properties.mongodb.baseJsonFile,
            mongoTemplate,
            mapper,
        )
    }

    @Test
    fun `given base data - when call Get Project Presentation list - then return all persisted projects`() {
        webTestClient.get()
            .uri("/projects-presentation")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.size()").value(greaterThanOrEqualTo(2))
            .jsonPath("$..id").exists()
            .jsonPath("$..projectId").value(hasItems("123withSingleSection", "345MultipleSections"))
            .jsonPath("$..sections..description")
            .value(hasItem("Our objective at this stage includes one simple premise: We want to help people create"))
            .jsonPath("$..sections..title")
            .value(hasItem("Introduction to a project presentation for a particular segment"))
    }

    @Test
    fun `given base data - when call Get Project Presentation list filtering by projectId - then return filtered persisted projects`() {
        webTestClient.get()
            .uri { builder ->
                builder.path("/projects-presentation").queryParam(
                    ProjectPresentationQueryParams.PROJECT_IDS.param,
                    "123withSingleSection",
                ).build()
            }
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.size()").value(greaterThanOrEqualTo(1))
            .jsonPath("$..projectId").value(hasItems("123withSingleSection"))
    }

    @Test
    fun `given base data - when retrieve Presentation by id - then existing is retrieved`() {
        val initElementQuery = Query()
        initElementQuery.addCriteria(
            Criteria.where("referenceName")
                .`is`("Project Presentation aimed to devs"),
        )
        val elementId =
            mongoTemplate.findOne(initElementQuery, ProjectPresentation::class.java).block()?.id

        webTestClient.get()
            .uri("/projects-presentation/{projectPresentationId}", elementId)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.id")
            .exists()
            .jsonPath("$.projectId")
            .isEqualTo("123withSingleSection")
            .jsonPath("$.referenceName")
            .isEqualTo("Project Presentation aimed to devs")
            .jsonPath("$..sections..description")
            .value(hasItem("This is our objective"))
            .jsonPath("$..sections..title")
            .value(hasItem("Join a great project!"))
            .jsonPath("$.sections[0].media.size()")
            .isEqualTo(3)
            .jsonPath("sections[0].mainMedia.resourceId")
            .exists()
            .jsonPath("sections[0].mainMedia.thumbnailUrl")
            .exists()
            .jsonPath("sections[0].mainMedia.url")
            .exists()
            .jsonPath("sections[0].mainMedia.mediaType")
            .exists()
    }

    @Test
    fun `given base data - when get non-existing Presentation - then 404 response`() {
        webTestClient.get()
            .uri("/projects-presentation/non-existing-id")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound
    }

    @Test
    fun `when post new Project Presentation - then new project presentation is persisted`() {
        val projectPresentationBody = ProjectPresentationDto(
            "567",
            setOf(SimpleContributor("1", emptySet())),
            "newReferenceName",
            listOf(
                PresentationSectionDto(
                    "introduction",
                    "this is a mocked project",
                    listOf(
                        PresentationMediaDto(
                            "image",
                            "http://an.image.jpg",
                            "http://an.image.jpg",
                            "an.image.jpg",
                        ),
                    ),
                    PresentationMediaDto(
                        "video.youtube",
                        "https://www.youtube.com/watch?v=tHisis4R3soURCeId",
                        "http://a.video.jpg",
                        "tHisis4R3soURCeId",
                    ),
                ),
            ),
        )
        webTestClient.post()
            .uri("/projects-presentation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .body(
                Mono.just(projectPresentationBody),
                ProjectPresentationDto::class.java,
            )
            .exchange()
            .expectStatus().isCreated.expectBody()
            .jsonPath("$.id").exists()
            .jsonPath("$.projectId").isEqualTo("567")
            .jsonPath("$.referenceName").isEqualTo("newReferenceName")
            .jsonPath("$..sections.size()").isEqualTo(1)
            .jsonPath("$.sections[0].title").isEqualTo("introduction")
            .jsonPath("$.sections[0].description").isEqualTo("this is a mocked project")
            .jsonPath("$.sections[0].media.size()").isEqualTo(1)
            .jsonPath("$.sections[0].mainMedia.mediaType").isEqualTo("video.youtube")
            .jsonPath("$.sections[0].mainMedia.url")
            .isEqualTo("https://www.youtube.com/watch?v=tHisis4R3soURCeId")
            .jsonPath("$.sections[0].mainMedia.thumbnailUrl").isEqualTo("http://a.video.jpg")
            .jsonPath("$.sections[0].mainMedia.resourceId").isEqualTo("tHisis4R3soURCeId")
    }

    @Test
    fun `given new persisted presentation - when retrieved - then data matches`() {
        val projectPresentationBody = ProjectPresentationDto(
            "789",
            setOf(SimpleContributor("1", emptySet())),
            "referenceName2",
            listOf(
                PresentationSectionDto(
                    "introduction",
                    "this is a mocked project",
                    listOf(
                        PresentationMediaDto(
                            "image",
                            "http://an.image.jpg",
                            "http://an.image.jpg",
                            "an.image.jpg",
                        ),
                    ),
                    PresentationMediaDto(
                        "video.youtube",
                        "https://www.youtube.com/watch?v=tHisis4R3soURCeId",
                        "http://a.video.jpg",
                        "tHisis4R3soURCeId",
                    ),
                ),
            ),
        )
        val newProjectPresentation = webTestClient.post()
            .uri("/projects-presentation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .body(
                Mono.just(projectPresentationBody),
                ProjectPresentationDto::class.java,
            )
            .exchange()
            .expectStatus().isCreated
            .expectBody(ProjectPresentationDto::class.java)
            .returnResult().responseBody ?: fail("Create operation retrieved empty response")

        webTestClient.get()
            .uri("/projects-presentation/${newProjectPresentation.id}")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk.expectBody()
            .jsonPath("$.id").isEqualTo(newProjectPresentation.id!!)
            .jsonPath("$.projectId").isEqualTo("789")
            .jsonPath("$.referenceName").isEqualTo("referenceName2")
            .jsonPath("$..sections.size()").isEqualTo(1)
            .jsonPath("$.sections[0].title").isEqualTo("introduction")
            .jsonPath("$.sections[0].description").isEqualTo("this is a mocked project")
            .jsonPath("$.sections[0].media.size()").isEqualTo(1)
            .jsonPath("$.sections[0].mainMedia.mediaType").isEqualTo("video.youtube")
            .jsonPath("$.sections[0].mainMedia.url")
            .isEqualTo("https://www.youtube.com/watch?v=tHisis4R3soURCeId")
            .jsonPath("$.sections[0].mainMedia.thumbnailUrl").isEqualTo("http://a.video.jpg")
            .jsonPath("$.sections[0].mainMedia.resourceId").isEqualTo("tHisis4R3soURCeId")
    }

    @Test
    fun `when post new Project Presentation without sections - then Created response`() {
        val projectPresentationBody = """
            {
              "projectId": "projectId456",
              "referenceName": "mockedReferenceName"
            }
        """.trimIndent()
        webTestClient.post()
            .uri("/projects-presentation")
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(projectPresentationBody),
                String::class.java,
            )
            .exchange()
            .expectStatus().isCreated
    }

    @Test
    fun `when post new Project Presentation without referenceName - then Bad Request response`() {
        val projectPresentationBody = """
            {
              "projectId": "projectId456",
              "sections": []
            }
        """.trimIndent()
        webTestClient.post()
            .uri("/projects-presentation")
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .body(
                Mono.just(projectPresentationBody),
                String::class.java,
            )
            .exchange()
            .expectStatus().isBadRequest
            .expectBody()
            .jsonPath("$.errorCode").isEqualTo("PROJECT_PRESENTATION_INVALID")
            .jsonPath("$.error").exists()
            .jsonPath("$.status").isEqualTo(HttpStatus.BAD_REQUEST.value())
            .jsonPath("$.message").isEqualTo("ProjectPresentation referenceName expected")
    }

    @Test
    fun `when post new Project Presentation with empty sections - then Bad Request response`() {
        val projectPresentationBody = ProjectPresentationDto(
            "567",
            setOf(SimpleContributor("1", emptySet())),
            "referenceName",
            emptyList(),
        )
        webTestClient.post()
            .uri("/projects-presentation")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaTypes.HAL_FORMS_JSON)
            .body(
                Mono.just(projectPresentationBody),
                ProjectPresentationDto::class.java,
            )
            .exchange()
            .expectStatus().isCreated
    }
}
