export const API_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080";
export const SESSION_KEY = "ohs.session";

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
    throw new Error("Your session has expired. Please sign in again.");
  }

  if (!response.ok) {
    throw new Error(body?.message ?? `Request failed (${response.status})`);
  }

  return body;
}
