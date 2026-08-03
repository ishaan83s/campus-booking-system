package backend.admin.service;
import backend.admin.dto.GlobalReportResponse;
import backend.admin.dto.UserManagementResponse;
import java.util.List;
public interface AdminService { List<UserManagementResponse> getAllUsers(); void deactivateUser(Long userId); void activateUser(Long userId); GlobalReportResponse getGlobalReport(); void forceCancelBooking(Long bookingId); }
