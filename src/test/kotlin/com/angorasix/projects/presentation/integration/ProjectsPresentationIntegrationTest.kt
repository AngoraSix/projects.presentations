package com.angorasix.projects.presentation.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.reactivestreams.client.MongoClients
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.eclipse.microprofile.config.inject.ConfigProperty
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import javax.inject.Inject

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
                `is`(1),
                "[0].projectId",
                `is`("123"),
            )
    }
}
