package com.angorasix.projects.presentation.integration

import com.angorasix.projects.presentation.presentation.controller.ProjectPresentationQueryParams
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.PresentationSectionDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.reactivestreams.client.MongoClients
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.apache.http.HttpStatus
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.Matchers.`is`
import org.hamcrest.Matchers.greaterThanOrEqualTo
import org.hamcrest.Matchers.hasItem
import org.hamcrest.Matchers.hasItems
import org.hamcrest.Matchers.notNullValue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.core.MediaType

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(MongodbResource::class)
class ProjectsPresentationIntegrationTest(
        @Inject @ConfigProperty(name = "quarkus.mongodb.connection-string") private val mongoConnString: String?,
        @Inject private val objectMapper: ObjectMapper?
) {

    @BeforeAll
    fun setup() {
        DbInitializer.initializeDb(
                MongoClients.create(mongoConnString),
                objectMapper!!
        )
    }

    @Test
    fun `given base data - when call Get Project Presentation list - then return all persisted projects`() {
        given().`when`()
                .get("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "$.size()",
                        greaterThanOrEqualTo(2),
                        "projectId",
                        hasItems("123withSingleSection", "345MultipleSections"),
                        "sections.description",
                        hasItem(hasItem("This is our objective")),
                        "sections.title",
                        hasItem(hasItem("Join a great project!")),
                )
    }

    @Test
    fun `given base data - when call Get Project Presentation list filtering by projectId - then return filtered persisted projects`() {
        given().`when`()
                .queryParam(ProjectPresentationQueryParams.PROJECT_IDS.param, "123withSingleSection")
                .get("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "$.size()",
                        greaterThanOrEqualTo(1),
                        "projectId",
                        hasItems("123withSingleSection"),
                )
    }

    @Test
    fun `when post new Project Presentation - then new project presentation is persisted`() {
        val projectPresentationBody = ProjectPresentationDto(
                "567",
                listOf(
                        PresentationSectionDto(
                                "introduction",
                                "this is a mocked project",
                                listOf(
                                        PresentationMediaDto(
                                                "image",
                                                "http://an.image.jpg",
                                                "http://an.image.jpg",
                                                "an.image.jpg"
                                        )
                                ),
                                PresentationMediaDto(
                                        "video.youtube",
                                        "https://www.youtube.com/watch?v=tHisis4R3soURCeId",
                                        "http://a.video.jpg",
                                        "tHisis4R3soURCeId"
                                )
                        )
                )
        )

        val response = given().body(projectPresentationBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .`when`()
                .post("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "id",
                        `notNullValue`(),
                        "projectId",
                        `is`("567"),
                        "sections.size()",
                        `is`(1),
                        "sections[0].title",
                        `is`("introduction"),
                        "sections[0].description",
                        `is`("this is a mocked project"),
                        "sections[0].media.size()",
                        `is`(1),
                        "sections[0].mainMedia.mediaType",
                        `is`("video.youtube"),
                        "sections[0].mainMedia.url",
                        `is`("https://www.youtube.com/watch?v=tHisis4R3soURCeId"),
                        "sections[0].mainMedia.thumbnailUrl",
                        `is`("http://a.video.jpg"),
                        "sections[0].mainMedia.resourceId",
                        `is`("tHisis4R3soURCeId")
                )
    }

    @Test
    fun `given base data - when retrieve Presentation by id - then existing is retrieved`() {
        given().`when`()
                .get("/projects-presentation/6178628cf8bc5c59d85948f1")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "id",
                        `is`("6178628cf8bc5c59d85948f1"),
                        "projectId",
                        `is`("123withSingleSection"),
                        "sections.description",
                        hasItem("This is our objective"),
                        "sections.title",
                        hasItem("Join a great project!"),
                        "sections[0].media.size()",
                        `is`(3),
                        "sections[0].mainMedia.resourceId",
                        notNullValue(),
                        "sections[0].mainMedia.thumbnailUrl",
                        notNullValue(),
                        "sections[0].mainMedia.url",
                        notNullValue(),
                        "sections[0].mainMedia.mediaType",
                        notNullValue()
                )
    }

    @Test
    fun `given base data - when get non-existing Presentation - then 404 response`() {
        given().`when`()
                .get("/projects-presentation/nonexistingid")
                .then()
                .statusCode(HttpStatus.SC_NOT_FOUND)
    }

    @Test
    fun `given new persisted presentation - when retrieved - then data matches`() {
        val projectPresentationBody = ProjectPresentationDto(
                "789",

                listOf(
                        PresentationSectionDto(
                                "introduction",
                                "this is a mocked project",
                                listOf(
                                        PresentationMediaDto(
                                                "image",
                                                "http://an.image.jpg",
                                                "http://an.image.jpg",
                                                "an.image.jpg"
                                        )
                                ),
                                PresentationMediaDto(
                                        "video.youtube",
                                        "https://www.youtube.com/watch?v=tHisis4R3soURCeId",
                                        "http://a.video.jpg",
                                        "tHisis4R3soURCeId"
                                )
                        )
                )
        )

        val newProject = given().body(projectPresentationBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .`when`()
                .post("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .extract().`as`<ProjectPresentationDto>(ProjectPresentationDto::class.java)

        given().`when`()
                .get("/projects-presentation/${newProject.id}")
                .then()
                .statusCode(HttpStatus.SC_OK)
                .body(
                        "id",
                        `is`(newProject.id),
                        "projectId",
                        `is`("789"),
                        "sections.size()",
                        `is`(1),
                        "sections[0].title",
                        `is`("introduction"),
                        "sections[0].description",
                        `is`("this is a mocked project"),
                        "sections[0].media.size()",
                        `is`(1),
                        "sections[0].mainMedia.mediaType",
                        `is`("video.youtube"),
                        "sections[0].mainMedia.url",
                        `is`("https://www.youtube.com/watch?v=tHisis4R3soURCeId"),
                        "sections[0].mainMedia.thumbnailUrl",
                        `is`("http://a.video.jpg"),
                        "sections[0].mainMedia.resourceId",
                        `is`("tHisis4R3soURCeId")
                )
    }

    @Test
    fun `when post new Project Presentation without sections - then Bad Request response`() {
        val response = given().body(
                "{\n" +
                        "    \"projectId\": \"projectId456\"\n" +
                        "}"
        )
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .`when`()
                .post("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(
                        "title",
                        notNullValue(),
                        "status",
                        `is`(HttpStatus.SC_BAD_REQUEST)
                )
    }

    @Test
    fun `when post new Project Presentation with empty sections - then Bad Request response`() {
        val projectPresentationBody = ProjectPresentationDto(
                "567",
                emptyList()
        )
        val response = given().body(projectPresentationBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .`when`()
                .post("/projects-presentation")
                .then()
                .statusCode(HttpStatus.SC_BAD_REQUEST)
                .body(
                        "status",
                        `is`(HttpStatus.SC_BAD_REQUEST),
                        "title",
                        notNullValue()
                )
    }
}
