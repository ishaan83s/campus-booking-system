package backend.professor.service;

import backend.professor.dto.SlotBookingsResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;

import java.util.List;

/**
 * Section 8.2 - Module Interface Specification.
 *
 * incrementBookedCount()/decrementBookedCount() are the two methods the
 * spec explicitly calls out as needing to exist before Dev D starts
 * BookingServiceImpl (Section 8.2, recommended "option (a) - keeps the
 * only-owner-writes rule with zero exceptions"). booking/ must call ONLY
 * these two methods to mutate a Slot it is holding under a pessimistic
 * lock via SlotRepository.findByIdForUpdate() - it must never call
 * slotRepository.save() itself (Section 16 - Integration Rules).
 */
public interface SlotService {

    SlotResponse createSlot(Long professorId, SlotRequest request);

    SlotResponse updateSlot(Long professorId, Long slotId, SlotRequest request);

    void cancelSlot(Long professorId, Long slotId);

    List<SlotResponse> getSlotsForProfessor(Long professorId);

    /**
     * Public/Student-facing view of a professor's slots
     * (main spec Section 3.2, GET /api/professors/{professorId}/slots):
     * only OPEN/FULL slots dated today or later are returned;
     * CANCELLED/COMPLETED and past slots are hidden. Added alongside
     * getSlotsForProfessor() (the professor's own full-history view,
     * all statuses, backing the Role Capability Matrix's "View own
     * schedule" row, Section 1.2) because the two endpoints have
     * genuinely different visibility rules - flagged here explicitly per
     * the AI Development Rules (Section 15) rather than silently reusing
     * one method for both.
     */
    List<SlotResponse> getBookableSlotsForProfessor(Long professorId);

    SlotBookingsResponse getBookingsForSlot(Long professorId, Long slotId);

    /**
     * ADMIN-accessible variant of getBookingsForSlot(), mirroring the
     * BookingService.cancelBooking(requesterId, requesterRole, bookingId)
     * pattern already used elsewhere in the bible (Section 8.4) for
     * endpoints with dual PROFESSOR-owner / ADMIN access (main spec
     * Section 3.2: "Role required: PROFESSOR (must own the slot) or
     * ADMIN"). Called only by ProfessorController when the requester's
     * role is ADMIN, skipping the ownership check.
     */
    SlotBookingsResponse getBookingsForSlotAsAdmin(Long slotId);

    void incrementBookedCount(Long slotId);

    void decrementBookedCount(Long slotId);
}