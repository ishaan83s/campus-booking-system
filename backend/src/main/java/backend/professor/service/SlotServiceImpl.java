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
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import backend.waitlist.service.WaitlistService;
import org.springframework.context.annotation.Lazy;
import java.util.stream.Collectors;

@Service
public class SlotServiceImpl implements SlotService {

    private static final Set<SlotStatus> BOOKABLE_STATUSES = Set.of(SlotStatus.OPEN, SlotStatus.FULL);

    private final SlotRepository slotRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final WaitlistEntryRepository waitlistEntryRepository;
    private final StudentProfileRepository studentProfileRepository;
    private final WaitlistService waitlistService;

    public SlotServiceImpl(
            SlotRepository slotRepository,
            UserRepository userRepository,
            BookingRepository bookingRepository,
            WaitlistEntryRepository waitlistEntryRepository,
            StudentProfileRepository studentProfileRepository,
            @Lazy WaitlistService waitlistService) {
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
        this.waitlistEntryRepository = waitlistEntryRepository;
        this.studentProfileRepository = studentProfileRepository;
        this.waitlistService = waitlistService;
    }

    @Override
    @Transactional
    public SlotResponse createSlot(Long professorId, SlotRequest request) {
        validateTimeOrder(request);

        if (slotRepository.existsOverlappingSlot(
                professorId, request.getSlotDate(),
                request.getStartTime(), request.getEndTime(), null)) {
            throw new SlotOverlapException(
                    "This slot overlaps with an existing slot on " + request.getSlotDate());
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
            if (request.getCapacity() < slot.getBookedCount()) {
                throw new SlotOverlapException(
                        "Capacity cannot be decreased below the current booked count (" + slot.getBookedCount() + ")");
            }
        } else {
            validateTimeOrder(request);
            boolean dateTimeChanged = !slot.getSlotDate().equals(request.getSlotDate())
                    || !slot.getStartTime().equals(request.getStartTime())
                    || !slot.getEndTime().equals(request.getEndTime());
            if (dateTimeChanged && slotRepository.existsOverlappingSlot(
                    professorId, request.getSlotDate(),
                    request.getStartTime(), request.getEndTime(), slotId)) {
                throw new SlotOverlapException(
                        "This slot overlaps with an existing slot on " + request.getSlotDate());
            }
            slot.setSlotDate(request.getSlotDate());
            slot.setStartTime(request.getStartTime());
            slot.setEndTime(request.getEndTime());
        }

        int oldCapacity = slot.getCapacity();
        slot.setCapacity(request.getCapacity());

        // Re-derive OPEN/FULL from the (possibly unchanged) booked count vs
        // the new capacity - mirrors the flip rule used by
        // increment/decrementBookedCount() below. Never touches a
        // CANCELLED/COMPLETED slot's status.
        if (slot.getStatus() != SlotStatus.CANCELLED && slot.getStatus() != SlotStatus.COMPLETED) {
            slot.setStatus(slot.getBookedCount() >= slot.getCapacity() ? SlotStatus.FULL : SlotStatus.OPEN);
        }

        Slot saved = slotRepository.save(slot);

        // If capacity was expanded, automatically promote queued waitlist students
        if (request.getCapacity() > oldCapacity && slot.getStatus() != SlotStatus.CANCELLED && slot.getStatus() != SlotStatus.COMPLETED) {
            waitlistService.promoteNext(slotId);
            saved = slotRepository.findById(slotId).orElse(saved);
        }

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

        // Cascade: cancel all active bookings for this slot so students
        // don't see a BOOKED record against a CANCELLED slot.
        List<Booking> activeBookings = bookingRepository.findBySlotIdAndStatus(slotId, BookingStatus.BOOKED);
        LocalDateTime now = LocalDateTime.now();
        for (Booking booking : activeBookings) {
            booking.setStatus(BookingStatus.CANCELLED);
            booking.setCancelledAt(now);
        }

        // Cascade: cancel all waiting waitlist entries for this slot.
        List<WaitlistEntry> waitingEntries = waitlistEntryRepository.findBySlotIdAndStatus(slotId, WaitlistStatus.WAITING);
        for (WaitlistEntry entry : waitingEntries) {
            entry.setStatus(WaitlistStatus.CANCELLED);
        }
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