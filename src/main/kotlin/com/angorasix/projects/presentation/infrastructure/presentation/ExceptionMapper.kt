package com.angorasix.projects.presentation.infrastructure.presentation

import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationExceptionMapper.ViolationReport
import io.quarkus.hibernate.validator.runtime.jaxrs.ResteasyReactiveViolationExceptionMapper.ViolationReport.Violation
import org.jboss.resteasy.reactive.RestResponse
import org.jboss.resteasy.reactive.server.ServerExceptionMapper
import javax.ws.rs.core.Response

/**
 * <p>
 * </p>
 *
 * @author rozagerardo
 */
class ExceptionMapper {

    @ServerExceptionMapper
    fun mapException(ex: ValueInstantiationException):
            RestResponse<ErrorResponse>? {
        val violation = Violation(ex.cause?.message, "must not be null")
        val violationReport = ViolationReport(
                "Constraint Violation",
                Response.Status.BAD_REQUEST,
                listOf(violation)
        )
        val errorResponse = ErrorResponse("Error deserializing JSON body - invalid field", "invalid_field", violationReport)
        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse)
    }

    @ServerExceptionMapper
    fun mapException(ex: IllegalArgumentException):
            RestResponse<ErrorResponse>? {
        val errorResponse = ErrorResponse(ex.message ?: "Illegal request argument presented", "illegal_argument", null)
        return RestResponse.status(Response.Status.BAD_REQUEST, errorResponse)
    }

    data class ErrorResponse(
            val message: String,
            val code: String,
            val violationReport: ViolationReport?,
    )
}
