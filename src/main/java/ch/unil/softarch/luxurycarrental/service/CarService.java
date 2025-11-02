package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.Car;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CarService {

    @Inject
    private ApplicationState state;

    // Read
    public Car getCar(UUID id) {
        Car car = state.getCars().get(id);
        if (car == null) throw new WebApplicationException("Car not found", 404); // ✅ 一致的异常处理
        return car;
    }

    public List<Car> getAllCars() {
        return new ArrayList<>(state.getCars().values());
    }

    // Create
    public Car addCar(Car car) {
        if (car.getCarType() == null || car.getCarType().getId() == null) {
            throw new WebApplicationException("CarType must be provided", 400);
        }
        // Check that the CarType exists
        if (!state.getCarTypes().containsKey(car.getCarType().getId())) {
            throw new WebApplicationException("CarType does not exist", 400);
        }

        if (car.getId() == null) car.setId(UUID.randomUUID());
        state.getCars().put(car.getId(), car);
        return car;
    }

    // Update
    public Car updateCar(UUID id, Car update) {
        Car existing = state.getCars().get(id);
        if (existing == null) throw new WebApplicationException("Car not found", 404);

        // If updating CarType, check it exists
        if (update.getCarType() != null) {
            if (update.getCarType().getId() == null ||
                    !state.getCarTypes().containsKey(update.getCarType().getId())) {
                throw new WebApplicationException("CarType does not exist", 400);
            }
            existing.setCarType(update.getCarType());
        }

        if (update.getLicensePlate() != null) existing.setLicensePlate(update.getLicensePlate());
        if (update.getDailyRentalPrice() > 0) existing.setDailyRentalPrice(update.getDailyRentalPrice());
        if (update.getDepositAmount() > 0) existing.setDepositAmount(update.getDepositAmount());
        if (update.getStatus() != null) existing.setStatus(update.getStatus());
        if (update.getImageUrl() != null) existing.setImageUrl(update.getImageUrl());
        if (update.getRegistrationDate() != null) existing.setRegistrationDate(update.getRegistrationDate());
        if (update.getLastMaintenanceDate() != null) existing.setLastMaintenanceDate(update.getLastMaintenanceDate());
        if (update.getVin() != null) existing.setVin(update.getVin());
        if (update.getColor() != null) existing.setColor(update.getColor());
        if (update.getInsuranceExpiryDate() != null) existing.setInsuranceExpiryDate(update.getInsuranceExpiryDate());

        return existing;
    }

    // Delete
    public boolean removeCar(UUID id) {
        return state.getCars().remove(id) != null;
    }
}