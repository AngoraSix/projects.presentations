package com.angorasix.projects.presentation.integration

import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.reactivestreams.client.MongoClients
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
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
    fun `given base data - when call Get Project Presentation list - then return persisted project`() {
        given().`when`()
            .get("/projects-presentation")
            .then()
            .statusCode(200)
            .body(
                "$.size()",
                greaterThanOrEqualTo(2),
                "projectId",
                hasItems("123", "345"),
                "objective",
                hasItem("This is our objective"),
                "title.size()",
                greaterThanOrEqualTo(2),
            )
    }

    @Test
    fun `when post new Project Presentation - then new project presetation is persisted`() {
        val projectPresentationBody = ProjectPresentationDto(
            "567",
            "a title",
            "an objective",
            listOf(PresentationMediaDto("image", "http://an.image.jpg"))
        )

        val response = given().body(projectPresentationBody)
            .header(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .`when`()
            .post("/projects-presentation")
            .then()
            .statusCode(200)
            .body(
                "id",
                `notNullValue`(),
                "projectId",
                `is`("567"),
                "objective",
                `is`("an objective"),
                "title",
                `is`("a title")
            )
    }

    @Test
    fun `given base data - when retrieve Presentation by id - then existing is retrieved`() {
        given().`when`()
            .get("/projects-presentation/6178628cf8bc5c59d85948f1")
            .then()
            .statusCode(200)
            .body(
                "id",
                `is`("6178628cf8bc5c59d85948f1"),
                "projectId",
                `is`("123"),
                "objective",
                `is`("This is our objective"),
                "title",
                `is`("Join a great project!"),
                "media.size()",
                `is`(2)
            )
    }

    @Test
    fun `given base data - when get non-existing Presentation - then 404 response`() {
        given().`when`()
            .get("/projects-presentation/nonexistingid")
            .then()
            .statusCode(404)
    }

    @Test
    fun `given new persisted presentation - when retrieved - then data matches`() {
        val projectPresentationBody = ProjectPresentationDto(
            "789",
            "another title",
            "another objective",
            listOf(PresentationMediaDto("image", "http://an.image.jpg"))
        )

        val newProject = given().body(projectPresentationBody)
            .header(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
            .`when`()
            .post("/projects-presentation")
            .then()
            .statusCode(200)
            .extract().`as`<ProjectPresentationDto>(ProjectPresentationDto::class.java)

        given().`when`()
            .get("/projects-presentation/${newProject.id}")
            .then()
            .statusCode(200)
            .body(
                "id",
                `is`(newProject.id),
                "projectId",
                `is`("789"),
                "objective",
                `is`("another objective"),
                "title",
                `is`("another title"),
                "media.size()",
                `is`(1)
            )
    }
}
