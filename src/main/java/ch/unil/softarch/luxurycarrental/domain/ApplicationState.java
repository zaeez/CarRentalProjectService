package ch.unil.softarch.luxurycarrental.domain;

import ch.unil.softarch.luxurycarrental.domain.entities.*;
import ch.unil.softarch.luxurycarrental.domain.enums.*;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@ApplicationScoped
public class ApplicationState {

    // ---------- In-memory storage ----------
    private Map<UUID, Customer> customers;
    private Map<UUID, Admin> admins;
    private Map<UUID, Car> cars;
    private Map<UUID, Booking> bookings;
    private Map<UUID, CarType> carTypes;

    // ---------- Initialization ----------
    @PostConstruct
    public void init() {
        customers = new TreeMap<>();
        admins = new TreeMap<>();
        cars = new TreeMap<>();
        bookings = new TreeMap<>();
        carTypes = new TreeMap<>();
        populateApplicationState();
    }

    // ---------- Data access methods ----------
    public Map<UUID, Customer> getCustomers() { return customers; }
    public Map<UUID, Admin> getAdmins() { return admins; }
    public Map<UUID, Car> getCars() { return cars; }
    public Map<UUID, Booking> getBookings() { return bookings; }
    public Map<UUID, CarType> getCarTypes() { return carTypes; }

    // ---------- Data population ----------
    private void populateApplicationState() {
        // Admin
        Admin admin = new Admin(UUID.randomUUID(), "admin", "1234", "Admin User", "admin@example.com");
        admins.put(admin.getId(), admin);

        // Customers (with password)
        Customer customer1 = new Customer(UUID.randomUUID(), "Alice", "Wang", "alice@example.com",
                "alicePass123", "+41791234567", "CH-123456", LocalDate.of(2028, 6, 30),
                29, true, "Rue de Lausanne 45, Lausanne", 2000.0,
                LocalDateTime.now());
        customers.put(customer1.getId(), customer1);

        Customer customer2 = new Customer(UUID.randomUUID(), "Bob", "Li", "bob@example.com",
                "bobPass456", "+41797654321", "CH-654321", LocalDate.of(2029, 3, 15),
                35, true, "Rue de Genève 12, Lausanne", 1500.0,
                LocalDateTime.now());
        customers.put(customer2.getId(), customer2);

        // Car Types
        CarType sedan = new CarType(UUID.randomUUID(), "Sedan", "Toyota", "Camry", "2.5L",
                203, 210, 8.0, 1500, DriveType.FRONT_WHEEL_DRIVE, Transmission.AUTOMATIC,
                5, "Comfortable sedan", List.of("Air Conditioning", "Bluetooth"));
        carTypes.put(sedan.getId(), sedan);

        CarType suv = new CarType(UUID.randomUUID(), "SUV", "BMW", "X5", "3.0L",
                335, 240, 6.2, 2200, DriveType.FOUR_WHEEL_DRIVE, Transmission.AUTOMATIC,
                5, "Luxury SUV", List.of("Leather Seats", "Navigation"));
        carTypes.put(suv.getId(), suv);

        // Cars (dependent on CarType)
        Car car1 = new Car(UUID.randomUUID(), "VD12345", sedan, 100.0, 300.0, CarStatus.AVAILABLE,
                "https://example.com/camry.jpg", LocalDate.now(), LocalDate.now(), "VIN123456",
                "White", LocalDate.now().plusYears(1));
        cars.put(car1.getId(), car1);

        Car car2 = new Car(UUID.randomUUID(), "VD67890", suv, 200.0, 500.0, CarStatus.AVAILABLE,
                "https://example.com/x5.jpg", LocalDate.now(), LocalDate.now(), "VIN654321",
                "Black", LocalDate.now().plusYears(1));
        cars.put(car2.getId(), car2);

        // Bookings (dependent on existing Car and Customer)
        Booking booking1 = new Booking(UUID.randomUUID(), car1, customer1,
                LocalDate.now().plusDays(1), LocalDate.now().plusDays(3),
                300.0, 100.0, BookingStatus.CONFIRMED, PaymentStatus.SUCCESSFUL);
        bookings.put(booking1.getBookingId(), booking1);
    }
}