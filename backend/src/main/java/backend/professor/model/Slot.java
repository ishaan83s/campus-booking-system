package backend.professor.model;

import backend.common.entity.BaseEntity;
import backend.common.entity.User;
import backend.common.enums.SlotStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "slots",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_slots_professor_datetime",
                        columnNames = {
                                "professor_id",
                                "slot_date",
                                "start_time"
                        }
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Slot extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "professor_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_slots_professor")
    )
    private User professor;

    @NotNull
    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @NotNull
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @NotNull
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @NotNull
    @Min(1)
    @Column(name = "capacity", nullable = false)
    @Builder.Default
    private Integer capacity = 1;

    @NotNull
    @Min(0)
    @Column(name = "booked_count", nullable = false)
    @Builder.Default
    private Integer bookedCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private SlotStatus status = SlotStatus.OPEN;
}