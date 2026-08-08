package backend.professor.model;

import backend.common.entity.BaseEntity;
import backend.common.entity.User;
import backend.common.enums.SlotStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * JPA entity mapping to the `slots` table (Backend Technical Spec, Section 2.3).
 *
 * Ownership: professor/ (Addendum Section 9 - Entity Ownership Matrix).
 * Only SlotService may write to {@code bookedCount} / {@code status}
 * (Section 8.2). booking/ is only permitted to read a locked row via
 * SlotRepository.findByIdForUpdate() and must route any mutation back
 * through SlotService.incrementBookedCount()/decrementBookedCount()
 * (Section 16 - Integration Rules).
 */
@Entity
@Table(name = "slots")
@Getter
@Setter
@NoArgsConstructor
public class Slot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "professor_id", nullable = false)
    private User professor;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Column(name = "booked_count", nullable = false)
    private Integer bookedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SlotStatus status = SlotStatus.OPEN;
}