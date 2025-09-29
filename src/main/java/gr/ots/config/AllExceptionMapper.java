package gr.ots.config;

import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;

import java.util.List;

@Provider
// TODO: this needs to be done better
public class AllExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        if (exception.getCause() != null && exception.getCause().getCause() != null && exception.getCause().getCause().getCause() != null) {
            Throwable cause = exception.getCause().getCause().getCause();

            if (cause instanceof JdbcSQLIntegrityConstraintViolationException) {
                ViolationReport constraintViolation = new ViolationReport("Constraint Violation",
                        Response.Status.BAD_REQUEST,
                        List.of(new ViolationReport.Violation(null, "Duplication found")));
                return Response.status(Response.Status.BAD_REQUEST)
                        .type(MediaType.APPLICATION_JSON)
                        .entity(constraintViolation)
                        .header("validation-exception", "true")
                        .build();
            }
        }
        if (exception.getCause() != null && exception.getCause().getCause() instanceof jakarta.validation.ConstraintViolationException) {
            return new ConstraintViolationExceptionMapper().toResponse((ConstraintViolationException) exception.getCause().getCause());
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildSimpleViolationReport(exception))
                .header("validation-exception", "true")
                .build();
    }

    private ViolationReport buildSimpleViolationReport(Exception exception) {
        return new ViolationReport("Constraint Violation",
                Response.Status.BAD_REQUEST,
                List.of(new ViolationReport.Violation(null, exception.getMessage())));
    }

}