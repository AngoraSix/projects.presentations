package com.angorasix.projects.presentation.integration

import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.reactivestreams.client.MongoClients
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.Matchers.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.net.http.HttpHeaders
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
    fun `given base data - when call Get Project Presentation with id 1 - then return persisted project`() { // val path = Paths.get("").toAbsolutePath().toString()
        // println(path)
        // val file = File("$path/filename")

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
                )
    }

    @Test
    fun `given project presentation - when call Post Project Presentation - then new project presetation is persisted`() {
        val projectPresentationBody = ProjectPresentationDto("567", "an objective", listOf(PresentationMediaDto("image", "http://an.image.jpg")))

        given().body(projectPresentationBody).header(javax.ws.rs.core.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON).`when`()
                .post("/projects-presentation")
                .then()
                .statusCode(200)
                .body(
                        "id",
                        `notNullValue`(),
                        "projectId",
                        `is`("567"),
                        "objective",
                        `is`("an objective")
                )
    }
}
