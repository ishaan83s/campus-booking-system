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
import backend.notification.dto.NotificationPayload;
import backend.notification.service.NotificationService;
import backend.professor.model.Slot;
import backend.professor.repository.SlotRepository;
import backend.professor.service.SlotService;
import backend.waitlist.dto.WaitlistEntryResponse;
import backend.waitlist.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Section 8.4 - BookingServiceImpl. Owns all writes to `bookings` (Section 9).
 *
 * Cross-module calls follow Section 16 (Integration Rules) exactly:
 * - Slot is only ever locked/read via SlotRepository.findByIdForUpdate()
 *   (the one repository method booking/ is permitted to call directly,
 *   per Section 8.2).
 * - Slot.bookedCount / Slot.status are only ever changed by calling
 *   SlotService.incrementBookedCount() / .decrementBookedCount() - the
 *   two methods Section 8.2 instructs the team to add to SlotService
 *   before this class is written (kickoff-meeting decision, option a).
 * // TODO: awaiting professor/ implementation of
 * //       SlotService.incrementBookedCount(Long) / decrementBookedCount(Long)
 *
 * - WaitlistService.joinWaitlist()/.promoteNext() are called ONLY from
 *   here, per Section 8.5's "nobody else may" rule (same-owner access,
 *   Dev D owns both booking/ and waitlist/, Section 11).
 * // TODO: awaiting waitlist/ package (next in build order)
 *
 * - NotificationService is called for every state change, never a raw
 *   log statement (Section 8.6).
 * // TODO: awaiting notification/ package (next in build order)
 */
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final SlotRepository slotRepository;
    private final SlotService slotService;
    private final WaitlistService waitlistService;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public BookingResult createBooking(Long studentId, BookingRequest request) {
        // Step 1: load slot with a pessimistic write lock (SELECT ... FOR
        // UPDATE) to prevent a race between two students booking the last
        // seat simultaneously (Section 3.3, step 1).
        Slot slot = slotRepository.findByIdForUpdate(request.getSlotId())
                .orElseThrow(() -> new ResourceNotFoundException("Slot with id " + request.getSlotId() + " not found"));

        // Step 2: reject cancelled/completed slots or past dates -> 409.
        if (slot.getStatus() == SlotStatus.CANCELLED || slot.getStatus() == SlotStatus.COMPLETED) {
            throw new ConflictException("This slot is no longer accepting bookings");
        }
        if (slot.getSlotDate().isBefore(LocalDate.now())) {
            throw new ConflictException("This slot's date has already passed");
        }

        // Step 3: reject a duplicate active booking for the same slot -> 409.
        if (bookingRepository.existsByStudentIdAndSlotIdAndStatus(studentId, slot.getId(), BookingStatus.BOOKED)) {
            throw new ConflictException("You already have an active booking for this slot");
        }

        // Step 4: room available -> confirm the booking.
        if (slot.getBookedCount() < slot.getCapacity()) {
            User student = userRepository.getReferenceById(studentId);

            Booking booking = Booking.builder()
                    .student(student)
                    .slot(slot)
                    .status(BookingStatus.BOOKED)
                    .bookedAt(LocalDateTime.now())
                    .build();
            booking = bookingRepository.save(booking);

            // Route the count/status change back through the owning
            // module's service - never write Slot fields directly (Section 8.2).
            slotService.incrementBookedCount(slot.getId());

            BookingResponse response = toBookingResponse(booking, slot);

            notificationService.sendBookingConfirmed(NotificationPayload.builder()
                    .recipientUserId(studentId)
                    .type("BOOKING_CONFIRMED")
                    .slotId(slot.getId())
                    .message("Your booking for " + slot.getSlotDate() + " " + slot.getStartTime() + " is confirmed.")
                    .build());

            return BookingResult.booked(response);
        }

        // Step 5: slot is full -> hand off to WaitlistService instead of
        // creating a bookings row (Section 2.1 design decision).
        WaitlistEntryResponse waitlistEntry = waitlistService.joinWaitlist(studentId, slot.getId());
        return BookingResult.waitlisted(waitlistEntry);
    }

    @Override
    @Transactional
    public void cancelBooking(Long requesterId, Role requesterRole, Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with id " + bookingId + " not found"));

        boolean isOwner = Objects.equals(booking.getStudent().getId(), requesterId);
        boolean isAdmin = requesterRole == Role.ADMIN;
        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You can only cancel your own bookings");
        }

        if (booking.getStatus() != BookingStatus.BOOKED) {
            throw new ConflictException("Booking is no longer active");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        Long slotId = booking.getSlot().getId();

        // Decrement first (flips slot back to OPEN if it was FULL), then
        // attempt to promote the next waiting student into the freed seat
        // (Section 3.3 - this is the waitlist-promotion trigger).
        slotService.decrementBookedCount(slotId);
        waitlistService.promoteNext(slotId);

        notificationService.sendBookingCancelled(NotificationPayload.builder()
                .recipientUserId(booking.getStudent().getId())
                .type("BOOKING_CANCELLED")
                .slotId(slotId)
                .message("Your booking for slot " + slotId + " has been cancelled.")
                .build());
    }

    @Override
    @Transactional(readOnly = true)
    public BookingHistoryResponse getBookingHistory(Long studentId, BookingStatus statusFilter) {
        List<Booking> bookings = bookingRepository.findByStudentId(studentId);

        List<BookingResponse> bookingResponses = bookings.stream()
                .filter(b -> statusFilter == null || b.getStatus() == statusFilter)
                .map(b -> toBookingResponse(b, b.getSlot()))
                .toList();

        // waitlist/ owns WaitlistEntry (Section 9); reading it here is
        // same-owner access (Dev D owns both booking/ and waitlist/, per
        // Section 11), routed through WaitlistService rather than a raw
        // repository call from a different service, keeping the module
        // boundary consistent even within one developer's ownership.
        List<WaitlistEntryResponse> waitlistEntries = waitlistService.getActiveEntriesForStudent(studentId);

        return BookingHistoryResponse.builder()
                .bookings(bookingResponses)
                .waitlistEntries(waitlistEntries)
                .build();
    }

    private BookingResponse toBookingResponse(Booking booking, Slot slot) {
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
