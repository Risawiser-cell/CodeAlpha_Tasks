
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Scanner;

public class HotelReservationSystem {
    // The program reads and writes reservations here when you run it.
    private static final Path BOOKINGS_FILE = Path.of("hotel_bookings.csv");

    private enum RoomCategory {
        STANDARD, DELUXE, SUITE;

        static RoomCategory fromInput(String input) {
            return RoomCategory.valueOf(input.trim().toUpperCase(Locale.ROOT));
        }
    }

    private static class Room {
        private final int id;
        private final RoomCategory category;
        private final double pricePerNight;

        Room(int id, RoomCategory category, double pricePerNight) {
            this.id = id;
            this.category = category;
            this.pricePerNight = pricePerNight;
        }
    }

    private static class Reservation {
        private final int id;
        private final String guestName;
        private final Room room;
        private final LocalDate checkIn;
        private final LocalDate checkOut;
        private final double totalPrice;
        private boolean active;

        Reservation(int id, String guestName, Room room, LocalDate checkIn,
                    LocalDate checkOut, double totalPrice, boolean active) {
            this.id = id;
            this.guestName = guestName;
            this.room = room;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalPrice = totalPrice;
            this.active = active;
        }
    }

    private static class DateRange {
        private final LocalDate checkIn;
        private final LocalDate checkOut;

        DateRange(LocalDate checkIn, LocalDate checkOut) {
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }
    }

    private final List<Room> rooms = new ArrayList<>();
    private final List<Reservation> reservations = new ArrayList<>();
    private int nextReservationId = 1;

    private HotelReservationSystem() {
        rooms.add(new Room(101, RoomCategory.STANDARD, 80.00));
        rooms.add(new Room(102, RoomCategory.STANDARD, 80.00));
        rooms.add(new Room(201, RoomCategory.DELUXE, 130.00));
        rooms.add(new Room(202, RoomCategory.DELUXE, 130.00));
        rooms.add(new Room(301, RoomCategory.SUITE, 220.00));
        rooms.add(new Room(302, RoomCategory.SUITE, 220.00));

        loadBookings();
    }

    public static void main(String[] args) {
        new HotelReservationSystem().run();
    }

