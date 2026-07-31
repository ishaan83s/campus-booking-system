package backend.booking.model;

import backend.common.entity.BaseEntity;
import backend.common.entity.User;
import backend.common.enums.BookingStatus;
import backend.professor.model.Slot;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "booked_at", nullable = false)
    @Builder.Default
    private LocalDateTime bookedAt = LocalDateTime.now();

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}