package backend.waitlist.service;

import backend.booking.entity.Booking;
import backend.booking.repository.BookingRepository;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.common.enums.SlotStatus;
import backend.common.enums.WaitlistStatus;
import backend.common.repository.UserRepository;
import backend.professor.entity.Slot;
import backend.professor.repository.SlotRepository;
import backend.student.entity.StudentProfile;
import backend.student.repository.StudentProfileRepository;
import backend.waitlist.dto.WaitlistEntryResponse;
import backend.waitlist.entity.WaitlistEntry;
import backend.waitlist.repository.WaitlistEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WaitlistServiceImpl implements WaitlistService {

    private final WaitlistEntryRepository waitlistRepository;
    private final UserRepository userRepository;
    private final SlotRepository slotRepository;
    private final StudentProfileRepository studentRepository;
    private final BookingRepository bookingRepository;

    public WaitlistServiceImpl(
            WaitlistEntryRepository waitlistRepository,
            UserRepository userRepository,
            SlotRepository slotRepository,
            StudentProfileRepository studentRepository,
            BookingRepository bookingRepository) {

        this.waitlistRepository = waitlistRepository;
        this.userRepository = userRepository;
        this.slotRepository = slotRepository;
        this.studentRepository = studentRepository;
        this.bookingRepository = bookingRepository;
    }

    @Override
    @Transactional
    public WaitlistEntryResponse joinWaitlist(Long studentId, Long slotId) {

        StudentProfile studentProfile = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        User user = studentProfile.getUser();

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        if (waitlistRepository.existsByStudentIdAndSlotId(
                user.getId(), slotId)) {
            throw new RuntimeException("Student is already in the waitlist");
        }

        List<WaitlistEntry> waitingEntries =
                waitlistRepository.findBySlotIdAndStatusOrderByPositionAsc(
                        slotId,
                        WaitlistStatus.WAITING
                );

        int nextPosition = waitingEntries.size() + 1;

        WaitlistEntry entry = WaitlistEntry.builder()
                .student(user)
                .slot(slot)
                .position(nextPosition)
                .status(WaitlistStatus.WAITING)
                .createdAt(LocalDateTime.now())
                .build();

        WaitlistEntry savedEntry = waitlistRepository.save(entry);

        return WaitlistEntryResponse.builder()
                .waitlistId(savedEntry.getId())
                .slotId(slot.getId())
                .studentId(user.getId())
                .status(savedEntry.getStatus())
                .position(savedEntry.getPosition())
                .createdAt(savedEntry.getCreatedAt())
                .build();
    }

    @Override
    @Transactional
    public void promoteNext(Long slotId) {

        WaitlistEntry entry =
                waitlistRepository
                        .findFirstBySlotIdAndStatusOrderByPositionAsc(
                                slotId,
                                WaitlistStatus.WAITING
                        )
                        .orElse(null);

        if (entry == null) {
            return;
        }

        Slot slot = slotRepository.findById(slotId)
                .orElseThrow(() -> new RuntimeException("Slot not found"));

        StudentProfile student =
                studentRepository.findByUserId(entry.getStudent().getId())
                        .orElseThrow(() ->
                                new RuntimeException("Student profile not found"));

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
        } else {
            slot.setStatus(SlotStatus.AVAILABLE);
        }

        slotRepository.save(slot);

        entry.setStatus(WaitlistStatus.PROMOTED);
        waitlistRepository.save(entry);

        resequenceWaitingEntries(slotId);
    }

    @Override
    @Transactional
    public void leaveWaitlist(Long studentId, Long waitlistId) {

        WaitlistEntry entry = waitlistRepository.findById(waitlistId)
                .orElseThrow(() ->
                        new RuntimeException("Waitlist entry not found"));

        StudentProfile student = studentRepository.findById(studentId)
                .orElseThrow(() -> new RuntimeException("Student not found"));

        if (!entry.getStudent().getId().equals(student.getUser().getId())) {
            throw new RuntimeException(
                    "You can only leave your own waitlist entry"
            );
        }

        entry.setStatus(WaitlistStatus.CANCELLED);
        waitlistRepository.save(entry);

        resequenceWaitingEntries(entry.getSlot().getId());
    }

    private void resequenceWaitingEntries(Long slotId) {

        List<WaitlistEntry> waitingEntries =
                waitlistRepository.findBySlotIdAndStatusOrderByPositionAsc(
                        slotId,
                        WaitlistStatus.WAITING
                );

        int position = 1;

        for (WaitlistEntry entry : waitingEntries) {
            entry.setPosition(position);
            position++;
        }

        waitlistRepository.saveAll(waitingEntries);
    }
}