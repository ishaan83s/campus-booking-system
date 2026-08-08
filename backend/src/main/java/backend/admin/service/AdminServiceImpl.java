package backend.admin.service;
import backend.admin.dto.*;
import backend.booking.repository.BookingRepository;
import backend.booking.service.BookingService;
import backend.common.enums.BookingStatus;
import backend.common.enums.Role;
import backend.common.enums.WaitlistStatus;
import backend.common.repository.UserRepository;
import backend.exception.ResourceNotFoundException;
import backend.professor.repository.SlotRepository;
import backend.waitlist.repository.WaitlistEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;

@Service @RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    private final UserRepository users; private final BookingRepository bookings; private final WaitlistEntryRepository waitlist; private final SlotRepository slots; private final BookingService bookingService;
    @Override @Transactional(readOnly = true) public List<UserManagementResponse> getAllUsers() { return users.findAll().stream().sorted(Comparator.comparing(u -> u.getId())).map(u -> new UserManagementResponse(u.getId(), u.getFullName(), u.getEmail(), u.getRole(), u.isActive())).toList(); }
    @Override @Transactional public void deactivateUser(Long userId) { var user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found")); user.setActive(false); }
    @Override @Transactional public void activateUser(Long userId) { var user = users.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found")); user.setActive(true); }
    @Override @Transactional(readOnly = true) public GlobalReportResponse getGlobalReport() { long capacity = slots.findAll().stream().mapToLong(s -> s.getCapacity()).sum(); long booked = slots.findAll().stream().mapToLong(s -> s.getBookedCount()).sum(); return new GlobalReportResponse(users.count(), users.findAll().stream().filter(u -> u.getRole() == Role.PROFESSOR).count(), users.findAll().stream().filter(u -> u.getRole() == Role.STUDENT).count(), bookings.count(), bookings.findAll().stream().filter(b -> b.getStatus() == BookingStatus.BOOKED).count(), waitlist.findAll().stream().filter(w -> w.getStatus() == WaitlistStatus.WAITING).count(), capacity == 0 ? 0.0 : booked * 100.0 / capacity); }
    @Override @Transactional public void forceCancelBooking(Long bookingId) { bookingService.cancelBooking(null, Role.ADMIN, bookingId); }
}
