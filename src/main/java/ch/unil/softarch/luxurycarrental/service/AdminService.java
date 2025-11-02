package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.Admin;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class AdminService {

    @Inject
    private ApplicationState state;

    // Create
    // AdminService.java
    public Admin addAdmin(Admin admin) {
        // Check for duplicate username
        boolean usernameExists = state.getAdmins().values().stream()
                .anyMatch(a -> a.getUsername().equals(admin.getUsername()));
        if (usernameExists) {
            throw new WebApplicationException("Username already exists", 400);
        }

        // Check for duplicate email
        boolean emailExists = state.getAdmins().values().stream()
                .anyMatch(a -> a.getEmail().equals(admin.getEmail()));
        if (emailExists) {
            throw new WebApplicationException("Email already exists", 400);
        }

        // Set ID and timestamps if not provided
        if (admin.getId() == null) admin.setId(UUID.randomUUID());
        if (admin.getCreatedAt() == null) admin.setCreatedAt(LocalDateTime.now());
        if (admin.getUpdatedAt() == null) admin.setUpdatedAt(LocalDateTime.now());

        // Save admin
        state.getAdmins().put(admin.getId(), admin);

        // --- Send welcome email ---
        String to = admin.getEmail();
        String subject = "Welcome to Luxury Car Rental Admin Panel";
        String body = "Hi " + admin.getName() + ",\n\n" +
                "Your administrator account has been successfully created.\n" +
                "Username: " + admin.getUsername() + "\n" +
                "Please keep your password secure.\n\n" +
                "Best regards,\nLuxury Car Rental Team";

        // Call the mail sending tool
        EmailSender.sendEmail(to, subject, body);

        return admin;
    }

    // Read
    public Admin getAdmin(UUID id) {
        Admin admin = state.getAdmins().get(id);
        if (admin == null) throw new WebApplicationException("Admin not found", 404);
        return admin;
    }

    public List<Admin> getAllAdmins() {
        return new ArrayList<>(state.getAdmins().values());
    }

    // Update
    public Admin updateAdmin(UUID id, Admin update) {
        Admin existing = state.getAdmins().get(id);
        if (existing == null) throw new WebApplicationException("Admin not found", 404);

        if (update.getName() != null) existing.setName(update.getName());
        if (update.getUsername() != null) existing.setUsername(update.getUsername());
        if (update.getEmail() != null) existing.setEmail(update.getEmail());
        if (update.getPassword() != null && !update.getPassword().isBlank()) {
            existing.setPassword(update.getPassword()); // 可加加密逻辑
        }

        existing.setUpdatedAt(LocalDateTime.now());
        return existing;
    }

    // Delete
    public boolean removeAdmin(UUID id) {
        return state.getAdmins().remove(id) != null;
    }

    // Login verification
    public Admin authenticate(String username, String password) {
        return state.getAdmins().values().stream()
                .filter(admin -> admin.getUsername().equals(username)
                        && admin.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("Invalid username or password", 401));
    }

    // Get the administrator according to the ID
    public Admin getAdminById(UUID id) {
        Admin admin = state.getAdmins().get(id);
        if (admin == null) throw new WebApplicationException("Admin not found", 404);
        return admin;
    }

    /**
     * Change admin password by ID
     */
    public boolean changePassword(UUID adminId, String oldPassword, String newPassword) {
        Admin admin = state.getAdmins().get(adminId);
        if (admin == null) return false; // admin not found
        if (!admin.getPassword().equals(oldPassword)) return false; // wrong old password

        admin.setPassword(newPassword);
        return true;
    }


}