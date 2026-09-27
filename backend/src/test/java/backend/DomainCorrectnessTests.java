package backend;

import backend.auth.dto.LoginRequest;
import backend.auth.dto.RegisterRequest;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResult;
import backend.booking.model.Booking;
import backend.booking.repository.BookingRepository;
import backend.booking.service.BookingService;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;
import backend.common.enums.SlotStatus;
import backend.common.enums.WaitlistStatus;
import backend.common.repository.UserRepository;
import backend.auth.service.AuthService;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;
import backend.professor.exception.SlotOverlapException;
import backend.professor.model.Slot;
import backend.professor.repository.SlotRepository;
import backend.professor.service.SlotService;
import backend.waitlist.model.WaitlistEntry;
import backend.waitlist.repository.WaitlistEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Regression tests for confirmed domain defects from the backend correctness audit.
 * All tests run against H2 in-memory database with create-drop DDL.
 */
@SpringBootTest
@Transactional
class DomainCorrectnessTests {

    @Autowired UserRepository userRepository;
    @Autowired SlotRepository slotRepository;
    @Autowired BookingRepository bookingRepository;
    @Autowired WaitlistEntryRepository waitlistEntryRepository;
    @Autowired SlotService slotService;
    @Autowired BookingService bookingService;
    @Autowired backend.waitlist.service.WaitlistService waitlistService;
    @Autowired AuthService authService;
    @Autowired PasswordEncoder passwordEncoder;

    private User professor;
    private User student1;
    private User student2;

    // Use a future date to avoid @FutureOrPresent rejection
    private static final LocalDate FUTURE_DATE = LocalDate.now().plusDays(7);

    @BeforeEach
    void setUp() {
        // Create a professor user directly
        professor = new User();
        professor.setFullName("Dr. Test Professor");
        professor.setEmail("prof@test.edu");
        professor.setPassword(passwordEncoder.encode("password123"));
        professor.setRole(Role.PROFESSOR);
        professor = userRepository.save(professor);

        // Create professor profile via registration indirectly is complex,
        // so create a minimal professor profile manually
        backend.professor.model.ProfessorProfile profProfile = new backend.professor.model.ProfessorProfile();
        profProfile.setUser(professor);
        profProfile.setDepartment("Computer Science");

        // Use EntityManager to persist since we don't want to import too much
        // Actually, we can use ProfessorProfileRepository
    }

    private User createStudent(String email, String rollNo) {
        User student = new User();
        student.setFullName("Student " + email);
        student.setEmail(email);
        student.setPassword(passwordEncoder.encode("password123"));
        student.setRole(Role.STUDENT);
        student = userRepository.save(student);

        backend.student.model.StudentProfile profile = new backend.student.model.StudentProfile();
        profile.setUser(student);
        profile.setRollNo(rollNo);
        profile.setYearOfStudy(3);

        return student;
    }

    // ========================
    // Finding 1: User JPA Column Mapping
    // ========================

    @Nested
    @DisplayName("Finding 1: User JPA Column Mapping")
    class UserColumnMappingTests {

        @Test
        @DisplayName("Role field stores enum correctly and is not mapped to is_active column")
        void roleFieldStoresEnumCorrectly() {
            User user = new User();
            user.setFullName("Column Test");
            user.setEmail("coltest@test.edu");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.STUDENT);
            user.setActive(true);
            User saved = userRepository.save(user);

            // Reload from database to verify persistence
            User reloaded = userRepository.findById(saved.getId()).orElseThrow();
            assertEquals(Role.STUDENT, reloaded.getRole(), "Role should round-trip correctly");
            assertTrue(reloaded.isActive(), "Active flag should round-trip correctly");
        }

        @Test
        @DisplayName("Deactivated user preserves role after reload")
        void deactivatedUserPreservesRole() {
            User user = new User();
            user.setFullName("Deactivated User");
            user.setEmail("deact@test.edu");
            user.setPassword(passwordEncoder.encode("password123"));
            user.setRole(Role.PROFESSOR);
            user.setActive(false);
            User saved = userRepository.save(user);

            User reloaded = userRepository.findById(saved.getId()).orElseThrow();
            assertEquals(Role.PROFESSOR, reloaded.getRole(), "Role must not be affected by active flag");
            assertFalse(reloaded.isActive(), "Active flag must persist as false");
        }

