package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.Customer;
import ch.unil.softarch.luxurycarrental.service.CustomerService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
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
}
