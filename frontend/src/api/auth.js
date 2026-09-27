import { api } from "./client";

export async function loginUser(credentials) {
  return api("/api/auth/login", {
    method: "POST",
    body: JSON.stringify(credentials),
  });
}

export async function registerUser(payload) {
  return api("/api/auth/register", {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export async function getMe(token) {
  return api("/api/auth/me", { token });
}
