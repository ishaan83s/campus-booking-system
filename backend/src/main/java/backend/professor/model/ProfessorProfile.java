package backend.professor.model;

import backend.common.entity.BaseEntity;
import backend.common.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "professor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorProfile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(name = "fk_professor_profiles_user")
    )
    private User user;

    @NotBlank
    @Column(name = "department", nullable = false, length = 100)
    private String department;

    @Column(name = "office_location", length = 150)
    private String officeLocation;

    @Column(name = "bio", length = 1000)
    private String bio;
}