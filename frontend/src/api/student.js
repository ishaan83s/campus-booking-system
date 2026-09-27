import { api } from "./client";

export async function getProfessors(token, page = 0, size = 50) {
  return api(`/api/professors?page=${page}&size=${size}`, { token });
}

export async function getProfessorSlots(token, professorId) {
  return api(`/api/professors/${professorId}/slots`, { token });
}

export async function bookSlot(token, slotId) {
  return api("/api/bookings", {
    token,
    method: "POST",
    body: JSON.stringify({ slotId }),
  });
}

export async function getMyBookings(token) {
  return api("/api/bookings/me", { token });
}

export async function cancelBooking(token, bookingId) {
  return api(`/api/bookings/${bookingId}`, {
    token,
    method: "DELETE",
  });
}

export async function leaveWaitlist(token, waitlistId) {
  return api(`/api/bookings/waitlist/${waitlistId}`, {
    token,
    method: "DELETE",
  });
}

export async function getStudentProfile(token) {
  return api("/api/students/me", { token });
}

export async function updateStudentProfile(token, profile) {
  return api("/api/students/me", {
    token,
    method: "PUT",
    body: JSON.stringify(profile),
  });
}
