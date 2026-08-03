package backend.auth.service;

import backend.auth.dto.*;
import backend.common.entity.User;
import backend.common.enums.Role;
import backend.common.repository.UserRepository;
import backend.exception.ConflictException;
import backend.exception.ForbiddenOperationException;
import backend.exception.ResourceNotFoundException;
import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.service.ProfessorService;
import backend.security.JwtTokenProvider;
import backend.security.UserPrincipal;
import backend.student.dto.StudentProfileRequest;
import backend.student.service.StudentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Locale;

@Service @RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository users; private final PasswordEncoder passwords; private final ProfessorService professors; private final StudentService students; private final JwtTokenProvider tokens;
    @Override @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request.getRole() == Role.ADMIN) throw new ForbiddenOperationException("Admin accounts must be seeded by an administrator");
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        if (users.existsByEmail(email)) throw new ConflictException("An account with this email already exists");
        if (request.getRole() == Role.STUDENT && (blank(request.getRollNo()) || request.getYearOfStudy() == null)) throw new IllegalArgumentException("rollNo and yearOfStudy are required for students");
        if (request.getRole() == Role.PROFESSOR && blank(request.getDepartment())) throw new IllegalArgumentException("department is required for professors");
        User user = new User(); user.setFullName(request.getFullName().trim()); user.setEmail(email); user.setPassword(passwords.encode(request.getPassword())); user.setRole(request.getRole()); user = users.save(user);
        if (user.getRole() == Role.STUDENT) students.createProfile(user.getId(), new StudentProfileRequest(request.getRollNo(), request.getYearOfStudy()));
        else professors.createProfile(user.getId(), new ProfessorProfileRequest(request.getDepartment(), null, null));
        return toResponse(user);
    }
    @Override @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(request.getEmail().trim().toLowerCase(Locale.ROOT)).orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if (!passwords.matches(request.getPassword(), user.getPassword())) throw new BadCredentialsException("Invalid email or password");
        if (!user.isActive()) throw new ForbiddenOperationException("This account has been deactivated");
        AuthResponse result = new AuthResponse(); result.setAccessToken(tokens.generateToken(UserPrincipal.from(user))); result.setTokenType("Bearer"); result.setExpiresIn(tokens.getExpirySeconds()); result.setUser(toResponse(user)); return result;
    }
    @Override @Transactional(readOnly = true) public UserResponse getCurrentUser(Long userId) { return toResponse(users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"))); }
    private boolean blank(String value) { return value == null || value.isBlank(); }
    private UserResponse toResponse(User user) { UserResponse response = new UserResponse(); response.setId(user.getId()); response.setFullName(user.getFullName()); response.setEmail(user.getEmail()); response.setRole(user.getRole()); response.setIsActive(user.isActive()); response.setCreatedAt(user.getCreatedAt()); return response; }
}
