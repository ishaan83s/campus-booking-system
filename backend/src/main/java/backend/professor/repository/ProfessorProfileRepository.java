package backend.professor.repository;

import backend.common.entity.User;
import backend.professor.model.ProfessorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfessorProfileRepository extends JpaRepository<ProfessorProfile, Long> {

    Optional<ProfessorProfile> findByUserId(Long userId);

    Optional<ProfessorProfile> findByUser(User user);

    boolean existsByUserId(Long userId);

    @Query("""
            SELECT p
            FROM ProfessorProfile p
            WHERE
                (:department IS NULL OR
                 LOWER(p.department) = LOWER(:department))
            AND
                (:name IS NULL OR
                 LOWER(p.user.fullName) LIKE LOWER(CONCAT('%', :name, '%')))
            """)
    Page<ProfessorProfile> searchProfessors(
            @Param("department") String department,
            @Param("name") String name,
            Pageable pageable
    );

}