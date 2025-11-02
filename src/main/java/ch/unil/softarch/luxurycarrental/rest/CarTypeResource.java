package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.CarType;
import ch.unil.softarch.luxurycarrental.service.CarTypeService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

@Path("/cartypes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarTypeResource {

    @Inject
    private CarTypeService carTypeService;

    @GET
    public List<CarType> getAllCarTypes() {
        return carTypeService.getAllCarTypes();
    }

    @GET
    @Path("/{id}")
    public CarType getCarType(@PathParam("id") UUID id) {
        return carTypeService.getCarType(id);
    }

    @POST
    public CarType addCarType(CarType carType) {
        return carTypeService.addCarType(carType);
    }

    @PUT
    @Path("/{id}")
    public CarType updateCarType(@PathParam("id") UUID id, CarType update) {
        return carTypeService.updateCarType(id, update);
    }

    @DELETE
    @Path("/{id}")
    public boolean removeCarType(@PathParam("id") UUID id) {
        return carTypeService.removeCarType(id);
    }
}