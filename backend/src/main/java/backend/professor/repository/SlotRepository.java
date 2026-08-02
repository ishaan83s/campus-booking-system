package backend.professor.repository;

import backend.common.enums.SlotStatus;
import backend.professor.entity.Slot;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Get all slots belonging to one professor
    List<Slot> findByProfessorId(Long professorId);

    // Get all slots having a particular status
    List<Slot> findByStatus(SlotStatus status);
}