package backend.professor.repository;

import backend.professor.entity.ProfessorProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProfessorProfileRepository extends JpaRepository<ProfessorProfile, Long> {

    Optional<ProfessorProfile> findByUserId(Long userId);

}