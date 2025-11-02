package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.Customer;
import ch.unil.softarch.luxurycarrental.domain.enums.BookingStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CustomerService {

    @Inject
    private ApplicationState state;

    // Create
    public Customer addCustomer(Customer customer) {
        // Check for duplicate email
        boolean emailExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getEmail().equals(customer.getEmail()));
        if (emailExists) {
            throw new WebApplicationException("Email already exists", 400);
        }

        // Check for duplicate driving license number
        boolean licenseExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getDrivingLicenseNumber().equals(customer.getDrivingLicenseNumber()));
        if (licenseExists) {
            throw new WebApplicationException("Driving license number already exists", 400);
        }

        // Optionally check phone number uniqueness
        boolean phoneExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getPhoneNumber().equals(customer.getPhoneNumber()));
        if (phoneExists) {
            throw new WebApplicationException("Phone number already exists", 400);
        }

        // Set ID and creation date if missing
        if (customer.getId() == null) customer.setId(UUID.randomUUID());
        if (customer.getCreationDate() == null) customer.setCreationDate(LocalDateTime.now());

        state.getCustomers().put(customer.getId(), customer);
        return customer;
    }

    // Read
    public Customer getCustomer(UUID id) {
        Customer customer = state.getCustomers().get(id);
        if (customer == null) throw new WebApplicationException("Customer not found", 404);
        return customer;
    }

    public List<Customer> getAllCustomers() {
        return state.getCustomers().values().stream().collect(Collectors.toList());
    }

    // Update
    public Customer updateCustomer(UUID id, Customer update) {
        Customer existing = state.getCustomers().get(id);
        if (existing == null) throw new WebApplicationException("Customer not found", 404);

        if (update.getFirstName() != null) existing.setFirstName(update.getFirstName());
        if (update.getLastName() != null) existing.setLastName(update.getLastName());
        if (update.getEmail() != null) existing.setEmail(update.getEmail());
        if (update.getPhoneNumber() != null) existing.setPhoneNumber(update.getPhoneNumber());
        if (update.getDrivingLicenseNumber() != null) existing.setDrivingLicenseNumber(update.getDrivingLicenseNumber());
        if (update.getDrivingLicenseExpiryDate() != null) existing.setDrivingLicenseExpiryDate(update.getDrivingLicenseExpiryDate());
        if (update.getAge() > 0) existing.setAge(update.getAge());
        existing.setVerifiedIdentity(update.isVerifiedIdentity());
        if (update.getBillingAddress() != null) existing.setBillingAddress(update.getBillingAddress());
        if (update.getBalance() != 0.0) existing.setBalance(update.getBalance());

        return existing;
    }

    public boolean removeCustomer(UUID id) {
        Customer customer = state.getCustomers().get(id);
        if (customer == null) {
            throw new WebApplicationException("Customer not found", 404);
        }

        // Check if customer has active bookings
        boolean hasActiveBookings = state.getBookings().values().stream()
                .anyMatch(b -> b.getCustomer().getId().equals(id) &&
                        b.getBookingStatus() != BookingStatus.COMPLETED);

        if (hasActiveBookings) {
            throw new WebApplicationException(
                    "Cannot delete customer with active bookings", 400);
        }

        state.getCustomers().remove(id);
        System.out.println("Deleted Customer " + id);
        return true;
    }
}