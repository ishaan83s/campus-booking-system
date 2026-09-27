import { api } from "./client";

export async function getAdminReports(token) {
  return api("/api/admin/reports", { token });
}

export async function getAdminUsers(token) {
  return api("/api/admin/users", { token });
}

export async function toggleUserActive(token, userId, action) {
  return api(`/api/admin/users/${userId}/${action}`, {
    token,
    method: "PATCH",
  });
}

export async function forceCancelBooking(token, bookingId) {
  return api(`/api/admin/bookings/${bookingId}`, {
    token,
    method: "DELETE",
  });
}
