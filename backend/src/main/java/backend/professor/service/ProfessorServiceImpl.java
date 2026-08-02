package backend.professor.service;

import backend.common.entity.User;
import backend.common.enums.SlotStatus;
import backend.common.repository.UserRepository;
import backend.professor.dto.ProfessorProfileRequest;
import backend.professor.dto.ProfessorResponse;
import backend.professor.dto.SlotRequest;
import backend.professor.dto.SlotResponse;
import backend.professor.entity.ProfessorProfile;
import backend.professor.entity.Slot;
import backend.professor.repository.ProfessorProfileRepository;
import backend.professor.repository.SlotRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProfessorServiceImpl implements ProfessorService {

    private final ProfessorProfileRepository professorRepository;
    private final SlotRepository slotRepository;
    private final UserRepository userRepository;

    public ProfessorServiceImpl(
            ProfessorProfileRepository professorRepository,
            SlotRepository slotRepository,
            UserRepository userRepository) {

        this.professorRepository = professorRepository;
        this.slotRepository = slotRepository;
        this.userRepository = userRepository;
    }


    // =====================================================
    // CREATE PROFESSOR PROFILE
    // =====================================================

    @Override
    public ProfessorResponse createProfessorProfile(
            ProfessorProfileRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() ->
                        new RuntimeException("User not found")
                );

        ProfessorProfile professor = ProfessorProfile.builder()
                .user(user)
                .department(request.getDepartment())
                .officeLocation(request.getOfficeLocation())
                .bio(request.getBio())
                .build();

        ProfessorProfile savedProfessor =
                professorRepository.save(professor);

        return ProfessorResponse.builder()
                .id(savedProfessor.getId())
                .fullName(
                        savedProfessor
                                .getUser()
                                .getFullName()
                )
                .department(
                        savedProfessor.getDepartment()
                )
                .officeLocation(
                        savedProfessor.getOfficeLocation()
                )
                .bio(savedProfessor.getBio())
                .build();
    }


    // =====================================================
    // GET PROFESSOR
    // =====================================================

    @Override
    public ProfessorResponse getProfessor(Long id) {

        ProfessorProfile professor =
                professorRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professor not found"
                                )
                        );

        return ProfessorResponse.builder()
                .id(professor.getId())
                .fullName(
                        professor
                                .getUser()
                                .getFullName()
                )
                .department(
                        professor.getDepartment()
                )
                .officeLocation(
                        professor.getOfficeLocation()
                )
                .bio(professor.getBio())
                .build();
    }


    // =====================================================
    // CREATE SLOT
    // =====================================================

    @Override
    public SlotResponse createSlot(
            Long professorId,
            SlotRequest request) {

        ProfessorProfile professor =
                professorRepository.findById(professorId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Professor not found"
                                )
                        );

        Slot slot = Slot.builder()
                .professor(professor)
                .slotDate(request.getSlotDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .capacity(request.getCapacity())
                .bookedCount(0)
                .status(SlotStatus.AVAILABLE)
                .build();

        Slot savedSlot =
                slotRepository.save(slot);

        return convertToSlotResponse(savedSlot);
    }


    // =====================================================
    // GET ONE PROFESSOR'S SLOTS
    // =====================================================

    @Override
    public List<SlotResponse> getProfessorSlots(
            Long professorId) {

        return slotRepository
                .findByProfessorId(professorId)
                .stream()
                .map(this::convertToSlotResponse)
                .collect(Collectors.toList());
    }


    // =====================================================
    // GET ALL AVAILABLE SLOTS
    // =====================================================

    @Override
    public List<SlotResponse> getAvailableSlots() {

        return slotRepository
                .findByStatus(SlotStatus.AVAILABLE)
                .stream()
                .map(this::convertToSlotResponse)
                .collect(Collectors.toList());
    }


    // =====================================================
    // CONVERT SLOT ENTITY -> SLOT RESPONSE
    // =====================================================

    private SlotResponse convertToSlotResponse(
            Slot slot) {

        ProfessorProfile professor =
                slot.getProfessor();

        return SlotResponse.builder()
                .id(slot.getId())

                .professorId(
                        professor.getId()
                )

                .professorName(
                        professor
                                .getUser()
                                .getFullName()
                )

                .department(
                        professor.getDepartment()
                )

                .officeLocation(
                        professor.getOfficeLocation()
                )

                .slotDate(
                        slot.getSlotDate()
                )

                .startTime(
                        slot.getStartTime()
                )

                .endTime(
                        slot.getEndTime()
                )

                .capacity(
                        slot.getCapacity()
                )

                .bookedCount(
                        slot.getBookedCount()
                )

                .status(
                        slot.getStatus()
                )

                .build();
    }
}