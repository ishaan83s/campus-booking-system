package backend.professor.controller;

import backend.security.UserPrincipal;
import backend.common.enums.Role;
import backend.professor.dto.ProfessorResponse;
import backend.professor.dto.SlotBookingsResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;
import backend.professor.service.ProfessorService;
import backend.professor.service.SlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Thin HTTP layer only (Section 4 - System Architecture, Section 12.4) -
 * request validation via @Valid and DTO<->HTTP mapping, no business logic
 * (Section 5.5 PR checklist: "Controllers contain no business logic").
 *
 * Endpoint ownership per Section 19 - API Ownership Matrix (Dev B):
 *   GET    /api/professors
 *   GET    /api/professors/{professorId}/slots
 *   POST   /api/professors/slots
 *   PUT    /api/professors/slots/{slotId}
 *   DELETE /api/professors/slots/{slotId}
 *   GET    /api/professors/slots/{slotId}/bookings
 *
 * GET /api/professors/slots/me is an addition not literally enumerated in
 * Section 19's table. It exists to satisfy the Role Capability Matrix's
 * "View own schedule / booked students" row for Professor (Section 1.2),
 * since GET /api/professors/{professorId}/slots is documented as the
 * Student-facing view that hides CANCELLED/COMPLETED and past slots
 * (Section 3.2) - a professor managing their own schedule needs to see
 * those too. Flagged here explicitly per the AI Development Rules
 * (Section 15) rather than silently overloading one endpoint for two
 * different visibility rules.
 *
 * @AuthenticationPrincipal User assumes CustomUserDetailsService
 * (security/, Dev A) resolves the JWT subject to the shared User entity -
 * matching the JwtAuthenticationFilter -> SecurityContext flow in
 * Section 1.1's architecture diagram.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ProfessorController {

    private final ProfessorService professorService;
    private final SlotService slotService;

    // ---- Public / Student browse endpoints -----------------------------

    @GetMapping("/professors")
    public ResponseEntity<Page<ProfessorResponse>> searchProfessors(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(professorService.searchProfessors(department, name, pageable));
    }

    @GetMapping("/professors/{professorId}/slots")
    public ResponseEntity<Map<String, Object>> getBookableSlots(@PathVariable Long professorId) {
        ProfessorResponse professor = professorService.getProfessorById(professorId);
        List<SlotResponse> slots = slotService.getBookableSlotsForProfessor(professorId);

        // Matches the exact wrapper shape in main spec Section 3.2:
        // { professorId, professorName, slots: [...] }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("professorId", professor.getProfessorId());
        body.put("professorName", professor.getFullName());
        body.put("slots", slots);
        return ResponseEntity.ok(body);
    }

    // ---- Professor-only slot management ---------------------------------

    @PreAuthorize("hasRole('PROFESSOR')")
    @GetMapping("/professors/slots/me")
    public ResponseEntity<List<SlotResponse>> getMySlots(@AuthenticationPrincipal UserPrincipal professor) {
        return ResponseEntity.ok(slotService.getSlotsForProfessor(professor.getId()));
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PostMapping("/professors/slots")
    public ResponseEntity<SlotResponse> createSlot(
            @AuthenticationPrincipal UserPrincipal professor,
            @Valid @RequestBody SlotRequest request) {
        SlotResponse response = slotService.createSlot(professor.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @PutMapping("/professors/slots/{slotId}")
    public ResponseEntity<SlotResponse> updateSlot(
            @AuthenticationPrincipal UserPrincipal professor,
            @PathVariable Long slotId,
            @Valid @RequestBody SlotRequest request) {
        SlotResponse response = slotService.updateSlot(professor.getId(), slotId, request);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('PROFESSOR')")
    @DeleteMapping("/professors/slots/{slotId}")
    public ResponseEntity<Void> cancelSlot(
            @AuthenticationPrincipal UserPrincipal professor,
            @PathVariable Long slotId) {
        slotService.cancelSlot(professor.getId(), slotId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasAnyRole('PROFESSOR', 'ADMIN')")
    @GetMapping("/professors/slots/{slotId}/bookings")
    public ResponseEntity<SlotBookingsResponse> getBookingsForSlot(
            @AuthenticationPrincipal UserPrincipal requester,
            @PathVariable Long slotId) {
        SlotBookingsResponse response = requester.getRole() == Role.ADMIN
                ? slotService.getBookingsForSlotAsAdmin(slotId)
                : slotService.getBookingsForSlot(requester.getId(), slotId);
        return ResponseEntity.ok(response);
    }
}
