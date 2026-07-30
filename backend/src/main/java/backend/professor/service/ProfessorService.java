package backend.professor.service;

import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorProfileResponse;
import backend.professor.dto.ProfessorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Section 8.2 - Module Interface Specification. This signature is fixed
 * by the Backend Development Bible; do not rename methods or add/remove
 * parameters without a team sign-off (Section 13 - Database Change
 * Governance style process, AI Development Rules Section 15).
 */
public interface ProfessorService {

    Page<ProfessorResponse> searchProfessors(String department, String name, Pageable pageable);

    ProfessorResponse getProfessorById(Long professorId);

    /**
     * Called only from AuthServiceImpl.register() right after the User
     * row is persisted (Section 6.1 - Interface Contracts Between
     * Developers). Nobody else may construct a ProfessorProfile directly
     * (Section 8.2, "Nobody else may").
     */
    ProfessorProfileResponse createProfile(Long userId, ProfessorProfileRequest request);
}