package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.exceptions.BookingConflictException;
import ch.unil.softarch.luxurycarrental.domain.exceptions.CarUnavailableException;
import ch.unil.softarch.luxurycarrental.domain.exceptions.PaymentFailedException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.logging.Logger;

/**
 * Global exception handler for REST API.
 * Maps domain and service layer exceptions into proper HTTP responses.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger log = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Exception exception) {
        log.severe("Exception caught: " + exception.getMessage());

        // Default HTTP status code
        Response.Status status = Response.Status.INTERNAL_SERVER_ERROR;
        String message = exception.getMessage();

        // Map domain exceptions to specific HTTP status codes
        if (exception instanceof CarUnavailableException) {
            status = Response.Status.CONFLICT; // 409
            message = "The selected car is currently unavailable.";
        } else if (exception instanceof BookingConflictException) {
            status = Response.Status.BAD_REQUEST; // 400
            message = "A booking conflict occurred. Please select another time.";
        } else if (exception instanceof PaymentFailedException) {
            status = Response.Status.PAYMENT_REQUIRED; // 402
            message = "Payment failed. Please check your payment method.";
        }

        // Return a JSON response
        return Response.status(status)
                .entity(new ErrorResponse(exception.getClass().getSimpleName(), message))
                .build();
    }

    /**
     * Simple error response model.
     * Sent back to the client as a JSON object.
     */
    public static class ErrorResponse {
        private String errorType;
        private String message;

        public ErrorResponse(String errorType, String message) {
            this.errorType = errorType;
            this.message = message;
        }

        public String getErrorType() {
            return errorType;
        }

        public String getMessage() {
            return message;
        }
    }
}