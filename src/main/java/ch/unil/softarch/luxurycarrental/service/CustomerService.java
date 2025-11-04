package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.Customer;
import ch.unil.softarch.luxurycarrental.domain.enums.BookingStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@ApplicationScoped
public class CustomerService {

    @Inject
    private ApplicationState state;

    // Create customer
    public Customer addCustomer(Customer customer) {
        // Check duplicates (email, license, phone)
        boolean emailExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getEmail().equals(customer.getEmail()));
        if (emailExists) throw new WebApplicationException("Email already exists", 400);

        boolean licenseExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getDrivingLicenseNumber().equals(customer.getDrivingLicenseNumber()));
        if (licenseExists) throw new WebApplicationException("Driving license number already exists", 400);

        boolean phoneExists = state.getCustomers().values().stream()
                .anyMatch(c -> c.getPhoneNumber().equals(customer.getPhoneNumber()));
        if (phoneExists) throw new WebApplicationException("Phone number already exists", 400);

        // Set ID and creation date
        if (customer.getId() == null) customer.setId(UUID.randomUUID());
        if (customer.getCreationDate() == null) customer.setCreationDate(LocalDateTime.now());

        // Save customer
        state.getCustomers().put(customer.getId(), customer);

        // --- Send confirmation email asynchronously ---
        String to = customer.getEmail();
        String subject = "Welcome to Luxury Car Rental!";
        String body = "Hi " + customer.getFirstName() + ",\n\n" +
                "Your account has been successfully created with the following details:\n" +
                "Name: " + customer.getFirstName() + " " + customer.getLastName() + "\n" +
                "Email: " + customer.getEmail() + "\n" +
                "Phone: " + customer.getPhoneNumber() + "\n" +
                "Driving License: " + customer.getDrivingLicenseNumber() + "\n\n" +
                "Thank you for choosing Luxury Car Rental!\n\n" +
                "Best regards,\nLuxury Car Rental Team";

        EmailSender.sendEmailAsync(to, subject, body);

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

    // Login verification
    public Customer authenticate(String email, String password) {
        return state.getCustomers().values().stream()
                .filter(c -> c.getEmail().equals(email)
                        && c.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new WebApplicationException("Invalid email or password", 401));
    }

    // Get customer information according to ID
    public Customer getCustomerById(UUID id) {
        Customer customer = state.getCustomers().get(id);
        if (customer == null) throw new WebApplicationException("Customer not found", 404);
        return customer;
    }


    /**
     * Send password reset code to customer's email.
     */
    public void sendPasswordResetCode(UUID customerId) {
        Customer customer = state.getCustomers().get(customerId);
        if (customer == null) {
            throw new WebApplicationException("Customer not found", 404);
        }

        // Generate random 6-digit code
        String code = String.format("%06d", new Random().nextInt(999999));

        // Save to application state
        state.getPasswordResetCodes().put(customerId,
                new ApplicationState.VerificationCode(code, LocalDateTime.now()));

        // Send email asynchronously
        String subject = "Password Reset Code";
        String body = "Hello " + customer.getFirstName() + ",\n\n" +
                "Your password reset code is: " + code + "\n" +
                "This code will expire in 5 minutes.\n\n" +
                "Luxury Car Rental Team";
        EmailSender.sendEmailAsync(customer.getEmail(), subject, body);
    }

    /**
     * Reset password using verification code.
     * The code is valid for 5 minutes and can only be used once.
     */
    public void resetPasswordWithCode(UUID customerId, String code, String newPassword) {
        Customer customer = state.getCustomers().get(customerId);
        if (customer == null) {
            throw new WebApplicationException("Customer not found", 404);
        }

        ApplicationState.VerificationCode stored = state.getPasswordResetCodes().get(customerId);
        if (stored == null) {
            throw new WebApplicationException("No verification code found", 400);
        }

        LocalDateTime now = LocalDateTime.now();
        // Check if expired (>= 5 minutes)
        if (Duration.between(stored.getCreatedAt(), now).toMinutes() >= 5) {
            state.getPasswordResetCodes().remove(customerId); // remove expired code
            throw new WebApplicationException("Verification code expired", 400);
        }

        // Check code correctness
        if (!stored.getCode().equals(code)) {
            throw new WebApplicationException("Invalid verification code", 400);
        }

        // Update password
        customer.setPassword(newPassword);

        // Invalidate the code immediately after successful use
        state.getPasswordResetCodes().remove(customerId);

        // Send email asynchronously
        String subject = "Your password has been changed";
        String body = "Hello " + customer.getFirstName() + ",\n\n" +
                "Your password has been successfully changed.\n" +
                "If you did not perform this change, please contact our support immediately.\n\n" +
                "Luxury Car Rental Team";
        EmailSender.sendEmailAsync(customer.getEmail(), subject, body);
    }
}