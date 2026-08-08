package backend.admin.dto;
import lombok.Value;
@Value public class GlobalReportResponse { long totalUsers; long totalProfessors; long totalStudents; long totalBookings; long totalActiveBookings; long totalWaitlisted; double slotUtilizationPercent; }
