package com.angorasix.projects.presentation.infrastructure

import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
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

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry().get(Document::class.java)

    override fun encode(writer: BsonWriter?, projectPresentation: ProjectPresentation, encoderContext: EncoderContext?) {
        documentCodec.encode(writer, projectPresentation.convertToDocument(), encoderContext)
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
        return document.convertToProjectPresentation()
    }

    override fun getEncoderClass(): Class<ProjectPresentation> {
        return ProjectPresentation::class.java
    }
}

class PresentationSectionCodec : Codec<PresentationSection> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry().get(Document::class.java)

    override fun encode(writer: BsonWriter?, presentationSection: PresentationSection, encoderContext: EncoderContext?) {
        documentCodec.encode(writer, presentationSection.convertToDocument(), encoderContext)
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): PresentationSection {
        val document: Document = documentCodec.decode(reader, decoderContext)
        return document.convertToSection()
    }

    override fun getEncoderClass(): Class<PresentationSection> {
        return PresentationSection::class.java
    }
}

class PresentationMediaCodec : Codec<PresentationMedia> {

    private val documentCodec: Codec<Document> = MongoClientSettings.getDefaultCodecRegistry().get(Document::class.java)

    override fun encode(writer: BsonWriter?, presentationMedia: PresentationMedia, encoderContext: EncoderContext?) {
        documentCodec.encode(writer, presentationMedia.convertToDocument(), encoderContext)
    }

    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): PresentationMedia {
        val document: Document = documentCodec.decode(reader, decoderContext)
        return document.convertToMedia()
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

// Convert to Document helper methods

private fun ProjectPresentation.convertToDocument(): Document {
    val doc = Document()
    doc["_id"] = id
    doc["projectId"] = projectId
    doc["sections"] = sections.map { it.convertToDocument() }
    return doc
}

private fun PresentationSection.convertToDocument(): Document {
    val doc = Document()
    doc["title"] = title
    doc["description"] = description
    doc["media"] = media.map { it.convertToDocument() }
    doc["mainMedia"] = mainMedia?.convertToDocument()
    return doc
}

private fun PresentationMedia.convertToDocument(): Document {
    val doc = Document()
    doc["mediaType"] = mediaType
    doc["url"] = url
    doc["thumbnailUrl"] = thumbnailUrl
    doc["resourceId"] = resourceId
    return doc
}

// Document Convert to [...] helper methods
private fun Document.convertToProjectPresentation(): ProjectPresentation {
    val sections = getList("sections", Document::class.java).map {
        it.convertToSection()
    }
    val projectPresentation = ProjectPresentation(getString("projectId"), sections)
    projectPresentation.id = getObjectId("_id")
    return projectPresentation
}

private fun Document.convertToSection(): PresentationSection {
    val presentationMedia = getList("media", Document::class.java).map {
        it.convertToMedia()
    }
    return PresentationSection(getString("title"), getString("description"), presentationMedia, get("mainMedia", Document::class.java).convertToMedia())
}

private fun Document.convertToMedia(): PresentationMedia {
    return PresentationMedia(getString("mediaType"), getString("url"), getString("thumbnailUrl"), getString("resourceId"))
}