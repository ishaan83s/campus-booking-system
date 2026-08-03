package backend.waitlist.repository;

import backend.common.enums.WaitlistStatus;
import backend.waitlist.model.WaitlistEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WaitlistEntryRepository extends JpaRepository<WaitlistEntry, Long> {
    Optional<WaitlistEntry> findFirstBySlotIdAndStatusOrderByPositionAsc(Long slotId, WaitlistStatus status);
    List<WaitlistEntry> findBySlotIdAndStatus(Long slotId, WaitlistStatus status);
    List<WaitlistEntry> findByStudentIdAndStatus(Long studentId, WaitlistStatus status);
    boolean existsByStudentIdAndSlotIdAndStatus(Long studentId, Long slotId, WaitlistStatus status);
}
