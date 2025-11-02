package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.Admin;
import ch.unil.softarch.luxurycarrental.service.AdminService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/admins")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AdminResource {

    @Inject
    private AdminService adminService;

    // Get all admins
    @GET
    public List<Admin> getAllAdmins() {
        return adminService.getAllAdmins();
    }

    // Get one admin by ID
    @GET
    @Path("/{id}")
    public Admin getAdmin(@PathParam("id") UUID id) {
        return adminService.getAdmin(id);
    }

    // Add new admin
    @POST
    public Admin addAdmin(Admin admin) {
        return adminService.addAdmin(admin);
    }

    // Update existing admin
    @PUT
    @Path("/{id}")
    public Admin updateAdmin(@PathParam("id") UUID id, Admin update) {
        return adminService.updateAdmin(id, update);
    }

    // Delete admin
    @DELETE
    @Path("/{id}")
    public boolean removeAdmin(@PathParam("id") UUID id) {
        return adminService.removeAdmin(id);
    }

    // Login
    @POST
    @Path("/login")
    public Response login(Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Username and password are required"))
                    .build();
        }

        try {
            Admin admin = adminService.authenticate(username, password);
            // Some information can be returned to avoid leaking the password.
            return Response.ok(Map.of(
                    "id", admin.getId(),
                    "username", admin.getUsername(),
                    "name", admin.getName(),
                    "email", admin.getEmail()
            )).build();
        } catch (WebApplicationException e) {
            return Response.status(e.getResponse().getStatus())
                    .entity(Map.of("message", e.getMessage()))
                    .build();
        }
    }

    // Change password
    public static class ChangePasswordRequest {
        public String oldPassword;
        public String newPassword;
    }
    @PUT
    @Path("/{id}/change-password")
    public Response changePassword(@PathParam("id") UUID id, ChangePasswordRequest request) {
        adminService.changePassword(id, request.oldPassword, request.newPassword);
        return Response.ok(Map.of("message", "Password changed successfully")).build();
    }

}