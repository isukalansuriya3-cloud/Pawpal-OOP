package com.pawpal.ui;

import com.pawpal.dao.*;
import com.pawpal.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class ConsoleMenu {

    private final Scanner sc = new Scanner(System.in);

    private final UserDAO userDAO = new UserDAO();
    private final PetDAO petDAO = new PetDAO();
    private final VetDAO vetDAO = new VetDAO();
    private final AppointmentDAO apptDAO = new AppointmentDAO();

    private User currentUser;

    public void start() {
        boolean running = true;

        while (running) {
            printMenu();
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1:
                    register();
                    break;
                case 2:
                    login();
                    break;
                case 3:
                    viewVets();
                    break;
                case 4:
                    registerPet();
                    break;
                case 5:
                    bookAppointment();
                    break;
                case 6:
                    viewAppointments();
                    break;
                case 7:
                    updateAppointment();
                    break;
                case 8:
                    cancelAppointment();
                    break;
                case 9:
                    addVet();
                    break;
                case 10:
                    updateVet();
                    break;
                case 11:
                    deleteVet();
                    break;
                case 12:
                    running = false;
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Choose 1-12.");
            }
        }
    }

    private void printMenu() {
        System.out.println("\n================================");
        System.out.println(" PAWPAL - APPOINTMENT BOOKING");
        System.out.println("================================");

        if (currentUser == null) {
            System.out.println("Not logged in");
        } else {
            System.out.println(currentUser.getDashboardTitle()
                    + " | " + currentUser.getFullName());
        }

        System.out.println("1. Register");
        System.out.println("2. Login");
        System.out.println("3. View Veterinarians");
        System.out.println("4. Register Pet");
        System.out.println("5. Book Appointment");
        System.out.println("6. View Appointments");
        System.out.println("7. Update Appointment");
        System.out.println("8. Cancel Appointment");
        System.out.println("9. Add Veterinarian (Admin)");
        System.out.println("10. Update Veterinarian Profile");
        System.out.println("11. Delete Veterinarian (Admin)");
        System.out.println("12. Exit");
    }

    // 1. Register
    private void register() {
        System.out.println("\n--- Register ---");

        String name = readLine("Full name: ");
        String email = readLine("Email: ");
        String password = readLine("Password: ");
        String city = readLine("City: ");
        String type = readLine(
                "Register as (1 = Pet Owner, 2 = Veterinarian): ");

        try {
            User user;

            if (type.equals("1")) {
                user = new PetOwner(
                        0, name, email, password, city);

            } else if (type.equals("2")) {
                String specialization =
                        readLine("Specialization: ");
                String license =
                        readLine("License number: ");

                user = new Veterinarian(
                        0, name, email, password, city,
                        0, specialization, license);

            } else {
                System.out.println("Invalid registration type.");
                return;
            }

            if (userDAO.add(user)) {
                System.out.println(
                        "Registration successful! Please log in.");
            } else {
                System.out.println(
                        "Registration failed. Check the database connection "
                        + "and whether the email is already registered.");
            }

        } catch (IllegalArgumentException e) {
            // Validation errors from the model setters
            System.out.println(e.getMessage());
        }
    }

    // 2. Login
    private void login() {
        System.out.println("\n--- Login ---");

        String email = readLine("Email: ");
        String password = readLine("Password: ");

        User user = userDAO.login(email, password);

        if (user == null) {
            System.out.println("Invalid email or password.");
        } else {
            currentUser = user;

            System.out.println("Welcome, "
                    + currentUser.getFullName() + "!");
            System.out.println("Role: "
                    + currentUser.getRole());
        }
    }

    // 3. View Veterinarians  (READ)
    private void viewVets() {
        List<Veterinarian> vets = vetDAO.findAll();

        System.out.println("\n--- Veterinarians ---");

        if (vets.isEmpty()) {
            System.out.println(
                    "No veterinarians registered yet.");
            return;
        }

        System.out.printf("%-8s %-22s %-22s %-15s%n",
                "Vet ID", "Name", "Specialization", "City");

        for (Veterinarian vet : vets) {
            System.out.printf("%-8d %-22s %-22s %-15s%n",
                    vet.getVetId(),
                    vet.getFullName(),
                    vet.getSpecialization(),
                    vet.getCity());
        }
    }

    // 9. Add Veterinarian (Admin only)  (CREATE)
    private void addVet() {
        if (!requireLogin()) {
            return;
        }

        if (!(currentUser instanceof Admin)) {
            System.out.println("Only admins can add veterinarians.");
            return;
        }

        System.out.println("\n--- Add Veterinarian ---");

        String name = readLine("Full name: ");
        String email = readLine("Email: ");
        String password = readLine("Password: ");
        String city = readLine("City: ");
        String specialization = readLine("Specialization: ");
        String license = readLine("License number: ");

        try {
            Veterinarian vet = new Veterinarian(
                    0, name, email, password, city,
                    0, specialization, license);

            if (vetDAO.add(vet)) {
                System.out.println("Veterinarian added successfully!");
            } else {
                System.out.println("Could not add the veterinarian.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // 10. Update Veterinarian Profile  (UPDATE)
    // A vet edits their own profile; an admin can edit any vet.
    private void updateVet() {
        if (!requireLogin()) {
            return;
        }

        Veterinarian vet;

        if (currentUser instanceof Veterinarian) {
            vet = (Veterinarian) currentUser;
        } else if (currentUser instanceof Admin) {
            viewVets();
            int vetId = readInt("Enter Vet ID to update: ");
            vet = vetDAO.findById(vetId);

            if (vet == null) {
                System.out.println("Veterinarian not found.");
                return;
            }
        } else {
            System.out.println(
                    "Only veterinarians and admins can update vet profiles.");
            return;
        }

        System.out.println("\n--- Update Veterinarian Profile ---");

        String city = readLine("New city (" + vet.getCity() + "): ");
        String specialization = readLine(
                "New specialization (" + vet.getSpecialization() + "): ");
        String license = readLine(
                "New license number (" + vet.getLicenseNo() + "): ");

        try {
            vet.setCity(city);
            vet.setSpecialization(specialization);
            vet.setLicenseNo(license);

            if (vetDAO.update(vet)) {
                System.out.println("Profile updated successfully.");
            } else {
                System.out.println("Could not update the profile.");
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    // 11. Delete Veterinarian (Admin only)  (DELETE)
    private void deleteVet() {
        if (!requireLogin()) {
            return;
        }

        if (!(currentUser instanceof Admin)) {
            System.out.println("Only admins can delete veterinarians.");
            return;
        }

        viewVets();

        int vetId = readInt("Enter Vet ID to delete: ");
        String confirm = readLine(
                "Permanently delete this veterinarian? (y/n): ");

        if (confirm.equalsIgnoreCase("y")) {
            if (vetDAO.delete(vetId)) {
                System.out.println("Veterinarian deleted.");
            } else {
                System.out.println("Could not delete the veterinarian.");
            }
        }
    }

    // 4. Register Pet
    private void registerPet() {
        if (!requireOwner()) {
            return;
        }

        System.out.println("\n--- Register Pet ---");

        String petName = readLine("Pet name: ");
        String species = readLine("Species (Dog/Cat/etc.): ");
        String breed = readLine("Breed: ");
        int age = readInt("Age in years: ");

        if (age < 0) {
            System.out.println("Age cannot be negative.");
            return;
        }

        Pet pet = new Pet(
                0,
                currentUser.getUserId(),
                petName,
                species,
                breed,
                age
        );

        if (petDAO.add(pet)) {
            System.out.println(
                    "Pet registered successfully!");
        } else {
            System.out.println(
                    "Could not register the pet. Check the database.");
        }
    }

    // 5. Book Appointment
    private void bookAppointment() {
        if (!requireOwner()) {
            return;
        }

        List<Pet> pets =
                petDAO.findByOwner(currentUser.getUserId());

        if (pets.isEmpty()) {
            System.out.println(
                    "Please register a pet first using option 4.");
            return;
        }

        System.out.println("\n--- Your Pets ---");

        for (Pet pet : pets) {
            System.out.println(
                    pet.getPetId() + ". "
                    + pet.getPetName() + " ("
                    + pet.getSpecies() + ")");
        }

        int petId = readInt("Enter Pet ID: ");
        boolean ownsPet = false;

        for (Pet pet : pets) {
            if (pet.getPetId() == petId) {
                ownsPet = true;
                break;
            }
        }

        if (!ownsPet) {
            System.out.println(
                    "Invalid Pet ID. Choose one of your pets.");
            return;
        }

        viewVets();

        List<Veterinarian> vets = vetDAO.findAll();

        if (vets.isEmpty()) {
            return;
        }

        int vetId = readInt("Enter Vet ID: ");
        boolean vetExists = false;

        for (Veterinarian vet : vets) {
            if (vet.getVetId() == vetId) {
                vetExists = true;
                break;
            }
        }

        if (!vetExists) {
            System.out.println("Invalid Vet ID.");
            return;
        }

        LocalDate date = readDate();
        LocalTime time = readTime();
        String reason = readLine("Reason for visit: ");

        Appointment appointment = new Appointment();
        appointment.setPetId(petId);
        appointment.setVetId(vetId);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setReason(reason);

        if (apptDAO.add(appointment)) {
            System.out.println(
                    "Appointment booked successfully! Status: PENDING");
        } else {
            System.out.println(
                    "Booking failed. Check the database and "
                    + "appointment availability.");
        }
    }

    // 6. View Appointments
    private void viewAppointments() {
        if (!requireLogin()) {
            return;
        }

        List<Appointment> appointments =
                apptDAO.findForUser(currentUser);

        System.out.println("\n--- Appointments ---");
        printAppointments(appointments);
    }

    // 7. Update Appointment
    private void updateAppointment() {
        if (!requireLogin()) {
            return;
        }

        List<Appointment> appointments =
                apptDAO.findForUser(currentUser);

        printAppointments(appointments);

        if (appointments.isEmpty()) {
            return;
        }

        int id = readInt(
                "Enter Appointment ID to update: ");

        Appointment target = findById(appointments, id);

        if (target == null) {
            System.out.println("Appointment not found.");
            return;
        }

        if (currentUser.canBookAppointments()) {
            // Pet owner can reschedule an appointment.

            if ("CANCELLED".equals(target.getStatus())
                    || "COMPLETED".equals(target.getStatus())) {
                System.out.println(
                        "Cancelled or completed appointments "
                        + "cannot be rescheduled.");
                return;
            }

            LocalDate date = readDate();
            LocalTime time = readTime();
            String reason = readLine("New reason for visit: ");

            if (apptDAO.updateDetails(
                    target.getAppointmentId(),
                    date, time, reason)) {
                System.out.println(
                        "Appointment updated successfully.");
            } else {
                System.out.println(
                        "Could not update the appointment.");
            }

        } else if (currentUser instanceof Veterinarian
                || currentUser instanceof Admin) {
            // Veterinarian or admin can update appointment status.

            System.out.println("1. PENDING");
            System.out.println("2. CONFIRMED");
            System.out.println("3. COMPLETED");

            int choice = readInt("Select new status: ");
            String status;

            switch (choice) {
                case 1:
                    status = "PENDING";
                    break;
                case 2:
                    status = "CONFIRMED";
                    break;
                case 3:
                    status = "COMPLETED";
                    break;
                default:
                    System.out.println("Invalid choice.");
                    return;
            }

            if (apptDAO.updateStatus(
                    target.getAppointmentId(), status)) {
                System.out.println(
                        "Appointment status updated to " + status);
            } else {
                System.out.println(
                        "Could not update appointment status.");
            }
        }
    }

    // 8. Cancel Appointment
    private void cancelAppointment() {
        if (!requireLogin()) {
            return;
        }

        List<Appointment> appointments =
                apptDAO.findForUser(currentUser);

        printAppointments(appointments);

        if (appointments.isEmpty()) {
            return;
        }

        int id = readInt(
                "Enter Appointment ID to cancel: ");

        Appointment target = findById(appointments, id);

        if (target == null) {
            System.out.println("Appointment not found.");
            return;
        }

        if ("CANCELLED".equals(target.getStatus())) {
            System.out.println(
                    "This appointment is already cancelled.");
            return;
        }

        if (currentUser instanceof Admin) {
            String confirm = readLine(
                    "Permanently delete this appointment? (y/n): ");

            if (confirm.equalsIgnoreCase("y")) {
                if (apptDAO.delete(target.getAppointmentId())) {
                    System.out.println("Appointment deleted.");
                } else {
                    System.out.println(
                            "Could not delete the appointment.");
                }
            }
        } else {
            if (apptDAO.updateStatus(
                    target.getAppointmentId(), "CANCELLED")) {
                System.out.println("Appointment cancelled.");
            } else {
                System.out.println(
                        "Could not cancel the appointment.");
            }
        }
    }

    // Helper methods

    private boolean requireLogin() {
        if (currentUser == null) {
            System.out.println(
                    "Please log in first using option 2.");
            return false;
        }

        return true;
    }

    private boolean requireOwner() {
        if (!requireLogin()) {
            return false;
        }

        if (!currentUser.canBookAppointments()) {
            System.out.println(
                    "Only pet owners can use this option.");
            return false;
        }

        return true;
    }

    private Appointment findById(
            List<Appointment> appointments, int id) {

        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId() == id) {
                return appointment;
            }
        }

        return null;
    }

    private void printAppointments(
            List<Appointment> appointments) {

        if (appointments.isEmpty()) {
            System.out.println("No appointments found.");
            return;
        }

        System.out.printf(
                "%-4s %-12s %-8s %-14s %-16s %-16s %-12s %s%n",
                "ID", "Date", "Time", "Pet",
                "Owner", "Vet", "Status", "Reason");

        for (Appointment appointment : appointments) {
            System.out.printf(
                    "%-4d %-12s %-8s %-14s %-16s %-16s %-12s %s%n",
                    appointment.getAppointmentId(),
                    appointment.getDate(),
                    appointment.getTime(),
                    appointment.getPetName(),
                    appointment.getOwnerName(),
                    appointment.getVetName(),
                    appointment.getStatus(),
                    appointment.getReason());
        }
    }

    private String readLine(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    private int readInt(String prompt) {
        while (true) {
            try {
                return Integer.parseInt(readLine(prompt));
            } catch (NumberFormatException e) {
                System.out.println(
                        "Please enter a valid number.");
            }
        }
    }

    private LocalDate readDate() {
        while (true) {
            try {
                LocalDate date = LocalDate.parse(
                        readLine("Date (yyyy-MM-dd): "));

                if (date.isBefore(LocalDate.now())) {
                    System.out.println(
                            "Date cannot be in the past.");
                    continue;
                }

                return date;

            } catch (DateTimeParseException e) {
                System.out.println(
                        "Use yyyy-MM-dd, for example 2026-11-20.");
            }
        }
    }

    private LocalTime readTime() {
        while (true) {
            try {
                return LocalTime.parse(
                        readLine("Time (HH:mm, 24-hour): "));
            } catch (DateTimeParseException e) {
                System.out.println(
                        "Use HH:mm, for example 14:30.");
            }
        }
    }
}
