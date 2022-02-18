package com.angorasix.projects.presentation.infrastructure

import com.angorasix.contributors.domain.contributor.ProjectPresentation
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.mongodb.MongoClientSettings
import org.bson.BsonReader
import org.bson.BsonString
import org.bson.BsonValue
import org.bson.BsonWriter
import org.bson.Document
import org.bson.codecs.Codec
import org.bson.codecs.CollectibleCodec
import org.bson.codecs.DecoderContext
import org.bson.codecs.EncoderContext
import org.bson.codecs.configuration.CodecProvider
import org.bson.codecs.configuration.CodecRegistry
import org.bson.types.ObjectId

class ProjectPresentationCodec : CollectibleCodec<ProjectPresentation> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry()
            .get(Document::class.java)

    override fun encode(
            writer: BsonWriter?,
            projectPresentation: ProjectPresentation,
            encoderContext: EncoderContext?
    ) {
        val doc = Document()
        doc["_id"] = projectPresentation.id
        doc["objective"] = projectPresentation.objective
        doc["title"] = projectPresentation.title
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
        val presentationMedia = document.getList("media", Document::class.java)
                .map {
                    PresentationMedia(it.getString("mediaType"),
                            it.getString("url"),
                            it.getString("thumbnailUrl"),
                            it.getString("resourceId"))
                }
        val projectPresentation = ProjectPresentation(
                document.getString("projectId"),
                document.getString("title"),
                document.getString("objective"),
                presentationMedia
        )
        projectPresentation.id = document.getObjectId("_id")
        return projectPresentation
    }

    override fun getEncoderClass(): Class<ProjectPresentation> {
        return ProjectPresentation::class.java
    }
}

class PresentationMediaCodec : Codec<PresentationMedia> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry()
            .get(Document::class.java)

    override fun encode(
            writer: BsonWriter?,
            presentationMedia: PresentationMedia,
            encoderContext: EncoderContext?
    ) {
        val doc = Document()
        doc["mediaType"] = presentationMedia.mediaType
        doc["url"] = presentationMedia.url
        doc["thumbnailUrl"] = presentationMedia.thumbnailUrl
        doc["resourceId"] = presentationMedia.resourceId

        documentCodec.encode(writer, doc, encoderContext)
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): PresentationMedia {
        val document: Document = documentCodec.decode(reader, decoderContext)
        return PresentationMedia(
                document.getString("mediaType"),
                document.getString("url"),
                document.getString("thumbnailUrl"),
                document.getString("resourceId")
        )
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
    doc["mediaType"] = mediaType
    doc["url"] = url
    doc["thumbnailUrl"] = thumbnailUrl
    doc["resourceId"] = resourceId
    return doc
}
