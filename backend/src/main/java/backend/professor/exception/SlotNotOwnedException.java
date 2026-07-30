package backend.professor.exception;

/**
 * Section 8.2 - "Exceptions owned" by professor/. Thrown when a professor
 * attempts to edit, cancel, or view the roster of a slot they do not own
 * (main spec Section 3.2 - "Role required: PROFESSOR (must own the
 * slot)"). Mapped to 403 FORBIDDEN by GlobalExceptionHandler
 * (Section 12.3).
 */
public class SlotNotOwnedException extends RuntimeException {
    public SlotNotOwnedException(String message) {
        super(message);
    }
}