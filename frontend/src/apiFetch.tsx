import { useCallback } from "react";
import { useSession } from "./GlobalContext/SessionContext";

export function useApiFetch() {
  const { sessionToken, requestNewSessionToken } = useSession();

  const apiFetch = useCallback(
    async (url: string, options: RequestInit = {}) => {
      const headers = new Headers(options.headers);

      if (sessionToken) {
        headers.set("Authorization", sessionToken);
      }

      let response = await fetch(url, {
        ...options,
        headers,
      });

      if (response.status === 401) {
        const newToken = await requestNewSessionToken();

        headers.set("Authorization", newToken);

        response = await fetch(url, {
          ...options,
          headers,
        });
      }

      return response;
    },
    [sessionToken, requestNewSessionToken],
  );

  return apiFetch;
}
