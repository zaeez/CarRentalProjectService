package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.Customer;
import ch.unil.softarch.luxurycarrental.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Path("/customers")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CustomerResource {

    @Inject
    private CustomerService customerService;

    // Get all customers
    @GET
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    // Get one customer by ID
    @GET
    @Path("/{id}")
    public Customer getCustomer(@PathParam("id") UUID id) {
        return customerService.getCustomer(id);
    }

    // Add new customer
    @POST
    public Customer addCustomer(Customer customer) {
        return customerService.addCustomer(customer);
    }

    // Update existing customer
    @PUT
    @Path("/{id}")
    public Customer updateCustomer(@PathParam("id") UUID id, Customer update) {
        return customerService.updateCustomer(id, update);
    }

    // Delete customer
    @DELETE
    @Path("/{id}")
    public boolean removeCustomer(@PathParam("id") UUID id) {
        return customerService.removeCustomer(id);
    }

    // Customer login
    @POST
    @Path("/login")
    public Response login(Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        if (email == null || password == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Email and password are required"))
                    .build();
        }

        try {
            Customer customer = customerService.authenticate(email, password);
            // Return some information to avoid password leakage
            return Response.ok(Map.of(
                    "id", customer.getId(),
                    "firstName", customer.getFirstName(),
                    "lastName", customer.getLastName(),
                    "email", customer.getEmail(),
                    "balance", customer.getBalance()
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
        customerService.changePassword(id, request.oldPassword, request.newPassword);
        return Response.ok(Map.of("message", "Password changed successfully")).build();
    }
}
