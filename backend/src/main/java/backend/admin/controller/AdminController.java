package backend.admin.controller;
import backend.admin.dto.*;
import backend.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/admin") @RequiredArgsConstructor @PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private final AdminService adminService;
    @GetMapping("/users") public ResponseEntity<List<UserManagementResponse>> users() { return ResponseEntity.ok(adminService.getAllUsers()); }
    @PatchMapping("/users/{userId}/deactivate") public ResponseEntity<Void> deactivate(@PathVariable Long userId) { adminService.deactivateUser(userId); return ResponseEntity.noContent().build(); }
    @PatchMapping("/users/{userId}/activate") public ResponseEntity<Void> activate(@PathVariable Long userId) { adminService.activateUser(userId); return ResponseEntity.noContent().build(); }
    @GetMapping("/reports") public ResponseEntity<GlobalReportResponse> reports() { return ResponseEntity.ok(adminService.getGlobalReport()); }
    @DeleteMapping("/bookings/{bookingId}") public ResponseEntity<Void> forceCancel(@PathVariable Long bookingId) { adminService.forceCancelBooking(bookingId); return ResponseEntity.noContent().build(); }
}
