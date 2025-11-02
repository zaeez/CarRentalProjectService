package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.service.MaintenanceService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Map;

@Path("/maintenance")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MaintenanceResource {

    @Inject
    private MaintenanceService service;

    /**
     * Schedule maintenance.
     * Example POST body:
     * {
     *     "adminId": "uuid-of-admin",
     *     "startTime": "2025-11-03T22:00:00",
     *     "endTime": "2025-11-04T06:00:00"
     * }
     */
    @POST
    public Map<String, Object> scheduleMaintenance(Map<String, String> request) {
        String adminId = request.get("adminId");
        String startStr = request.get("startTime");
        String endStr = request.get("endTime");

        if (adminId == null || startStr == null || endStr == null) {
            throw new WebApplicationException("adminId, startTime and endTime are required", 400);
        }

        try {
            LocalDateTime startTime = LocalDateTime.parse(startStr);
            LocalDateTime endTime = LocalDateTime.parse(endStr);
            return service.scheduleMaintenance(adminId, startTime, endTime);
        } catch (DateTimeParseException e) {
            throw new WebApplicationException("Invalid datetime format. Use yyyy-MM-dd'T'HH:mm:ss", 400);
        }
    }
}