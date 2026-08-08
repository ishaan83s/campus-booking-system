package backend.waitlist.service;

import backend.booking.model.Booking;
import backend.booking.repository.BookingRepository;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.common.enums.WaitlistStatus;
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
import backend.waitlist.model.WaitlistEntry;
import backend.waitlist.repository.WaitlistEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WaitlistServiceImpl implements WaitlistService {
    private final WaitlistEntryRepository waitlistRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final SlotService slotService;
    private final NotificationService notificationService;

    @Override @Transactional
    public WaitlistEntryResponse joinWaitlist(Long studentId, Long slotId) {
        if (waitlistRepository.existsByStudentIdAndSlotIdAndStatus(studentId, slotId, WaitlistStatus.WAITING)) throw new ConflictException("You are already on this slot's waitlist");
        Slot slot = slotRepository.findByIdForUpdate(slotId).orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        if (slot.getBookedCount() < slot.getCapacity()) throw new ConflictException("This slot has availability; create a booking instead");
        User student = userRepository.getReferenceById(studentId);
        int position = waitlistRepository.findBySlotIdAndStatus(slotId, WaitlistStatus.WAITING).stream().map(WaitlistEntry::getPosition).max(Integer::compareTo).orElse(0) + 1;
        WaitlistEntry saved = waitlistRepository.save(WaitlistEntry.builder().student(student).slot(slot).position(position).status(WaitlistStatus.WAITING).build());
        return toResponse(saved);
    }

    @Override @Transactional
    public void promoteNext(Long slotId) {
        Slot slot = slotRepository.findByIdForUpdate(slotId).orElseThrow(() -> new ResourceNotFoundException("Slot with id " + slotId + " not found"));
        if (slot.getBookedCount() >= slot.getCapacity()) return;
        waitlistRepository.findFirstBySlotIdAndStatusOrderByPositionAsc(slotId, WaitlistStatus.WAITING).ifPresent(entry -> {
            bookingRepository.save(Booking.builder().student(entry.getStudent()).slot(slot).status(BookingStatus.BOOKED).bookedAt(LocalDateTime.now()).build());
            slotService.incrementBookedCount(slotId);
            entry.setStatus(WaitlistStatus.PROMOTED); entry.setPromotedAt(LocalDateTime.now());
            resequence(slotId);
            notificationService.sendWaitlistPromoted(NotificationPayload.builder().recipientUserId(entry.getStudent().getId()).slotId(slotId).type("WAITLIST_PROMOTED").message("A seat is now confirmed for your waitlisted slot.").build());
        });
    }

    @Override @Transactional
    public void leaveWaitlist(Long studentId, Long waitlistId) {
        WaitlistEntry entry = waitlistRepository.findById(waitlistId).orElseThrow(() -> new ResourceNotFoundException("Waitlist entry with id " + waitlistId + " not found"));
        if (!entry.getStudent().getId().equals(studentId)) throw new ForbiddenOperationException("You can only leave your own waitlist entry");
        if (entry.getStatus() != WaitlistStatus.WAITING) throw new ConflictException("This waitlist entry is no longer active");
        entry.setStatus(WaitlistStatus.CANCELLED); resequence(entry.getSlot().getId());
    }
    @Override @Transactional(readOnly = true)
    public List<WaitlistEntryResponse> getActiveEntriesForStudent(Long studentId) { return waitlistRepository.findByStudentIdAndStatus(studentId, WaitlistStatus.WAITING).stream().sorted(Comparator.comparing(e -> e.getSlot().getSlotDate())).map(this::toResponse).toList(); }
    private void resequence(Long slotId) { List<WaitlistEntry> entries = waitlistRepository.findBySlotIdAndStatus(slotId, WaitlistStatus.WAITING); entries.sort(Comparator.comparing(WaitlistEntry::getPosition).thenComparing(WaitlistEntry::getCreatedAt)); for (int i = 0; i < entries.size(); i++) entries.get(i).setPosition(i + 1); }
    private WaitlistEntryResponse toResponse(WaitlistEntry entry) { return WaitlistEntryResponse.builder().waitlistId(entry.getId()).slotId(entry.getSlot().getId()).studentId(entry.getStudent().getId()).status(entry.getStatus()).position(entry.getPosition()).createdAt(entry.getCreatedAt()).build(); }
}
