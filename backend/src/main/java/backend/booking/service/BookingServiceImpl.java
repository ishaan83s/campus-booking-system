package backend.booking.service;

import backend.booking.dto.BookingHistoryResponse;
import backend.booking.dto.BookingRequest;
import backend.booking.dto.BookingResponse;
import backend.booking.dto.BookingResult;
import backend.booking.model.Booking;
import backend.booking.repository.BookingRepository;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;
import backend.common.enums.SlotStatus;
import backend.common.repository.UserRepository;
import backend.exception.ConflictException;
import backend.exception.ForbiddenOperationException;
import backend.exception.ResourceNotFoundException;
import backend.professor.model.Slot;
import backend.professor.repository.SlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    @Override
    public BookingResult createBooking(Long studentId, BookingRequest request) {

        User student = userRepository.findById(studentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Student with id " + studentId + " not found"
                        ));

        Slot slot = slotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Slot with id " + request.getSlotId() + " not found"
                        ));

        if (slot.getStatus() == SlotStatus.CANCELLED
                || slot.getStatus() == SlotStatus.COMPLETED) {

            throw new ConflictException(
                    "Booking is not allowed for this slot."
            );
        }

        if (slot.getSlotDate().isBefore(LocalDate.now())) {

            throw new ConflictException(
                    "Cannot book a past slot."
            );
        }

        boolean alreadyBooked =
                bookingRepository.existsByStudentIdAndSlotIdAndStatus(
                        studentId,
                        slot.getId(),
                        BookingStatus.BOOKED
                );

        if (alreadyBooked) {
            throw new ConflictException(
                    "You already have an active booking for this slot."
            );
        }

        if (slot.getBookedCount() >= slot.getCapacity()) {

            /*
             * Phase 2:
             * return BookingResult.waitlisted(
             *      waitlistService.joinWaitlist(studentId, slot.getId())
             * );
             */

            throw new ConflictException(
                    "Slot is already full."
            );
        }

        Booking booking = Booking.builder()
                .student(student)
                .slot(slot)
                .status(BookingStatus.BOOKED)
                .bookedAt(LocalDateTime.now())
                .build();

        bookingRepository.save(booking);

        slot.setBookedCount(slot.getBookedCount() + 1);

        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setStatus(SlotStatus.FULL);
        }

        slotRepository.save(slot);

        BookingResponse response = BookingResponse.builder()
                .bookingId(booking.getId())
                .slotId(slot.getId())
                .studentId(student.getId())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .professorName(slot.getProfessor().getFullName())
                .build();

        return BookingResult.booked(response);
    }
    @Override
    public void cancelBooking(Long requesterId,
                              Role requesterRole,
                              Long bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Booking with id " + bookingId + " not found"
                        ));

        if (requesterRole != Role.ADMIN
                && !booking.getStudent().getId().equals(requesterId)) {

            throw new ForbiddenOperationException(
                    "You can only cancel your own bookings."
            );
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ConflictException(
                    "Booking is already cancelled."
            );
        }

        Slot slot = slotRepository.findByIdForUpdate(
                        booking.getSlot().getId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Slot with id "
                                        + booking.getSlot().getId()
                                        + " not found"
                        ));

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        bookingRepository.save(booking);

        if (slot.getBookedCount() > 0) {
            slot.setBookedCount(slot.getBookedCount() - 1);
        }

        if (slot.getStatus() == SlotStatus.FULL) {
            slot.setStatus(SlotStatus.OPEN);
        }

        slotRepository.save(slot);

        /*
         * ===============================
         * Phase 2
         * ===============================
         *
         * waitlistService.promoteNext(slot.getId());
         *
         * notificationService.sendBookingCancelled(...);
         *
         */
    }
    @Override
    public BookingHistoryResponse getBookingHistory(Long studentId,
                                                    BookingStatus statusFilter) {

        List<Booking> bookings;

        if (statusFilter != null) {
            bookings = bookingRepository.findByStudentIdAndStatus(
                    studentId,
                    statusFilter
            );
        } else {
            bookings = bookingRepository.findByStudentId(studentId);
        }

        List<BookingResponse> responses = bookings.stream()
                .map(this::mapToBookingResponse)
                .collect(Collectors.toList());

        return BookingHistoryResponse.builder()
                .bookings(responses)

                /*
                 * Phase 2:
                 *
                 * Populate waitlist entries once the
                 * Waitlist module is implemented.
                 */
                .waitlistEntries(List.of())
                .build();
    }

    /**
     * Maps Booking entity to BookingResponse DTO.
     */
    private BookingResponse mapToBookingResponse(Booking booking) {

        Slot slot = booking.getSlot();

        return BookingResponse.builder()
                .bookingId(booking.getId())
                .slotId(slot.getId())
                .studentId(booking.getStudent().getId())
                .status(booking.getStatus())
                .bookedAt(booking.getBookedAt())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .professorName(slot.getProfessor().getFullName())
                .build();
    }

}