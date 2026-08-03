package backend.waitlist.repository;

import backend.common.enums.WaitlistStatus;
import backend.waitlist.entity.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {

    boolean existsByStudentIdAndSlotId(Long studentId, Long slotId);

    List<WaitlistEntry> findBySlotIdAndStatusOrderByPositionAsc(
            Long slotId,
            WaitlistStatus status
    );

    Optional<WaitlistEntry> findFirstBySlotIdAndStatusOrderByPositionAsc(
            Long slotId,
            WaitlistStatus status
    );
}