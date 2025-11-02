package ch.unil.softarch.luxurycarrental.rest;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.core.UriInfo;

import java.time.LocalDateTime;
import java.util.logging.Logger;

/**
 * Global exception handler for the REST API.
 * Maps exceptions from the domain or service layer into appropriate HTTP responses with extra details.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    @Context
    private UriInfo uriInfo;

    @Override
    public Response toResponse(Exception exception) {
        int statusCode = 500;
        String message = exception.getMessage();

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorType(exception.getClass().getSimpleName());
        errorResponse.setMessage(message);
        errorResponse.setTimestamp(LocalDateTime.now());
        errorResponse.setPath(uriInfo != null ? uriInfo.getPath() : "");

        return Response.status(statusCode)
                .entity(errorResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    public static class ErrorResponse {
        private String errorType;
        private String message;
        private LocalDateTime timestamp;
        private String path;

        public ErrorResponse() {}

        public ErrorResponse(String errorType, String message, LocalDateTime timestamp, String path) {
            this.errorType = errorType;
            this.message = message;
            this.timestamp = timestamp;
            this.path = path;
        }

        public String getErrorType() { return errorType; }
        public void setErrorType(String errorType) { this.errorType = errorType; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public LocalDateTime getTimestamp() { return timestamp; }
        public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
    }
}