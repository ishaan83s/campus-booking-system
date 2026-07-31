package backend.professor.repository;

import backend.common.enums.SlotStatus;
import backend.professor.model.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SlotRepository extends JpaRepository<Slot, Long> {

    /**
     * Used ONLY by BookingService to safely lock a slot during booking.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId")
    Optional<Slot> findByIdForUpdate(@Param("slotId") Long slotId);

    /**
     * Professor's own slots.
     */
    List<Slot> findByProfessorIdOrderBySlotDateAscStartTimeAsc(Long professorId);

    /**
     * Student browse endpoint.
     * Returns future slots for a professor.
     */
    List<Slot> findByProfessorIdAndSlotDateGreaterThanEqualOrderBySlotDateAscStartTimeAsc(
            Long professorId,
            LocalDate slotDate
    );

    /**
     * Overlap / duplicate start-time validation.
     */
    boolean existsByProfessorIdAndSlotDateAndStartTime(
            Long professorId,
            LocalDate slotDate,
            LocalTime startTime
    );

    /**
     * Student-visible future slots filtered by status.
     */
    List<Slot> findByProfessorIdAndSlotDateGreaterThanEqualAndStatusInOrderBySlotDateAscStartTimeAsc(
            Long professorId,
            LocalDate slotDate,
            List<SlotStatus> statuses
    );
}