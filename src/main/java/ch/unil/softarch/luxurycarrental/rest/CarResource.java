package ch.unil.softarch.luxurycarrental.rest;

import ch.unil.softarch.luxurycarrental.domain.entities.Car;
import ch.unil.softarch.luxurycarrental.service.CarService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;
import java.util.UUID;

/**
 * REST resource class for managing Car entities.
 * Handles CRUD operations through HTTP requests.
 */
@Path("/cars")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CarResource {

    @Inject
    private CarService carService;

    /**
     * Retrieve all cars.
     * @return a list of all available cars
     */
    @GET
    public List<Car> getAllCars() {
        return carService.getAllCars();
    }

    /**
     * Retrieve a specific car by its ID.
     * @param id the unique identifier of the car
     * @return the car with the given ID
     */
    @GET
    @Path("/{id}")
    public Car getCar(@PathParam("id") UUID id) {
        return carService.getCar(id);
    }

    /**
     * Create a new car entry.
     * @param car the car object to be added
     * @return the created car with its generated ID
     */
    @POST
    public Car addCar(Car car) {
        return carService.addCar(car);
    }

    /**
     * Update an existing car's information.
     * Only non-null or positive fields will be updated.
     * @param id the ID of the car to update
     * @param update the new car data
     * @return the updated car object
     */
    @PUT
    @Path("/{id}")
    public Car updateCar(@PathParam("id") UUID id, Car update) {
        return carService.updateCar(id, update);
    }

    /**
     * Delete a car by its ID.
     * @param id the unique identifier of the car
     * @return true if deletion was successful, false otherwise
     */
    @DELETE
    @Path("/{id}")
    public boolean removeCar(@PathParam("id") UUID id) {
        return carService.removeCar(id);
    }
}