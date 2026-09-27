package backend.professor.repository;

import backend.professor.model.Slot;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

/**
 * Section 9 - Entity Ownership Matrix: owns the slots table.
 *
 * findByIdForUpdate() and findByProfessorIdAndSlotDateGreaterThanEqual()
 * are named verbatim in Section 8.2's interface listing.
 * findByIdForUpdate() is the ONLY method booking/ is allowed to call on
 * this repository (Section 8.2 / Section 16 - Integration Rules), and the
 * caller must never call .save() on the Slot it gets back directly - only
 * SlotService.incrementBookedCount()/decrementBookedCount() may mutate
 * bookedCount/status (Section 8.2, "recommended option (a)").
 */
public interface SlotRepository extends JpaRepository<Slot, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Slot s WHERE s.id = :slotId")
    Optional<Slot> findByIdForUpdate(@Param("slotId") Long slotId);

    List<Slot> findByProfessorIdAndSlotDateGreaterThanEqual(Long professorId, LocalDate date);

    /**
     * Backs the uq_slots_professor_datetime check (Section 2.4 - Key
     * Constraints Cheat-Sheet) at the service layer, ahead of hitting the
     * DB unique constraint, so SlotServiceImpl can throw a documented
     * SlotOverlapException (409) instead of surfacing a raw
     * DataIntegrityViolationException to the client.
     */
    boolean existsByProfessorIdAndSlotDateAndStartTime(Long professorId, LocalDate slotDate, LocalTime startTime);

    /**
     * True time-range overlap check: two slots overlap when
     * newStart &lt; existingEnd AND existingStart &lt; newEnd.
     * Excludes CANCELLED slots and optionally a specific slot ID (for updates).
     */
    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Slot s "
            + "WHERE s.professor.id = :professorId "
            + "AND s.slotDate = :slotDate "
            + "AND s.startTime < :endTime "
            + "AND s.endTime > :startTime "
            + "AND s.status <> backend.common.enums.SlotStatus.CANCELLED "
            + "AND (:excludeSlotId IS NULL OR s.id <> :excludeSlotId)")
    boolean existsOverlappingSlot(@Param("professorId") Long professorId,
                                  @Param("slotDate") LocalDate slotDate,
                                  @Param("startTime") LocalTime startTime,
                                  @Param("endTime") LocalTime endTime,
                                  @Param("excludeSlotId") Long excludeSlotId);
}