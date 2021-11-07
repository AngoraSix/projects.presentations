package com.angorasix.projects.presentation.infrastructure

import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.mongodb.MongoClientSettings
import org.bson.*
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId


class ProjectPresentationCodec : CollectibleCodec<ProjectPresentation> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry().get(Document::class.java)

    override fun encode(writer: BsonWriter?, projectPresentation: ProjectPresentation, encoderContext: EncoderContext?) {
        val doc = Document()
        doc["objective"] = projectPresentation.objective
        doc["projectId"] = projectPresentation.projectId
        doc["media"] = projectPresentation.media.map { it.convertToDocument() }

        documentCodec.encode(writer, doc, encoderContext)
    }

    override fun generateIdIfAbsentFromDocument(document: ProjectPresentation): ProjectPresentation {
        if (!documentHasId(document)) {
            document.id = ObjectId()
        }
        return document
    }

    override fun documentHasId(document: ProjectPresentation): Boolean {
        return document.id != null
    }

    override fun getDocumentId(document: ProjectPresentation): BsonValue {
        return BsonString(document.id.toString())
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): ProjectPresentation {
        val document: Document = documentCodec.decode(reader, decoderContext)
        val presentationMedia = document.getList("media", Document::class.java).map { PresentationMedia(it.getString("type"), it.getString("url")) }
        val projectPresentation = ProjectPresentation(document.getString("projectId"), document.getString("objective"), presentationMedia)
        projectPresentation.id = document.getObjectId("_id")
        return projectPresentation
    }

    override fun getEncoderClass(): Class<ProjectPresentation> {
        return ProjectPresentation::class.java
    }
}

class PresentationMediaCodec : Codec<PresentationMedia> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry().get(Document::class.java)

    override fun encode(writer: BsonWriter?, presentationMedia: PresentationMedia, encoderContext: EncoderContext?) {
        val doc = Document()
        doc["type"] = presentationMedia.type
        doc["url"] = presentationMedia.url

        documentCodec.encode(writer, doc, encoderContext)
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): PresentationMedia {
        val document: Document = documentCodec.decode(reader, decoderContext)
        return PresentationMedia(document.getString("type"), document.getString("url"))
    }

    override fun getEncoderClass(): Class<PresentationMedia> {
        return PresentationMedia::class.java
    }
}

class ProjectPresentationCodecProvider : CodecProvider {

    @Suppress("UNCHECKED_CAST")
    override operator fun <T> get(clazz: Class<T>, registry: CodecRegistry?): Codec<T>? {
        return when (clazz) {
            ProjectPresentation::class.java -> {
                ProjectPresentationCodec() as Codec<T>?
            }
            PresentationMedia::class.java -> {
                PresentationMediaCodec() as Codec<T>?
            }
            else -> null
        }
    }
}

private fun PresentationMedia.convertToDocument(): Document {
    val doc = Document()
    doc["type"] = type
    doc["url"] = url
    return doc
}