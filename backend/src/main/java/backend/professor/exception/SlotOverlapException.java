package backend.professor.exception;

/**
 * Section 8.2 - "Exceptions owned" by professor/. Thrown when a professor
 * tries to create/update a slot that collides with one they already own
 * at the exact same date + start time (the uq_slots_professor_datetime
 * constraint, Section 2.4 - Key Constraints Cheat-Sheet), or when a
 * PUT /api/professors/slots/{slotId} request violates the
 * "bookings exist -> only capacity may increase" rule (Section 3.2).
 * Mapped to 409 CONFLICT by GlobalExceptionHandler (Section 12.3).
 */
public class SlotOverlapException extends RuntimeException {
    public SlotOverlapException(String message) {
        super(message);
    }
}