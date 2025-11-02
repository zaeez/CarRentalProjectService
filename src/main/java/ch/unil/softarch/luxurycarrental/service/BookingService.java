package ch.unil.softarch.luxurycarrental.service;

import ch.unil.softarch.luxurycarrental.domain.ApplicationState;
import ch.unil.softarch.luxurycarrental.domain.entities.*;
import ch.unil.softarch.luxurycarrental.domain.enums.BookingStatus;
import ch.unil.softarch.luxurycarrental.domain.enums.CarStatus;
import ch.unil.softarch.luxurycarrental.domain.enums.PaymentStatus;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;

import java.io.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

    // Get bookings by customer ID
    public List<Booking> getBookingsByCustomerId(UUID customerId) {
        return state.getBookings().values().stream()
                .filter(b -> b.getCustomer() != null && b.getCustomer().getId().equals(customerId))
                .collect(Collectors.toList());
    }

    // Get bookings by car ID
    public List<Booking> getBookingsByCarId(UUID carId) {
        return state.getBookings().values().stream()
                .filter(b -> b.getCar() != null && b.getCar().getId().equals(carId))
                .collect(Collectors.toList());
    }

    public Booking createBooking(Booking booking) {
        Car car = state.getCars().get(booking.getCar().getId());
        Customer customer = state.getCustomers().get(booking.getCustomer().getId());

        if (car == null) throw new WebApplicationException("Car does not exist", 400);
        if (customer == null) throw new WebApplicationException("Customer does not exist", 400);
        if (car.getStatus() != CarStatus.AVAILABLE) throw new WebApplicationException("Car is not available", 400);
        if (customer.getBalance() < booking.getDepositAmount()) throw new WebApplicationException("Insufficient balance", 400);
        if (booking.getStartDate() == null || booking.getEndDate() == null)
            throw new WebApplicationException("Start date and end date are required", 400);
        if (booking.getEndDate().isBefore(booking.getStartDate()))
            throw new WebApplicationException("End date must be after start date", 400);


        booking.setBookingStatus(BookingStatus.PENDING);
        booking.setPaymentStatus(PaymentStatus.PENDING);
        if (booking.getBookingId() == null) booking.setBookingId(UUID.randomUUID());

        state.getBookings().put(booking.getBookingId(), booking);

        // --- Prepare HTML mail ---
        String htmlBody = "<h2>Booking Confirmation</h2>" +
                "<p>Hi <b>" + customer.getFirstName() + " " + customer.getLastName() + "</b>,</p>" +
                "<p>Your booking has been <b>confirmed</b>! Here are the details:</p>" +
                "<h3>Car Details</h3>" +
                "<table border='1' cellpadding='5'>" +
                "<tr><td>Brand & Model</td><td>" + car.getCarType().getBrand() + " " + car.getCarType().getModel() + "</td></tr>" +
                "<tr><td>Category</td><td>" + car.getCarType().getCategory() + "</td></tr>" +
                "<tr><td>Engine</td><td>" + car.getCarType().getEngine() + "</td></tr>" +
                "<tr><td>Power</td><td>" + car.getCarType().getPower() + " HP</td></tr>" +
                "<tr><td>Max Speed</td><td>" + car.getCarType().getMaxSpeed() + " km/h</td></tr>" +
                "<tr><td>Seats</td><td>" + car.getCarType().getSeats() + "</td></tr>" +
                "<tr><td>Transmission</td><td>" + car.getCarType().getTransmission() + "</td></tr>" +
                "<tr><td>Drive Type</td><td>" + car.getCarType().getDriveType() + "</td></tr>" +
                "<tr><td>Color</td><td>" + car.getColor() + "</td></tr>" +
                "<tr><td>License Plate</td><td>" + car.getLicensePlate() + "</td></tr>" +
                "<tr><td>VIN</td><td>" + car.getVin() + "</td></tr>" +
                "<tr><td>Insurance Expiry</td><td>" + car.getInsuranceExpiryDate() + "</td></tr>" +
                "</table>" +
                "<h3>Booking Details</h3>" +
                "<table border='1' cellpadding='5'>" +
                "<tr><td>Start Date</td><td>" + booking.getStartDate() + "</td></tr>" +
                "<tr><td>End Date</td><td>" + booking.getEndDate() + "</td></tr>" +
                "<tr><td>Deposit</td><td>" + booking.getDepositAmount() + "</td></tr>" +
                "<tr><td>Total Cost</td><td>" + booking.getTotalCost() + "</td></tr>" +
                "<tr><td>Payment Status</td><td>" + booking.getPaymentStatus() + "</td></tr>" +
                "</table>" +
                "<h3>Customer Details</h3>" +
                "<table border='1' cellpadding='5'>" +
                "<tr><td>Name</td><td>" + customer.getFirstName() + " " + customer.getLastName() + "</td></tr>" +
                "<tr><td>Email</td><td>" + customer.getEmail() + "</td></tr>" +
                "<tr><td>Phone</td><td>" + customer.getPhoneNumber() + "</td></tr>" +
                "<tr><td>Address</td><td>" + customer.getBillingAddress() + "</td></tr>" +
                "<tr><td>Driving License</td><td>" + customer.getDrivingLicenseNumber() + " (Expires: " + customer.getDrivingLicenseExpiryDate() + ")</td></tr>" +
                "</table>" +
                "<p>Thank you for choosing <b>Luxury Car Rental</b>!</p>";

        // --- Generate PDF attachments ---
        byte[] pdfBytes;
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document doc = new Document(pdfDoc);
            doc.add(new Paragraph("Booking Confirmation"));
            doc.add(new Paragraph("Customer: " + customer.getFirstName() + " " + customer.getLastName()));
            doc.add(new Paragraph("Email: " + customer.getEmail()));
            doc.add(new Paragraph("Phone: " + customer.getPhoneNumber()));
            doc.add(new Paragraph("Address: " + customer.getBillingAddress()));
            doc.add(new Paragraph("Driving License: " + customer.getDrivingLicenseNumber() +
                    " (Expiry: " + customer.getDrivingLicenseExpiryDate() + ")"));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Car Details: " + car.getCarType().getBrand() + " " + car.getCarType().getModel()));
            doc.add(new Paragraph("Category: " + car.getCarType().getCategory()));
            doc.add(new Paragraph("Engine: " + car.getCarType().getEngine() + ", Power: " + car.getCarType().getPower() + " HP"));
            doc.add(new Paragraph("Max Speed: " + car.getCarType().getMaxSpeed() + " km/h"));
            doc.add(new Paragraph("Seats: " + car.getCarType().getSeats()));
            doc.add(new Paragraph("Transmission: " + car.getCarType().getTransmission()));
            doc.add(new Paragraph("Drive Type: " + car.getCarType().getDriveType()));
            doc.add(new Paragraph("Color: " + car.getColor()));
            doc.add(new Paragraph("License Plate: " + car.getLicensePlate()));
            doc.add(new Paragraph("VIN: " + car.getVin()));
            doc.add(new Paragraph("Insurance Expiry: " + car.getInsuranceExpiryDate()));
            doc.add(new Paragraph(" "));
            doc.add(new Paragraph("Booking Details:"));
            doc.add(new Paragraph("Start Date: " + booking.getStartDate()));
            doc.add(new Paragraph("End Date: " + booking.getEndDate()));
            doc.add(new Paragraph("Deposit: " + booking.getDepositAmount()));
            doc.add(new Paragraph("Total Cost: " + booking.getTotalCost()));
            doc.add(new Paragraph("Payment Status: " + booking.getPaymentStatus()));
            doc.close();
            pdfBytes = baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            pdfBytes = new byte[0];
        }

        // --- Asynchronous email sending ---
        final byte[] finalPdfBytes = pdfBytes;
        new Thread(() -> EmailSender.sendEmailWithAttachment(
                customer.getEmail(),
                "Booking Confirmation - Luxury Car Rental",
                htmlBody,
                finalPdfBytes
        )).start();

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