    private void run() {
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.println("---Hotel Reservation System---");

            while (true) {
                System.out.println("\n1. Search rooms");
                System.out.println("2. Book a room");
                System.out.println("3. Cancel a reservation");
                System.out.println("4. View reservation details");
                System.out.println("5. Exit");
                System.out.print("Choose an option: ");

                String choice = scanner.nextLine().trim();

                switch (choice) {
                    case "1":
                        searchRooms(scanner);
                        break;
                    case "2":
                        bookRoom(scanner);
                        break;
                    case "3":
                        cancelReservation(scanner);
                        break;
                    case "4":
                        viewReservation(scanner);
                        break;
                    case "5":
                        System.out.println("Goodbye.");
                        return;
                    default:
                        System.out.println("Enter a number from 1 to 5.");
                }
            }
        }
    }

    private void searchRooms(Scanner scanner) {
        DateRange dates = readDates(scanner);
        if (dates == null) {
            return;
        }

        RoomCategory category = readCategory(scanner);
        if (category == null) {
            return;
        }

        List<Room> availableRooms =
                findAvailableRooms(category, dates.checkIn, dates.checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println("No rooms in that category are available for those dates.");
            return;
        }

        System.out.println("Available rooms:");
        for (Room room : availableRooms) {
            System.out.printf("Room %d | %s | $%.2f per night%n",
                    room.id, room.category, room.pricePerNight);
        }
    }

    private void bookRoom(Scanner scanner) {
        System.out.print("Guest name: ");
        String guestName = scanner.nextLine().trim().replace(",", " ");
        if (guestName.isEmpty()) {
            System.out.println("Guest name cannot be empty.");
            return;
        }

        DateRange dates = readDates(scanner);
        if (dates == null) {
            return;
        }

        RoomCategory category = readCategory(scanner);
        if (category == null) {
            return;
        }

        List<Room> availableRooms =
                findAvailableRooms(category, dates.checkIn, dates.checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println("No rooms in that category are available for those dates.");
            return;
        }

        System.out.println("Available rooms:");
        for (Room room : availableRooms) {
            System.out.printf("Room %d | $%.2f per night%n",
                    room.id, room.pricePerNight);
        }

        System.out.print("Enter the room number to book: ");
        int roomId;
        try {
            roomId = Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid room number.");
            return;
        }

        Room selectedRoom = null;
        for (Room room : availableRooms) {
            if (room.id == roomId) {
                selectedRoom = room;
                break;
            }
        }

        if (selectedRoom == null) {
            System.out.println("That room is not available for those dates.");
            return;
        }

        long nights = ChronoUnit.DAYS.between(dates.checkIn, dates.checkOut);
        double totalPrice = nights * selectedRoom.pricePerNight;

        System.out.printf("%nNights: %d%nTotal: $%.2f%n", nights, totalPrice);
        System.out.print("Simulate payment? (y/n): ");
        if (!scanner.nextLine().trim().equalsIgnoreCase("y")) {
            System.out.println("Payment cancelled. No reservation was made.");
            return;
        }

        System.out.println("Payment simulation successful.");

        Reservation reservation = new Reservation(
                nextReservationId,
                guestName,
                selectedRoom,
                dates.checkIn,
                dates.checkOut,
                totalPrice,
                true
        );

        reservations.add(reservation);
        if (!saveBookings()) {
            reservations.remove(reservation);
            System.out.println("Could not save the booking. No reservation was made.");
            return;
        }

        nextReservationId++;
        System.out.println("Reservation confirmed. ID: " + reservation.id);
    }

    private void cancelReservation(Scanner scanner) {
        System.out.print("Reservation ID to cancel: ");
        int id = readReservationId(scanner);
        if (id < 0) {
            return;
        }

        Reservation reservation = findReservation(id);
        if (reservation == null || !reservation.active) {
            System.out.println("Active reservation not found.");
            return;
        }

        reservation.active = false;
        if (!saveBookings()) {
            reservation.active = true;
            System.out.println("Could not save the cancellation.");
            return;
        }

        System.out.println("Reservation " + id + " has been cancelled.");
    }

    private void viewReservation(Scanner scanner) {
        System.out.print("Reservation ID: ");
        int id = readReservationId(scanner);
        if (id < 0) {
            return;
        }

        Reservation reservation = findReservation(id);
        if (reservation == null) {
            System.out.println("Reservation not found.");
            return;
        }

        System.out.println("\nReservation details");
        System.out.println("ID: " + reservation.id);
        System.out.println("Guest: " + reservation.guestName);
        System.out.println("Room: " + reservation.room.id);
        System.out.println("Category: " + reservation.room.category);
        System.out.println("Check-in: " + reservation.checkIn);
        System.out.println("Check-out: " + reservation.checkOut);
        System.out.printf("Total: $%.2f%n", reservation.totalPrice);
        System.out.println("Status: " + (reservation.active ? "Active" : "Cancelled"));
    }

    private DateRange readDates(Scanner scanner) {
        try {
            System.out.print("Check-in (YYYY-MM-DD): ");
            LocalDate checkIn = LocalDate.parse(scanner.nextLine().trim());

            System.out.print("Check-out (YYYY-MM-DD): ");
            LocalDate checkOut = LocalDate.parse(scanner.nextLine().trim());

            if (!checkOut.isAfter(checkIn)) {
                System.out.println("Check-out must be after check-in.");
                return null;
            }

            return new DateRange(checkIn, checkOut);
        } catch (Exception e) {
            System.out.println("Enter valid dates in YYYY-MM-DD format.");
            return null;
        }
    }

    private RoomCategory readCategory(Scanner scanner) {
        System.out.print("Category (standard, deluxe, suite): ");
        try {
            return RoomCategory.fromInput(scanner.nextLine());
        } catch (IllegalArgumentException e) {
            System.out.println("Unknown room category.");
            return null;
        }
    }

    private List<Room> findAvailableRooms(RoomCategory category,
                                          LocalDate checkIn, LocalDate checkOut) {
        List<Room> available = new ArrayList<>();

        for (Room room : rooms) {
            if (room.category == category && isAvailable(room, checkIn, checkOut)) {
                available.add(room);
            }
        }

        return available;
    }

    private boolean isAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        for (Reservation reservation : reservations) {
            if (!reservation.active || reservation.room.id != room.id) {
                continue;
            }

            boolean datesOverlap = checkIn.isBefore(reservation.checkOut)
                    && reservation.checkIn.isBefore(checkOut);

            if (datesOverlap) {
                return false;
            }
        }

        return true;
    }

    private Reservation findReservation(int id) {
        for (Reservation reservation : reservations) {
            if (reservation.id == id) {
                return reservation;
            }
        }
        return null;
    }

    private int readReservationId(Scanner scanner) {
        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid reservation ID.");
            return -1;
        }
    }

    // Loads saved bookings at startup. Availability is calculated from active bookings.
    private void loadBookings() {
        if (!Files.exists(BOOKINGS_FILE)) {
            return;
        }

        try {
            for (String line : Files.readAllLines(BOOKINGS_FILE, StandardCharsets.UTF_8)) {
                String[] fields = line.split(",", -1);
                if (fields.length != 8) {
                    continue;
                }

                int id = Integer.parseInt(fields[0]);
                String guestName = fields[1];
                int roomId = Integer.parseInt(fields[2]);
                RoomCategory category = RoomCategory.valueOf(fields[3]);
                LocalDate checkIn = LocalDate.parse(fields[4]);
                LocalDate checkOut = LocalDate.parse(fields[5]);
                double totalPrice = Double.parseDouble(fields[6]);
                boolean active = Boolean.parseBoolean(fields[7]);

                Room room = findRoom(roomId);
                if (room == null || room.category != category) {
                    continue;
                }

                reservations.add(new Reservation(
                        id, guestName, room, checkIn, checkOut, totalPrice, active
                ));
                nextReservationId = Math.max(nextReservationId, id + 1);
            }
        } catch (IOException | RuntimeException e) {
            System.out.println("Warning: bookings could not be fully loaded.");
        }
    }

    // Saves bookings, including cancellations, to the CSV file.
    private boolean saveBookings() {
        List<String> lines = new ArrayList<>();

        for (Reservation reservation : reservations) {
            lines.add(String.join(",",
                    Integer.toString(reservation.id),
                    reservation.guestName,
                    Integer.toString(reservation.room.id),
                    reservation.room.category.name(),
                    reservation.checkIn.toString(),
                    reservation.checkOut.toString(),
                    Double.toString(reservation.totalPrice),
                    Boolean.toString(reservation.active)
            ));
        }

        try {
            Files.write(BOOKINGS_FILE, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private Room findRoom(int id) {
        for (Room room : rooms) {
            if (room.id == id) {
                return room;
            }
        }
        return null;
    }
}
