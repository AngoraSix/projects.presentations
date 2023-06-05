package com.angorasix.projects.presentation.utils

import com.angorasix.commons.domain.SimpleContributor
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationMedia
import com.angorasix.projects.presentation.domain.projectpresentation.PresentationSection
import com.angorasix.projects.presentation.domain.projectpresentation.ProjectPresentation
import com.angorasix.projects.presentation.presentation.dto.PresentationMediaDto
import com.angorasix.projects.presentation.presentation.dto.PresentationSectionDto
import com.angorasix.projects.presentation.presentation.dto.ProjectPresentationDto
import java.util.*

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
fun mockPresentation(modifier: String = ""): ProjectPresentation =
    ProjectPresentation(
        "mockedProjectId$modifier",
        setOf(SimpleContributor("mockedContributorId", emptySet())),
        "mockedReferenceName$modifier",
        listOf<PresentationSection>(mockSection("1"), mockSection("2")),
    )

fun mockSection(modifier: String = ""): PresentationSection = PresentationSection(
    "mockedSectionTitle$modifier",
    "mockedDescription$modifier",
    listOf(mockMedia("${modifier}1"), mockMedia("${modifier}2")),
    mockMedia("${modifier}Main"),
)

fun mockMedia(modifier: String = ""): PresentationMedia = PresentationMedia(
    "mockedMediaType$modifier",
    "http://localhost/mocked-url$modifier",
    "http://localhost/mocked-thumbnail-url$modifier",
    "mockedResourceId$modifier",
)

fun mockPresentationDto(modifier: String = ""): ProjectPresentationDto =
    ProjectPresentationDto(
        "mockedProjectId$modifier",
        setOf(SimpleContributor("1", emptySet())),
        "mockedReferenceName$modifier",
        listOf<PresentationSectionDto>(mockSectionDto("1"), mockSectionDto("2")),
        "mockedPresentationId$modifier",
    )

fun mockSectionDto(modifier: String = ""): PresentationSectionDto =
    PresentationSectionDto(
        "mockedSectionTitle$modifier",
        "mockedDescription$modifier",
        listOf(mockMediaDto("${modifier}1"), mockMediaDto("${modifier}2")),
        mockMediaDto("${modifier}Main"),
    )

fun mockMediaDto(modifier: String = ""): PresentationMediaDto = PresentationMediaDto(
    "mockedMediaType$modifier",
    "http://localhost/mocked-url$modifier",
    "http://localhost/mocked-thumbnail-url$modifier",
    "mockedResourceId$modifier",
)

fun mockRequestingContributorHeader(asAdmin: Boolean = false): String {
    val requestingContributorJson = """
        {
          "contributorId": "mockedContributorId1",
          "projectAdmin": $asAdmin
        }
    """.trimIndent()
    return Base64.getUrlEncoder().encodeToString(requestingContributorJson.toByteArray())
}
