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

/**
 * Maps to the `bookings` table (Backend Development Bible, Section 2.3).
 * Owned exclusively by booking/ (Section 9 - Entity Ownership Matrix).
 * Only ever represents a CONFIRMED reservation - a student on a full slot's
 * queue gets a row in waitlist_entries instead (Section 2.1).
 * No physical DELETE in the happy path - status flips to CANCELLED/COMPLETED
 * so booking history is preserved.
 */
@Entity
@Table(name = "bookings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Booking extends BaseEntity {

    /**
     * FK -> users.id (the student who made the booking).
     * Read-only reference - booking/ never writes to the User row itself,
     * per Section 8.1 (Auth Module ownership rules).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    /**
     * FK -> slots.id.
     * booking/ only ever reads Slot via SlotRepository.findByIdForUpdate()
     * and mutates booked_count/status through SlotService, never directly
     * (Section 8.2 / Section 16 - Integration Rules).
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "slot_id", nullable = false)
    private Slot slot;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BookingStatus status = BookingStatus.BOOKED;

    @Column(name = "booked_at", nullable = false)
    private LocalDateTime bookedAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}