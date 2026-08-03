package backend.waitlist.service;

import backend.waitlist.dto.WaitlistEntryResponse;
import java.util.List;

public interface WaitlistService {
    WaitlistEntryResponse joinWaitlist(Long studentId, Long slotId);
    void promoteNext(Long slotId);
    void leaveWaitlist(Long studentId, Long waitlistId);
    List<WaitlistEntryResponse> getActiveEntriesForStudent(Long studentId);
}
