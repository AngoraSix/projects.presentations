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
        RestResponse<ViolationReport>? {
            val violation = Violation(ex.cause?.message, "must not be null")
            val violationReport = ViolationReport(
                "Constraint Violation",
                Response.Status.BAD_REQUEST,
                listOf(violation)
            )
            return RestResponse.status(Response.Status.BAD_REQUEST, violationReport)
        }
}
