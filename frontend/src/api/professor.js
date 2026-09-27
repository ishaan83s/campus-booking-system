import { api } from "./client";

export async function getMySlots(token) {
  return api("/api/professors/slots/me", { token });
}

export async function createSlot(token, slotData) {
  return api("/api/professors/slots", {
    token,
    method: "POST",
    body: JSON.stringify(slotData),
  });
}

export async function updateSlot(token, slotId, slotData) {
  return api(`/api/professors/slots/${slotId}`, {
    token,
    method: "PUT",
    body: JSON.stringify(slotData),
  });
}

export async function cancelSlot(token, slotId) {
  return api(`/api/professors/slots/${slotId}`, {
    token,
    method: "DELETE",
  });
}

export async function getSlotBookings(token, slotId) {
  return api(`/api/professors/slots/${slotId}/bookings`, { token });
}
