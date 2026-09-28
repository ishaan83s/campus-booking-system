export const API_URL = (import.meta.env.VITE_API_URL ?? "http://localhost:8080").replace(/\/+$/, "");
export const SESSION_KEY = "ohs.session";

export class ApiError extends Error {
  constructor(message, status, body = null) {
    super(message);
    this.name = "ApiError";
    this.status = status;
    this.body = body;
  }
}

export async function api(path, { token, ...options } = {}) {
  const headers = {
    ...(options.body ? { "Content-Type": "application/json" } : {}),
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const response = await fetch(`${API_URL}${path}`, {
    ...options,
    headers,
  });

  if (response.status === 204) return null;

  const body = await response.json().catch(() => null);

  if (response.status === 401 && !path.startsWith("/api/auth/login")) {
    localStorage.removeItem(SESSION_KEY);
    window.dispatchEvent(new CustomEvent("ohs:session-expired"));
    throw new ApiError("Your session has expired. Please sign in again.", 401, body);
  }

  if (!response.ok) {
    throw new ApiError(body?.message ?? `Request failed (${response.status})`, response.status, body);
  }

  return body;
}
