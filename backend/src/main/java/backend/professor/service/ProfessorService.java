package backend.professor.service;

import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorProfileResponse;
import backend.professor.dto.ProfessorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProfessorService {

    /**
     * Search professors by optional department and/or name.
     *
     * Used by:
     * GET /api/professors
     */
    Page<ProfessorResponse> searchProfessors(
            String department,
            String name,
            Pageable pageable
    );

    /**
     * Fetch a professor by ID.
     *
     * Used internally by Booking module.
     */
    ProfessorResponse getProfessorById(Long professorId);

    /**
     * Creates the ProfessorProfile after successful registration.
     *
     * Called ONLY from AuthService.register().
     */
    ProfessorProfileResponse createProfile(
            Long userId,
            ProfessorProfileRequest request
    );

}