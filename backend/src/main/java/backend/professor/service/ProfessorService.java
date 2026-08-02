package backend.professor.service;

import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;

import java.util.List;

public interface ProfessorService {

    ProfessorResponse createProfessorProfile(
            ProfessorProfileRequest request
    );

    ProfessorResponse getProfessor(Long id);

    SlotResponse createSlot(
            Long professorId,
            SlotRequest request
    );

    List<SlotResponse> getProfessorSlots(
            Long professorId
    );

    List<SlotResponse> getAvailableSlots();
}