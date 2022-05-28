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

    override fun start(): MutableMap<String, String> {
        db.start()
        return mutableMapOf(
            "quarkus.mongodb.connection-string"
                to "mongodb://${db.getContainerIpAddress()}:${db.getFirstMappedPort()}" +
                    "/a6-projectpresentations"
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
        // nothing to do here...
    }

    override fun onError(t: Throwable?) {
        // nothing to do here...
    }

    override fun onComplete() {
        // nothing to do here...
    }
}

const val DATE_TIME = "dateTime"
const val CREATED_AT = "createdAt"
const val ID = "_id"

object DbInitializer {
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
        val fileStream = DbInitializer::class.java.classLoader
            .getResourceAsStream("integration-data--base.json")
        val typeRef = object : TypeReference<Collection<MutableMap<String, Any>>>() {}
        val dataEntries: Collection<MutableMap<String, Any>> =
            objectMapper.readValue<Collection<MutableMap<String, Any>>>(
                fileStream,
                typeRef
            )
        var documents = dataEntries.map { entry ->
            entry[ID] = mapId(entry)
            Document(entry)
        }
        val collection = mongoClient.getDatabase("a6-projectpresentations")
            .getCollection("ProjectPresentation")
        collection.insertMany(documents)
            .subscribe(TestSubscriber())
    }
}
