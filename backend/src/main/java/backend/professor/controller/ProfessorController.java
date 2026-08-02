package backend.professor.controller;

import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;
import backend.professor.service.ProfessorService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/professors")
public class ProfessorController {

    private final ProfessorService professorService;

    public ProfessorController(ProfessorService professorService) {
        this.professorService = professorService;
    }


    // =====================================================
    // CREATE PROFESSOR PROFILE
    // =====================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProfessorResponse createProfessorProfile(
            @RequestBody ProfessorProfileRequest request) {

        return professorService.createProfessorProfile(request);
    }


    // =====================================================
    // GET ALL AVAILABLE SLOTS
    // IMPORTANT: Keep this before /{id}
    // =====================================================

    @GetMapping("/slots/available")
    public List<SlotResponse> getAvailableSlots() {

        return professorService.getAvailableSlots();
    }


    // =====================================================
    // GET PROFESSOR
    // =====================================================

    @GetMapping("/{id}")
    public ProfessorResponse getProfessor(
            @PathVariable Long id) {

        return professorService.getProfessor(id);
    }


    // =====================================================
    // CREATE PROFESSOR SLOT
    // =====================================================

    @PostMapping("/{id}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public SlotResponse createSlot(
            @PathVariable Long id,
            @RequestBody SlotRequest request) {

        return professorService.createSlot(id, request);
    }


    // =====================================================
    // GET ONE PROFESSOR'S SLOTS
    // =====================================================

    @GetMapping("/{id}/slots")
    public List<SlotResponse> getProfessorSlots(
            @PathVariable Long id) {

        return professorService.getProfessorSlots(id);
    }
}