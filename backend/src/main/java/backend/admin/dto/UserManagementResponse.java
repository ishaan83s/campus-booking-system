package backend.admin.dto;
import backend.common.enums.Role;
import lombok.Value;
@Value public class UserManagementResponse { Long id; String fullName; String email; Role role; Boolean isActive; }
