package com.angorasix.projects.presentation.integration

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.`is`
import org.junit.jupiter.api.Test

@QuarkusTest
class ProjectsPresentationIntegrationTest {

    @Test
    fun `given base data - when call Get Project Presentation with id 1 - then return persisted project`() {
        given()
            .`when`().get("/hello-resteasy-reactive")
          .then()
             .statusCode(200)
             .body(`is`("Hello RESTEasy Reactive"))
    }

}
