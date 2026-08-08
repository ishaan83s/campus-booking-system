package backend.professor.repository;

import backend.professor.model.ProfessorProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Section 9 - Entity Ownership Matrix: owns the professor_profiles table.
 * Only professor/ may call .save()/.delete() here (Section 8.2 /
 * Section 8.1 - a ProfessorProfile is only ever constructed via
 * ProfessorService.createProfile()).
 */
public interface ProfessorProfileRepository extends JpaRepository<ProfessorProfile, Long> {

    /**
     * Nested-property derived query (resolves to professorProfile.user.id
     * since ProfessorProfile has no literal "userId" field). Used by
     * ProfessorServiceImpl.getProfessorById() and by AuthServiceImpl
     * (read-only) to check whether a profile already exists for a user.
     */
    Optional<ProfessorProfile> findByUserId(Long userId);

    /**
     * Backs ProfessorService.searchProfessors(department, name, pageable)
     * (Section 6.1's interface contract with Dev C, and Section 8.2).
     * department is matched exactly (case-insensitive); name is matched
     * against the linked User.fullName with a case-insensitive "contains".
     * Either filter may be null (no filter applied).
     */
    @Query("SELECT pp FROM ProfessorProfile pp JOIN pp.user u "
            + "WHERE (:department IS NULL OR LOWER(pp.department) = LOWER(:department)) "
            + "AND (:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%')))")
    Page<ProfessorProfile> search(@Param("department") String department,
                                  @Param("name") String name,
                                  Pageable pageable);
}