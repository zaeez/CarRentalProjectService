package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.Admin;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class MaintenanceService {

    @Inject
    private ApplicationState state;

    /**
     * Schedule system maintenance.
     * Sends email notification to all admins and customers asynchronously.
     *
     * @param adminId   ID of admin performing the action
     * @param startTime maintenance start
     * @param endTime   maintenance end
     * @return confirmation message with start and end time
     */
    public Map<String, Object> scheduleMaintenance(String adminId, LocalDateTime startTime, LocalDateTime endTime) {
        try {
            // Validate admin
            UUID adminUUID = UUID.fromString(adminId);
            Admin admin = state.getAdmins().get(adminUUID);
            if (admin == null) throw new WebApplicationException("Admin not found", 404);

            // Validate time
            if (startTime.isAfter(endTime)) throw new WebApplicationException("Start time must be before end time", 400);

            // Send emails asynchronously
            notifyAllUsers(startTime, endTime);

            return Map.of(
                    "message", "Maintenance scheduled successfully",
                    "startTime", startTime.toString(),
                    "endTime", endTime.toString()
            );

        } catch (IllegalArgumentException e) {
            throw new WebApplicationException("Invalid UUID format for adminId", 400);
        } catch (WebApplicationException e) {
            throw e; // propagate 404/400
        } catch (Exception e) {
            e.printStackTrace(); // log for server
            throw new WebApplicationException("Internal Server Error: " + e.getMessage(), 500);
        }
    }

    /**
     * Notify all admins and customers about maintenance.
     * Emails are sent asynchronously using EmailSender.
     */
    private void notifyAllUsers(LocalDateTime startTime, LocalDateTime endTime) {
        String subject = "System Maintenance Notification";
        String body = String.format(
                "Dear user,\n\nThe system will undergo maintenance from %s to %s.\nPlease plan accordingly.\n\nLuxury Car Rental Team",
                startTime, endTime
        );

        // Notify all admins
        state.getAdmins().values().forEach(admin ->
                EmailSender.sendEmailAsync(admin.getEmail(), subject, body)
        );

        // Notify all customers
        state.getCustomers().values().forEach(customer ->
                EmailSender.sendEmailAsync(customer.getEmail(), subject, body)
        );
    }
}