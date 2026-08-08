package backend.professor.model;

import backend.common.entity.BaseEntity;
import backend.common.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity mapping to the `professor_profiles` table (Section 2.3).
 * 1:1 extension of {@link User} for users whose role is PROFESSOR.
 *
 * Ownership: professor/ (Section 9 - Entity Ownership Matrix).
 * Nobody outside professor/ may construct or persist this entity directly -
 * it is only ever created via {@code ProfessorService.createProfile()},
 * which is called exclusively from AuthServiceImpl.register() (Section 8.2,
 * Section 6.1).
 */
@Entity
@Table(name = "professor_profiles")
@Getter
@Setter
@NoArgsConstructor
public class ProfessorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "office_location", length = 150)
    private String officeLocation;

    @Column(name = "bio", length = 1000)
    private String bio;
}