        @Test
        @DisplayName("All three roles can coexist with active/inactive states")
        void allRolesCoexist() {
            for (Role role : Role.values()) {
                User user = new User();
                user.setFullName("User-" + role);
                user.setEmail(role.name().toLowerCase() + "@test.edu");
                user.setPassword(passwordEncoder.encode("password123"));
                user.setRole(role);
                user.setActive(role != Role.ADMIN); // ADMIN inactive for variety
                userRepository.save(user);
            }

            List<User> all = userRepository.findAll();
            // Filter to our test users
            long studentCount = all.stream().filter(u -> u.getRole() == Role.STUDENT && u.getEmail().endsWith("@test.edu")).count();
            long profCount = all.stream().filter(u -> u.getRole() == Role.PROFESSOR && u.getEmail().endsWith("@test.edu")).count();
            long adminCount = all.stream().filter(u -> u.getRole() == Role.ADMIN).count();

            assertTrue(studentCount >= 1, "At least one STUDENT should exist");
            assertTrue(profCount >= 1, "At least one PROFESSOR should exist");
            assertTrue(adminCount >= 1, "At least one ADMIN should exist");
        }
    }

    // ========================
    // Finding 2: Slot Overlap Validation
    // ========================

    @Nested
    @DisplayName("Finding 2: Slot Overlap Validation")
    class SlotOverlapTests {

        @BeforeEach
        void createBaseSlot() {
            // Create a slot from 10:00-11:00 on FUTURE_DATE
            slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(10, 0), LocalTime.of(11, 0), 2));
        }

        @Test
        @DisplayName("(a) Existing 10:00-11:00, new 10:30-11:30 should be rejected")
        void overlappingEndOverlap() {
            assertThrows(SlotOverlapException.class, () ->
                    slotService.createSlot(professor.getId(), new SlotRequest(
                            FUTURE_DATE, LocalTime.of(10, 30), LocalTime.of(11, 30), 1)));
        }

        @Test
        @DisplayName("(b) Existing 10:00-11:00, new 09:30-10:30 should be rejected")
        void overlappingStartOverlap() {
            assertThrows(SlotOverlapException.class, () ->
                    slotService.createSlot(professor.getId(), new SlotRequest(
                            FUTURE_DATE, LocalTime.of(9, 30), LocalTime.of(10, 30), 1)));
        }

        @Test
        @DisplayName("(c) Existing 10:00-11:00, new 10:30-11:00 should be rejected")
        void subsetOverlap() {
            assertThrows(SlotOverlapException.class, () ->
                    slotService.createSlot(professor.getId(), new SlotRequest(
                            FUTURE_DATE, LocalTime.of(10, 30), LocalTime.of(11, 0), 1)));
        }

        @Test
        @DisplayName("(d) Existing 10:00-11:00, new 09:00-12:00 should be rejected")
        void supersetOverlap() {
            assertThrows(SlotOverlapException.class, () ->
                    slotService.createSlot(professor.getId(), new SlotRequest(
                            FUTURE_DATE, LocalTime.of(9, 0), LocalTime.of(12, 0), 1)));
        }

        @Test
        @DisplayName("(e) Adjacent slots 09:00-10:00 and 10:00-11:00 should be allowed")
        void adjacentSlotsAllowed() {
            // 09:00-10:00 is adjacent but NOT overlapping with existing 10:00-11:00
            SlotResponse adjacent = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(9, 0), LocalTime.of(10, 0), 1));
            assertNotNull(adjacent.getSlotId(), "Adjacent slot should be created successfully");
        }

        @Test
        @DisplayName("Adjacent slot 11:00-12:00 after existing 10:00-11:00 should be allowed")
        void adjacentAfterSlotAllowed() {
            SlotResponse adjacent = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(11, 0), LocalTime.of(12, 0), 1));
            assertNotNull(adjacent.getSlotId(), "Adjacent-after slot should be created successfully");
        }

        @Test
        @DisplayName("Same time slot on different date should be allowed")
        void sameDateDifferentDay() {
            SlotResponse slot = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE.plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0), 1));
            assertNotNull(slot.getSlotId(), "Same time on different date should be allowed");
        }
    }

    // ========================
    // Finding 3: Slot Cancellation Consistency
    // ========================

    @Nested
    @DisplayName("Finding 3: Slot Cancellation Consistency")
    class SlotCancellationTests {

        @Test
        @DisplayName("Cancelling a slot cascades cancellation to active bookings")
        void cancelSlotCascadesToBookings() {
            // Create slot with capacity 2
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(14, 0), LocalTime.of(15, 0), 2));
            Long slotId = slotResponse.getSlotId();

            // Create two students and book them
            User s1 = createStudent("s1cancel@test.edu", "ROLL-S1C");
            User s2 = createStudent("s2cancel@test.edu", "ROLL-S2C");

            // Need student profiles for booking
            backend.student.model.StudentProfile sp1 = new backend.student.model.StudentProfile();
            sp1.setUser(s1); sp1.setRollNo("ROLL-S1C"); sp1.setYearOfStudy(2);

            backend.student.model.StudentProfile sp2 = new backend.student.model.StudentProfile();
            sp2.setUser(s2); sp2.setRollNo("ROLL-S2C"); sp2.setYearOfStudy(2);

            bookingService.createBooking(s1.getId(), new BookingRequest(slotId));
            bookingService.createBooking(s2.getId(), new BookingRequest(slotId));

            // Verify bookings exist
            List<Booking> before = bookingRepository.findBySlotIdAndStatus(slotId, BookingStatus.BOOKED);
            assertEquals(2, before.size(), "Both bookings should be active");

            // Cancel the slot
            slotService.cancelSlot(professor.getId(), slotId);

            // Verify slot is cancelled
            Slot slot = slotRepository.findById(slotId).orElseThrow();
            assertEquals(SlotStatus.CANCELLED, slot.getStatus());

            // Verify bookings are cancelled
            List<Booking> afterBooked = bookingRepository.findBySlotIdAndStatus(slotId, BookingStatus.BOOKED);
            assertEquals(0, afterBooked.size(), "No bookings should remain active after slot cancellation");

            // Verify all bookings got CANCELLED status (not deleted)
            List<Booking> allBookings = bookingRepository.findByStudentId(s1.getId());
            allBookings.addAll(bookingRepository.findByStudentId(s2.getId()));
            assertTrue(allBookings.stream().allMatch(b -> b.getStatus() == BookingStatus.CANCELLED),
                    "All bookings should have CANCELLED status (history preserved)");
        }

        @Test
        @DisplayName("Cancelling a slot cascades cancellation to waitlist entries")
        void cancelSlotCascadesToWaitlist() {
            // Create slot with capacity 1
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(15, 0), LocalTime.of(16, 0), 1));
            Long slotId = slotResponse.getSlotId();

            // Student 1 books, student 2 gets waitlisted
            User s1 = createStudent("s1w@test.edu", "ROLL-S1W");
            User s2 = createStudent("s2w@test.edu", "ROLL-S2W");

            bookingService.createBooking(s1.getId(), new BookingRequest(slotId));
            BookingResult result = bookingService.createBooking(s2.getId(), new BookingRequest(slotId));
            assertEquals(BookingResult.TYPE_WAITLISTED, result.getType(), "Student 2 should be waitlisted");

            // Cancel the slot
            slotService.cancelSlot(professor.getId(), slotId);

            // Verify waitlist entries are cancelled
            List<WaitlistEntry> waiting = waitlistEntryRepository.findBySlotIdAndStatus(slotId, WaitlistStatus.WAITING);
            assertEquals(0, waiting.size(), "No waitlist entries should remain WAITING after slot cancellation");
        }

        @Test
        @DisplayName("Promote next on a cancelled slot does nothing and creates no bookings")
        void promoteNextOnCancelledSlotDoesNothing() {
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(19, 0), LocalTime.of(20, 0), 1));
            Long slotId = slotResponse.getSlotId();

            User s1 = createStudent("pnc1@test.edu", "ROLL-PNC1");
            User s2 = createStudent("pnc2@test.edu", "ROLL-PNC2");

            bookingService.createBooking(s1.getId(), new BookingRequest(slotId));
            bookingService.createBooking(s2.getId(), new BookingRequest(slotId));

            // Cancel the slot
            slotService.cancelSlot(professor.getId(), slotId);

            // Attempt to trigger promoteNext manually on the cancelled slot
            waitlistService.promoteNext(slotId);

            // Verify no new bookings were created for student 2
            List<Booking> s2Bookings = bookingRepository.findByStudentId(s2.getId());
            boolean hasBooked = s2Bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.BOOKED);
            assertFalse(hasBooked, "Cancelled slot must never promote waitlist entries to BOOKED");
        }

        @Test
        @DisplayName("Joining waitlist on a cancelled slot is rejected with ConflictException")
        void joinWaitlistOnCancelledSlotRejected() {
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(20, 0), LocalTime.of(21, 0), 1));
            Long slotId = slotResponse.getSlotId();

            slotService.cancelSlot(professor.getId(), slotId);

            User s1 = createStudent("jwc1@test.edu", "ROLL-JWC1");
            assertThrows(backend.exception.ConflictException.class, () ->
                    waitlistService.joinWaitlist(s1.getId(), slotId),
                    "Joining waitlist on cancelled slot must be rejected");
        }
    }

    // ========================
    // Finding 4: Same-Day Slot Validation
    // ========================

    @Nested
    @DisplayName("Finding 4: Same-Day Slot Validation")
    class SameDaySlotTests {

        @Test
        @DisplayName("Slot creation for today should not be rejected by validation")
        void todaySlotAllowed() {
            // Create a slot for later today - this should work with @FutureOrPresent
            SlotRequest request = new SlotRequest(
                    LocalDate.now(),
                    LocalTime.of(23, 0),
                    LocalTime.of(23, 30),
                    1);
            SlotResponse response = slotService.createSlot(professor.getId(), request);
            assertNotNull(response.getSlotId(), "Slot for today should be created");
            assertEquals(LocalDate.now(), response.getSlotDate());
        }

        @Test
        @DisplayName("Slot creation for future date should still work")
        void futureDateSlotAllowed() {
            SlotRequest request = new SlotRequest(
                    FUTURE_DATE,
                    LocalTime.of(10, 0),
                    LocalTime.of(11, 0),
                    1);
            SlotResponse response = slotService.createSlot(professor.getId(), request);
            assertNotNull(response.getSlotId(), "Slot for future date should be created");
        }
    }

    // ========================
    // Booking Concurrency and Capacity
    // ========================

    @Nested
    @DisplayName("Booking Safety: Capacity and Duplicate Prevention")
    class BookingSafetyTests {

        @Test
        @DisplayName("Booking fills slot and waitlists next student")
        void bookingFillsSlotAndWaitlists() {
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(16, 0), LocalTime.of(17, 0), 1));
            Long slotId = slotResponse.getSlotId();

            User s1 = createStudent("fill1@test.edu", "ROLL-FILL1");
            User s2 = createStudent("fill2@test.edu", "ROLL-FILL2");

            BookingResult r1 = bookingService.createBooking(s1.getId(), new BookingRequest(slotId));
            assertEquals(BookingResult.TYPE_BOOKED, r1.getType(), "First student should be booked");

            BookingResult r2 = bookingService.createBooking(s2.getId(), new BookingRequest(slotId));
            assertEquals(BookingResult.TYPE_WAITLISTED, r2.getType(), "Second student should be waitlisted");

            // Verify slot is now FULL
            Slot slot = slotRepository.findById(slotId).orElseThrow();
            assertEquals(SlotStatus.FULL, slot.getStatus());
            assertEquals(1, slot.getBookedCount());
        }

        @Test
        @DisplayName("Duplicate booking for same slot by same student is rejected")
        void duplicateBookingRejected() {
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(17, 0), LocalTime.of(18, 0), 5));
            Long slotId = slotResponse.getSlotId();

            User s1 = createStudent("dup@test.edu", "ROLL-DUP");
            bookingService.createBooking(s1.getId(), new BookingRequest(slotId));

            assertThrows(Exception.class, () ->
                    bookingService.createBooking(s1.getId(), new BookingRequest(slotId)),
                    "Duplicate booking should be rejected");
        }

        @Test
        @DisplayName("Waitlist promotion after cancellation")
        void waitlistPromotionAfterCancellation() {
            SlotResponse slotResponse = slotService.createSlot(professor.getId(), new SlotRequest(
                    FUTURE_DATE, LocalTime.of(18, 0), LocalTime.of(19, 0), 1));
            Long slotId = slotResponse.getSlotId();

            User s1 = createStudent("promo1@test.edu", "ROLL-P1");
            User s2 = createStudent("promo2@test.edu", "ROLL-P2");

            BookingResult r1 = bookingService.createBooking(s1.getId(), new BookingRequest(slotId));
            Long bookingId = r1.getBooking().getBookingId();

            bookingService.createBooking(s2.getId(), new BookingRequest(slotId));

            // Cancel student1's booking
            bookingService.cancelBooking(s1.getId(), Role.STUDENT, bookingId);

            // Student 2 should now have a confirmed booking
            List<Booking> s2Bookings = bookingRepository.findByStudentId(s2.getId());
            boolean hasBooked = s2Bookings.stream().anyMatch(b -> b.getStatus() == BookingStatus.BOOKED);
            assertTrue(hasBooked, "Student 2 should be promoted from waitlist to confirmed booking");
        }
    }
}
