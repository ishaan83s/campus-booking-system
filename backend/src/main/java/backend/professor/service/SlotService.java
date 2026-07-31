package backend.professor.service;

import backend.professor.dto.SlotBookingsResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;

import java.util.List;

public interface SlotService {

    /**
     * Creates a new office-hour slot for the authenticated professor.
     */
    SlotResponse createSlot(Long professorId, SlotRequest request);

    /**
     * Updates an existing slot owned by the authenticated professor.
     */
    SlotResponse updateSlot(Long professorId,
                            Long slotId,
                            SlotRequest request);

    /**
     * Soft deletes (CANCELLED) a slot.
     */
    void cancelSlot(Long professorId,
                    Long slotId);

    /**
     * Returns all slots belonging to the authenticated professor.
     */
    List<SlotResponse> getSlotsForProfessor(Long professorId);

    /**
     * Returns booking roster and waitlist for a slot.
     */
    SlotBookingsResponse getBookingsForSlot(Long professorId,
                                            Long slotId);

    /**
     * Used ONLY by BookingService.
     * Increments bookedCount and updates SlotStatus if required.
     */
    void incrementBookedCount(Long slotId);

    /**
     * Used ONLY by BookingService.
     * Decrements bookedCount and updates SlotStatus if required.
     */
    void decrementBookedCount(Long slotId);

}