package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.*;
import ch.unil.softarch.luxurycarrental.domain.enums.BookingStatus;
import ch.unil.softarch.luxurycarrental.domain.enums.CarStatus;
import ch.unil.softarch.luxurycarrental.domain.enums.PaymentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.util.Collection;
import java.util.UUID;

@ApplicationScoped
public class BookingService {

    @Inject
    private ApplicationState state;

    // ---------------- CRUD ----------------
    public Collection<Booking> getAllBookings() {
        return state.getBookings().values();
    }

    public Booking getBooking(UUID id) {
        return state.getBookings().get(id);
    }

    public Booking createBooking(Booking booking) {
        // Get car and customer from state
        Car car = state.getCars().get(booking.getCar().getId());
        Customer customer = state.getCustomers().get(booking.getCustomer().getId());

        // --- Validation ---
        if (car == null) throw new WebApplicationException("Car does not exist", 400);
        if (customer == null) throw new WebApplicationException("Customer does not exist", 400);
        if (car.getStatus() != CarStatus.AVAILABLE) throw new WebApplicationException("Car is not available", 400);
        if (customer.getBalance() < booking.getDepositAmount()) throw new WebApplicationException("Insufficient balance for deposit", 400);
        if (booking.getStartDate() == null || booking.getEndDate() == null) throw new WebApplicationException("Start date and end date are required", 400);
        if (booking.getEndDate().isBefore(booking.getStartDate())) throw new WebApplicationException("End date must be after start date", 400);

        // --- Update customer and car ---
        customer.setBalance(customer.getBalance() - booking.getDepositAmount());
        car.setStatus(CarStatus.UNAVAILABLE);

        // --- Set booking fields ---
        booking.setBookingStatus(BookingStatus.CONFIRMED);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        if (booking.getBookingId() == null) booking.setBookingId(UUID.randomUUID());

        // --- Add booking to state ---
        state.getBookings().put(booking.getBookingId(), booking);
        return booking;
    }

    public Booking updateBooking(UUID id, Booking update) {
        Booking existing = state.getBookings().get(id);
        if (existing == null) throw new WebApplicationException("Booking not found", 404);

        // --- Update car if changed ---
        if (update.getCar() != null && !update.getCar().getId().equals(existing.getCar().getId())) {
            Car newCar = state.getCars().get(update.getCar().getId());
            if (newCar == null) throw new WebApplicationException("New car does not exist", 400);
            if (newCar.getStatus() != CarStatus.AVAILABLE) throw new WebApplicationException("New car is not available", 400);

            // Release old car and occupy new car
            existing.getCar().setStatus(CarStatus.AVAILABLE);
            newCar.setStatus(CarStatus.UNAVAILABLE);
            existing.setCar(newCar);
        }

        // --- Update customer if changed ---
        if (update.getCustomer() != null && !update.getCustomer().getId().equals(existing.getCustomer().getId())) {
            Customer newCustomer = state.getCustomers().get(update.getCustomer().getId());
            if (newCustomer == null) throw new WebApplicationException("New customer does not exist", 400);
            // Refund deposit from old customer
            existing.getCustomer().setBalance(existing.getCustomer().getBalance() + existing.getDepositAmount());
            // Deduct deposit from new customer
            if (newCustomer.getBalance() < existing.getDepositAmount())
                throw new WebApplicationException("New customer has insufficient balance", 400);
            newCustomer.setBalance(newCustomer.getBalance() - existing.getDepositAmount());
            existing.setCustomer(newCustomer);
        }

        // --- Update other fields ---
        if (update.getTotalCost() > 0) existing.setTotalCost(update.getTotalCost());
        if (update.getDepositAmount() > 0) existing.setDepositAmount(update.getDepositAmount());
        if (update.getBookingStatus() != null) existing.setBookingStatus(update.getBookingStatus());
        if (update.getPaymentStatus() != null) existing.setPaymentStatus(update.getPaymentStatus());
        if (update.getStartDate() != null) existing.setStartDate(update.getStartDate());
        if (update.getEndDate() != null) existing.setEndDate(update.getEndDate());

        return existing;
    }

    public boolean removeBooking(UUID id) {
        Booking booking = state.getBookings().remove(id);
        if (booking != null) {
            // 恢复车辆状态
            booking.getCar().setStatus(CarStatus.AVAILABLE);
            // 退还押金
            booking.getCustomer().setBalance(booking.getCustomer().getBalance() + booking.getDepositAmount());
            return true;
        }
        return false;
    }

    // ---------------- Booking Operations ----------------
    public void completeBooking(UUID bookingId) {
        Booking booking = state.getBookings().get(bookingId);
        if (booking == null) throw new WebApplicationException("Booking not found", 404);

        Car car = booking.getCar();
        Customer customer = booking.getCustomer();

        car.setStatus(CarStatus.AVAILABLE);

        double remaining = booking.getTotalCost() - booking.getDepositAmount();
        if (remaining > 0) {
            if (customer.getBalance() < remaining) throw new WebApplicationException("Insufficient balance to complete booking", 400);
            customer.setBalance(customer.getBalance() - remaining);
        }

        booking.setBookingStatus(BookingStatus.COMPLETED);
        booking.setPaymentStatus(PaymentStatus.SUCCESSFUL);
    }

    public void cancelBooking(UUID bookingId) {
        Booking booking = state.getBookings().get(bookingId);
        if (booking == null) throw new WebApplicationException("Booking not found", 404);

        Car car = booking.getCar();
        Customer customer = booking.getCustomer();

        car.setStatus(CarStatus.AVAILABLE);
        customer.setBalance(customer.getBalance() + booking.getDepositAmount());

        booking.setBookingStatus(BookingStatus.CANCELLED);
        booking.setPaymentStatus(PaymentStatus.REFUNDED);
    }
}