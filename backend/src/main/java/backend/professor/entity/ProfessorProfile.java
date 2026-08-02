package backend.professor.entity;

import backend.common.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "professor_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String department;

    private String officeLocation;

    @Column(length = 500)
    private String bio;
}