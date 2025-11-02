package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.Booking;
import ch.unil.softarch.luxurycarrental.service.BookingService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingResource {

    @Inject
    private BookingService bookingService;

    // ---------------- CRUD ----------------
    @GET
    public Collection<Booking> getAllBookings() {
        return bookingService.getAllBookings();
    }

    @GET
    @Path("/{id}")
    public Booking getBooking(@PathParam("id") UUID id) {
        return bookingService.getBooking(id);
    }

    @GET
    @Path("/customer/{customerId}")
    public List<Booking> getBookingsByCustomer(@PathParam("customerId") UUID customerId) {
        return bookingService.getBookingsByCustomerId(customerId);
    }

    @GET
    @Path("/car/{carId}")
    public List<Booking> getBookingsByCar(@PathParam("carId") UUID carId) {
        return bookingService.getBookingsByCarId(carId);
    }

    @POST
    public Booking createBooking(Booking booking) {
        return bookingService.createBooking(booking);
    }

    @PUT
    @Path("/{id}")
    public Booking updateBooking(@PathParam("id") UUID id, Booking update) {
        return bookingService.updateBooking(id, update);
    }

    @DELETE
    @Path("/{id}")
    public Response removeBooking(@PathParam("id") UUID id) {
        boolean removed = bookingService.removeBooking(id);
        if (removed) return Response.ok("Booking removed successfully").build();
        return Response.status(Response.Status.NOT_FOUND).entity("Booking not found").build();
    }

    // ---------------- Operations ----------------
    @PUT
    @Path("/complete/{id}")
    public Response completeBooking(@PathParam("id") UUID id) {
        bookingService.completeBooking(id);
        return Response.ok("Booking completed successfully").build();
    }

    @PUT
    @Path("/cancel/{id}")
    public Response cancelBooking(@PathParam("id") UUID id) {
        bookingService.cancelBooking(id);
        return Response.ok("Booking canceled and deposit refunded").build();
    }
}