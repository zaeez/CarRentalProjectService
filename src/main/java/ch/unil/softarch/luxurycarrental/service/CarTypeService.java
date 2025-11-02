package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.CarType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CarTypeService {

    @Inject
    private ApplicationState state;

    // Create
    public CarType addCarType(CarType carType) {
        if (carType.getId() == null) carType.setId(UUID.randomUUID());
        state.getCarTypes().put(carType.getId(), carType);
        return carType;
    }

    // Read
    public CarType getCarType(UUID id) {
        CarType carType = state.getCarTypes().get(id);
        if (carType == null) throw new WebApplicationException("CarType not found", 404);
        return carType;
    }

    public List<CarType> getAllCarTypes() {
        return new ArrayList<>(state.getCarTypes().values());
    }

    // Update
    public CarType updateCarType(UUID id, CarType update) {
        CarType existing = state.getCarTypes().get(id);
        if (existing == null) throw new WebApplicationException("CarType not found", 404);

        if (update.getCategory() != null) existing.setCategory(update.getCategory());
        if (update.getBrand() != null) existing.setBrand(update.getBrand());
        if (update.getModel() != null) existing.setModel(update.getModel());
        if (update.getEngine() != null) existing.setEngine(update.getEngine());
        if (update.getPower() > 0) existing.setPower(update.getPower());
        if (update.getMaxSpeed() > 0) existing.setMaxSpeed(update.getMaxSpeed());
        if (update.getAcceleration() > 0) existing.setAcceleration(update.getAcceleration());
        if (update.getWeight() > 0) existing.setWeight(update.getWeight());
        if (update.getDriveType() != null) existing.setDriveType(update.getDriveType());
        if (update.getTransmission() != null) existing.setTransmission(update.getTransmission());
        if (update.getSeats() > 0) existing.setSeats(update.getSeats());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getFeatures() != null) existing.setFeatures(update.getFeatures());

        return existing;
    }

    // Delete
    public boolean removeCarType(UUID id) {
        return state.getCarTypes().remove(id) != null;
    }
}
