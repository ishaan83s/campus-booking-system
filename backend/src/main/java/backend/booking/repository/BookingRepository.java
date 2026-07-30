package backend.booking.repository;

import backend.booking.model.Booking;
import backend.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Section 8.4 - Booking Module Exposes.
 * Interfaces only, no logic (Section 1.1 - Repository Layer rule).
 *
 * Read access granted to other modules per Section 9 (Entity Ownership
 * Matrix): admin/ must never call these directly to mutate a Booking -
 * it always goes through BookingService.cancelBooking() (Section 8.4 -
 * "Nobody else may" rule).
 */
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByStudentIdAndSlotIdAndStatus(Long studentId, Long slotId, BookingStatus status);

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findBySlotIdAndStatus(Long slotId, BookingStatus status);
}