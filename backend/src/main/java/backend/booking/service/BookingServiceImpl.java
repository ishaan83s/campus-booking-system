package backend.booking.service;

import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResponse;
import backend.booking.dto.BookingResult;
import backend.booking.entity.Booking;
import backend.booking.repository.BookingRepository;
import backend.common.enums.BookingStatus;
import backend.common.enums.SlotStatus;
import backend.professor.entity.Slot;
import backend.professor.repository.SlotRepository;
import backend.student.entity.StudentProfile;
import backend.student.repository.StudentProfileRepository;
import backend.waitlist.dto.WaitlistEntryResponse;
import backend.waitlist.service.WaitlistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final StudentProfileRepository studentRepository;
    private final SlotRepository slotRepository;
    private final WaitlistService waitlistService;

    public BookingServiceImpl(
            BookingRepository bookingRepository,
            StudentProfileRepository studentRepository,
            SlotRepository slotRepository,
            WaitlistService waitlistService) {

        this.bookingRepository = bookingRepository;
        this.studentRepository = studentRepository;
        this.slotRepository = slotRepository;
        this.waitlistService = waitlistService;
    }

    @Override
    @Transactional
    public BookingResult createBooking(BookingRequest request) {

        StudentProfile student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new RuntimeException("Student not found"));

        Slot slot = slotRepository.findById(request.getSlotId())
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        // If the slot is already full, add the student to the waitlist.
        if (slot.getBookedCount() >= slot.getCapacity()) {

            WaitlistEntryResponse waitlistResponse =
                    waitlistService.joinWaitlist(
                            student.getId(),
                            slot.getId()
                    );

            return BookingResult.builder()
                    .type("WAITLISTED")
                    .booking(null)
                    .waitlistEntry(waitlistResponse)
                    .build();
        }

        // Otherwise create a confirmed booking.
        slot.setBookedCount(slot.getBookedCount() + 1);

        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setStatus(SlotStatus.FULL);
        } else {
            slot.setStatus(SlotStatus.AVAILABLE);
        }

        slotRepository.save(slot);

        Booking booking = Booking.builder()
                .student(student)
                .slot(slot)
                .status(BookingStatus.BOOKED)
                .bookedAt(LocalDateTime.now())
                .build();

        Booking savedBooking = bookingRepository.save(booking);

        BookingResponse bookingResponse = BookingResponse.builder()
                .bookingId(savedBooking.getId())
                .studentId(student.getId())
                .slotId(slot.getId())
                .status(savedBooking.getStatus())
                .build();

        return BookingResult.builder()
                .type("BOOKED")
                .booking(bookingResponse)
                .waitlistEntry(null)
                .build();
    }

    @Override
    public List<BookingResponse> getStudentBookings(Long studentId) {

        return bookingRepository.findByStudentId(studentId)
                .stream()
                .map(booking -> BookingResponse.builder()
                        .bookingId(booking.getId())
                        .studentId(booking.getStudent().getId())
                        .slotId(booking.getSlot().getId())
                        .status(booking.getStatus())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void cancelBooking(Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Booking not found"));

        // Prevent cancelling the same booking twice.
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new RuntimeException("Booking is already cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);

        Slot slot = booking.getSlot();

        if (slot.getBookedCount() > 0) {
            slot.setBookedCount(slot.getBookedCount() - 1);
        }

        slot.setStatus(SlotStatus.AVAILABLE);

        slotRepository.save(slot);
        bookingRepository.save(booking);

        // If somebody is waiting, give the newly available seat
        // to the first student in the waitlist.
        waitlistService.promoteNext(slot.getId());
    }
}