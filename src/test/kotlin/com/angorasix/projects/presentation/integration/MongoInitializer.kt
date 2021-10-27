package com.angorasix.projects.presentation.integration

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.mongodb.client.result.InsertManyResult
import com.mongodb.reactivestreams.client.MongoClient
import io.quarkus.test.common.QuarkusTestResourceLifecycleManager
import io.quarkus.vertx.http.runtime.attribute.DateTimeAttribute.DATE_TIME
import org.bson.Document
import org.bson.types.ObjectId
import org.reactivestreams.Subscriber
import org.reactivestreams.Subscription
import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName
import java.text.SimpleDateFormat

/**
 *
 *
 * @author rozagerardo
 */
class MongodbResource : QuarkusTestResourceLifecycleManager {

    val db: MongoDBContainer = MongoDBContainer(DockerImageName.parse("mongo:5.0"))

    override fun start(): MutableMap<String, String> { // println("GERGERGER")
        // println(Paths.get("src/test/resources/mongo-init.js").toAbsolutePath().toString())
        // println(File(Paths.get("src/test/resources/mongo-init.js").toAbsolutePath().toString()).exists())

        // File(//"./src/main/resources/films.json").readText(Charsets.UTF_8)
        // val jsonEl = Json.parseToJsonElement(jsonString) // JACKSON??
        // Serviria mapear a BSON??? (tiene metodo para pasar Map -> Bson directamente
        db.start()
        return mutableMapOf(
            "quarkus.mongodb.connection-string" to "mongodb://${db.getContainerIpAddress()}:${db.getFirstMappedPort()}/a6-projectpresentations"
        )
    }

    override fun stop() {
        db.stop()
    }
}

class TestSubscriber : Subscriber<InsertManyResult> {
    override fun onSubscribe(s: Subscription?) {
        s?.request(1)
    }

    override fun onNext(t: InsertManyResult?) {
    }

    override fun onError(t: Throwable?) {
    }

    override fun onComplete() {
    }
}

const val DATE_TIME = "dateTime"
const val CREATED_AT = "createdAt"
const val ID = "_id"

class DbInitializer {
    companion object {
        private fun mapCreatedAt(fieldsMap: MutableMap<String, Any>): Map<String, Any> {
            fieldsMap[DATE_TIME] = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(fieldsMap[DATE_TIME] as String)
            return fieldsMap
        }

        private fun mapId(fieldsMap: MutableMap<String, Any>): ObjectId {
//            fieldsMap[ID] = ObjectId(fieldsMap[ID] as String)
//            return fieldsMap
            return ObjectId(fieldsMap[ID] as String)
        }

        fun initializeDb(
            mongoClient: MongoClient,
            objectMapper: ObjectMapper
        ) {
            val fileStream =
                javaClass.classLoader.getResourceAsStream("integration-data--base.json") // val jsonString = String(fileStream.readAllBytes(), StandardCharsets.UTF_8)
            val typeRef = object : TypeReference<Collection<MutableMap<String, Any>>>() {}
            println(fileStream)
            val dataEntries: Collection<MutableMap<String, Any>> =
                objectMapper.readValue<Collection<MutableMap<String, Any>>>(
                    fileStream,
                    typeRef
                )
            var documents = dataEntries.map { entry ->
//                entry[CREATED_AT] = mapCreatedAt(entry[CREATED_AT] as MutableMap<String, Any>)
                entry[ID] = mapId(entry)
                Document(entry)
            }
            val collection = mongoClient.getDatabase("a6-projectpresentations")
                .getCollection("ProjectPresentation")
            collection.insertMany(documents)
                .subscribe(TestSubscriber())
        }
    }
}
