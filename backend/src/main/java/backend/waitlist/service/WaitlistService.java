package backend.waitlist.service;

import backend.waitlist.dto.WaitlistEntryResponse;

public interface WaitlistService {

    WaitlistEntryResponse joinWaitlist(Long studentId, Long slotId);

    void promoteNext(Long slotId);

    void leaveWaitlist(Long studentId, Long waitlistId);
}