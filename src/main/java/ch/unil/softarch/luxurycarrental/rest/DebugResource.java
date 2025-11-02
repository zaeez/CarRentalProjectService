package ch.unil.softarch.luxurycarrental.rest;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

//TODO: Eventually, this class should be removed, as it was just for testing purposes.
@Path("/debug")
public class DebugResource {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path("/evenValue/{intVal}")
    public Response testValue(@PathParam("intVal") Integer value) {
        if (value % 2 == 0) {
            return Response.ok("Value is a correct even number").build();
        } else {
            return Response.notAcceptable(Collections.emptyList()).build();
        }
    }

    @POST
    @Path("/timeslots")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response receiveTimeslots(DebugRequest request) {
        List<LocalDateTime> timestamps = request.getTimeslots();
        System.out.println("Received timestamps: " + timestamps);  // Debugging output
        return Response.ok("Received " + timestamps.size() + " timestamps.").build();
    }
}