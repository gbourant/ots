package gr.ots.config;

import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.List;

@Provider
public class NullPointerExceptionMapper implements ExceptionMapper<NullPointerException> {

    @Override
    public Response toResponse(NullPointerException exception) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(buildSimpleViolationReport(exception))
                .header("validation-exception", "true")
                .build();
    }

    private String extractMethodName(String message) {
        if (message != null && message.contains("Cannot invoke \"")) {
            int start = message.indexOf("\"") + 1;
            int end = message.indexOf("\"", start);
            if (start > 0 && end > start) {
                return message.substring(start, end);
            }
        }
        return "Unknown method";
    }

    private ViolationReport buildSimpleViolationReport(NullPointerException exception) {
        String methodName = extractMethodName(exception.getMessage());
        return new ViolationReport("Constraint Violation",
                Response.Status.BAD_REQUEST,
                List.of(new ViolationReport.Violation(methodName, "Cannot invoke the field because the return value is null")));
    }


}