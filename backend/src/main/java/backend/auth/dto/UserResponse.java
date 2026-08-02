package backend.auth.dto;

import backend.common.enums.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;

    private String fullName;

    private String email;

    private Role role;
}