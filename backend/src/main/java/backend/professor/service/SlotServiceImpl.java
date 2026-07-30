package backend.professor.service;

import backend.booking.model.Booking;
import backend.booking.repository.BookingRepository;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.common.enums.SlotStatus;
import backend.common.enums.WaitlistStatus;
import backend.common.repository.UserRepository;
import backend.exception.ResourceNotFoundException;
import backend.professor.dto.SlotBookingsResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;
import backend.professor.exception.SlotNotOwnedException;
import backend.professor.exception.SlotOverlapException;
import backend.professor.model.Slot;
import backend.professor.repository.SlotRepository;
import backend.student.repository.StudentProfileRepository;
import backend.waitlist.model.WaitlistEntry;
import backend.waitlist.repository.WaitlistEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SlotServiceImpl implements SlotService {

    private static final Set<SlotStatus> BOOKABLE_STATUSES = Set.of(SlotStatus.OPEN, SlotStatus.FULL);

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    // Section 16 - Integration Rules: SlotService.getBookingsForSlot() is a
    // documented exception that reads booking/'s and waitlist/'s
    // repositories directly, read-only, to assemble the roster response -
    // the one explicitly carved-out cross-module read in the whole spec
    // ("both explicitly exposed as read paths").
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;

    // Section 8.3 grants booking/ read access to StudentProfileRepository
    // for rollNo display on SlotBookingsResponse; since that response is
    // actually assembled here in professor/ (its owning module per
    // Section 10), professor/ needs the same read-only access to satisfy
    // the exact JSON contract in main spec Section 3.2 ("rollNo" field).
    private final StudentProfileRepository studentProfileRepository;

    @Override
    @Transactional
    public SlotResponse createSlot(Long professorId, SlotRequest request) {
        validateTimeOrder(request);

        if (slotRepository.existsByProfessorIdAndSlotDateAndStartTime(
                professorId, request.getSlotDate(), request.getStartTime())) {
            throw new SlotOverlapException(
                    "You already have a slot starting at " + request.getStartTime()
                            + " on " + request.getSlotDate());
        }

        User professor = userRepository.findById(professorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor with id " + professorId + " not found"));

        Slot slot = new Slot();
        slot.setProfessor(professor);
        slot.setSlotDate(request.getSlotDate());
        slot.setStartTime(request.getStartTime());
        slot.setEndTime(request.getEndTime());
        slot.setCapacity(request.getCapacity());
        slot.setBookedCount(0);
        slot.setStatus(SlotStatus.OPEN);

        Slot saved = slotRepository.save(slot);
        return toSlotResponse(saved);
    }

    @Override
    @Transactional
    public SlotResponse updateSlot(Long professorId, Long slotId, SlotRequest request) {
        Slot slot = loadOwnedSlot(professorId, slotId);

        // Main spec Section 3.2, PUT /api/professors/slots/{slotId}: if the
        // slot has active bookings, only capacity may be increased -
        // date/time must not change, so a student's confirmed reservation
        // is never silently invalidated.
        boolean hasActiveBookings = slot.getBookedCount() != null && slot.getBookedCount() > 0;

        if (hasActiveBookings) {
            boolean dateTimeChanged = !slot.getSlotDate().equals(request.getSlotDate())
                    || !slot.getStartTime().equals(request.getStartTime())
                    || !slot.getEndTime().equals(request.getEndTime());
            if (dateTimeChanged) {
                throw new SlotOverlapException(
                        "Slot has active bookings - only capacity may be changed, not date/time");
            }
            if (request.getCapacity() < slot.getCapacity()) {
                throw new SlotOverlapException(
                        "Capacity cannot be decreased below the current booked count on a slot with active bookings");
            }
        } else {
            validateTimeOrder(request);
            boolean movingToNewStartTime = !slot.getStartTime().equals(request.getStartTime())
                    || !slot.getSlotDate().equals(request.getSlotDate());
            if (movingToNewStartTime && slotRepository.existsByProfessorIdAndSlotDateAndStartTime(
                    professorId, request.getSlotDate(), request.getStartTime())) {
                throw new SlotOverlapException(
                        "You already have a slot starting at " + request.getStartTime()
                                + " on " + request.getSlotDate());
            }
            slot.setSlotDate(request.getSlotDate());
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
        }

        slot.setCapacity(request.getCapacity());

        // Re-derive OPEN/FULL from the (possibly unchanged) booked count vs
        // the new capacity - mirrors the flip rule used by
        // increment/decrementBookedCount() below. Never touches a
        // CANCELLED/COMPLETED slot's status.
        if (slot.getStatus() != SlotStatus.CANCELLED && slot.getStatus() != SlotStatus.COMPLETED) {
            slot.setStatus(slot.getBookedCount() >= slot.getCapacity() ? SlotStatus.FULL : SlotStatus.OPEN);
        }

        Slot saved = slotRepository.save(slot);
        return toSlotResponse(saved);
    }

    @Override
    @Transactional
    public void cancelSlot(Long professorId, Long slotId) {
        Slot slot = loadOwnedSlot(professorId, slotId);
        // Section 2.1 - Design Decisions: no physical DELETE in the happy
        // path - soft-cancel via status only, so booking history is
        // preserved (a stated requirement).
        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
        // NOTE: notifying affected students on cancellation (main spec
        // Section 3.2, "If active bookings exist, all affected students
        // should be notified") is triggered from booking/, the owning
        // module of the affected Booking rows - professor/ only owns the
        // Slot row itself and does not call NotificationService directly.
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getSlotsForProfessor(Long professorId) {
        return slotRepository.findByProfessorIdAndSlotDateGreaterThanEqual(professorId, LocalDate.now())
                .stream()
                .sorted(Comparator.comparing(Slot::getSlotDate).thenComparing(Slot::getStartTime))
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<SlotResponse> getBookableSlotsForProfessor(Long professorId) {
        return slotRepository.findByProfessorIdAndSlotDateGreaterThanEqual(professorId, LocalDate.now())
                .stream()
                .filter(s -> BOOKABLE_STATUSES.contains(s.getStatus()))
                .sorted(Comparator.comparing(Slot::getSlotDate).thenComparing(Slot::getStartTime))
                .map(this::toSlotResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SlotBookingsResponse getBookingsForSlot(Long professorId, Long slotId) {
        Slot slot = loadOwnedSlot(professorId, slotId);
        return buildRoster(slot);
    }

    @Override
    @Transactional(readOnly = true)
    public SlotBookingsResponse getBookingsForSlotAsAdmin(Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        return buildRoster(slot);
    }

    @Override
    @Transactional
    public void incrementBookedCount(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        slot.setBookedCount(slot.getBookedCount() + 1);
        if (slot.getBookedCount() >= slot.getCapacity()) {
            slot.setStatus(SlotStatus.FULL);
        }
        slotRepository.save(slot);
    }

    @Override
    @Transactional
    public void decrementBookedCount(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        slot.setBookedCount(Math.max(0, slot.getBookedCount() - 1));
        if (slot.getStatus() == SlotStatus.FULL && slot.getBookedCount() < slot.getCapacity()) {
            slot.setStatus(SlotStatus.OPEN);
        }
        slotRepository.save(slot);
    }

    // ---- helpers -------------------------------------------------------

    private Slot loadOwnedSlot(Long professorId, Long slotId) {
        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        if (!slot.getProfessor().getId().equals(professorId)) {
            throw new SlotNotOwnedException("You do not own this slot");
        }
        return slot;
    }

    private void validateTimeOrder(SlotRequest request) {
        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("endTime must be after startTime");
        }
    }

    private SlotResponse toSlotResponse(Slot slot) {
        return SlotResponse.builder()
                .slotId(slot.getId())
                .professorId(slot.getProfessor().getId())
                .slotDate(slot.getSlotDate())
                .startTime(slot.getStartTime())
                .endTime(slot.getEndTime())
                .capacity(slot.getCapacity())
                .bookedCount(slot.getBookedCount())
                .status(slot.getStatus())
                .build();
    }

    private SlotBookingsResponse buildRoster(Slot slot) {
        List<Booking> confirmedBookings =
                bookingRepository.findBySlotIdAndStatus(slot.getId(), BookingStatus.BOOKED);
        List<WaitlistEntry> waitingEntries =
                waitlistEntryRepository.findBySlotIdAndStatus(slot.getId(), WaitlistStatus.WAITING);

        List<SlotBookingsResponse.BookingRosterEntry> bookingEntries = confirmedBookings.stream()
                .map(b -> SlotBookingsResponse.BookingRosterEntry.builder()
                        .bookingId(b.getId())
                        .studentId(b.getStudent().getId())
                        .studentName(b.getStudent().getFullName())
                        .rollNo(studentProfileRepository.findByUserId(b.getStudent().getId())
                                .map(sp -> sp.getRollNo())
                                .orElse(null))
                        .status(b.getStatus())
                        .bookedAt(b.getBookedAt())
                        .build())
                .collect(Collectors.toList());

        List<SlotBookingsResponse.WaitlistRosterEntry> waitlistEntries = waitingEntries.stream()
                .sorted(Comparator.comparing(WaitlistEntry::getPosition))
                .map(w -> SlotBookingsResponse.WaitlistRosterEntry.builder()
                        .waitlistId(w.getId())
                        .studentId(w.getStudent().getId())
                        .studentName(w.getStudent().getFullName())
                        .position(w.getPosition())
                        .build())
                .collect(Collectors.toList());

        return SlotBookingsResponse.builder()
                .slotId(slot.getId())
                .bookings(bookingEntries)
                .waitlist(waitlistEntries)
                .build();
    }
}