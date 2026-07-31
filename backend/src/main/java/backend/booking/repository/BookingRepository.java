package backend.booking.repository;

import backend.booking.model.Booking;
import backend.common.enums.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByStudentIdAndSlotIdAndStatus(
            Long studentId,
            Long slotId,
            BookingStatus status
    );

    List<Booking> findByStudentId(Long studentId);

    List<Booking> findByStudentIdAndStatus(
            Long studentId,
            BookingStatus status
    );

    List<Booking> findBySlotIdAndStatus(
            Long slotId,
            BookingStatus status
    );
}