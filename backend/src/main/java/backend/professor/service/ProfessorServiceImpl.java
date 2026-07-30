package backend.professor.service;

import backend.common.entity.User;
import backend.common.repository.UserRepository;
import backend.exception.ResourceNotFoundException;
import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorProfileResponse;
import backend.professor.dto.ProfessorResponse;
import backend.professor.model.ProfessorProfile;
import backend.professor.repository.ProfessorProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfessorServiceImpl implements ProfessorService {

    private final ProfessorProfileRepository professorProfileRepository;

    // Read-only per Section 8.1: "Every other module may call
    // UserRepository.findById() read-only, to resolve a name/email for
    // display purposes." professor/ never writes to User here - only
    // AuthServiceImpl may do that.
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<ProfessorResponse> searchProfessors(String department, String name, Pageable pageable) {
        Page<ProfessorProfile> page = professorProfileRepository.search(
                blankToNull(department), blankToNull(name), pageable);
        return page.map(this::toProfessorResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfessorResponse getProfessorById(Long professorId) {
        // professorId is the shared users.id (the FK target throughout the
        // schema, Section 2.2), so we look the profile up by its user_id.
        ProfessorProfile profile = professorProfileRepository.findByUserId(professorId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Professor with id " + professorId + " not found"));
        return toProfessorResponse(profile);
    }

    @Override
    @Transactional
    public ProfessorProfileResponse createProfile(Long userId, ProfessorProfileRequest request) {
        // The User row is expected to already be persisted by
        // AuthServiceImpl.register() before this is ever called
        // (Section 6.1) - professor/ never inserts into `users` itself
        // (Section 8.1, "Nobody else may... Insert a row into users
        // directly").
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User with id " + userId + " not found"));

        ProfessorProfile profile = new ProfessorProfile();
        profile.setUser(user);
        profile.setDepartment(request.getDepartment());
        profile.setOfficeLocation(request.getOfficeLocation());
        profile.setBio(request.getBio());

        ProfessorProfile saved = professorProfileRepository.save(profile);

        return ProfessorProfileResponse.builder()
                .professorId(saved.getId())
                .userId(user.getId())
                .fullName(user.getFullName())
                .department(saved.getDepartment())
                .officeLocation(saved.getOfficeLocation())
                .bio(saved.getBio())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    private ProfessorResponse toProfessorResponse(ProfessorProfile profile) {
        return ProfessorResponse.builder()
                .professorId(profile.getUser().getId())
                .fullName(profile.getUser().getFullName())
                .department(profile.getDepartment())
                .officeLocation(profile.getOfficeLocation())
                .bio(profile.getBio())
                .build();
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value;
    }
}