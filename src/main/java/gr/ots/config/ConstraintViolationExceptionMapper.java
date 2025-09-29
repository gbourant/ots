package gr.ots.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.ArrayList;
import java.util.List;

@Provider
public class ConstraintViolationExceptionMapper implements ExceptionMapper<ConstraintViolationException> {

    @Override
    public Response toResponse(ConstraintViolationException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildSimpleViolationReport(exception))
                .header("validation-exception", "true")
                .build();
    }

    private ViolationReport buildSimpleViolationReport(ConstraintViolationException cve) {
        List<ViolationReport.Violation> violations = new ArrayList<>();
        for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
            violations.add(new ViolationReport.Violation(cv.getPropertyPath().toString(), cv.getMessage()));
        }
        return new ViolationReport("Constraint Violation", Response.Status.BAD_REQUEST, violations);
    }

    public static class ViolationReport {
        public String error;
        public int status;
        public List<Violation> violations;

        public ViolationReport(String error, Response.Status status, List<Violation> violations) {
            this.error = error;
            this.status = status.getStatusCode();
            this.violations = violations;
        }

        public static class Violation {
            public String path;
            public String message;

            public Violation(String path, String message) {
                this.path = path;
                this.message = message;
            }
        }
    }

}