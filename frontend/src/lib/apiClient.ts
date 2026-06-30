// Single HTTP entry point for the web client.
//
// The browser talks ONLY to the API Gateway (same-origin `/api` in dev via the Vite proxy,
// the gateway's public URL in prod). Auth-token injection, refresh, and typed endpoint
// wrappers are added here as features land — this is the transport boundary only.

const BASE_URL = import.meta.env.VITE_API_GATEWAY_URL ?? "/api";

export async function apiFetch<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${BASE_URL}${path}`, {
    headers: { "Content-Type": "application/json", ...init?.headers },
    ...init,
  });
  if (!response.ok) {
    throw new Error(`API ${response.status}: ${response.statusText}`);
  }
  return response.json() as Promise<T>;
}
