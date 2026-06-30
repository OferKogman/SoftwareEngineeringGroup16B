import { useCallback, useEffect, useState } from "react";
import { SessionContext } from "./SessionContext";

export function SessionProvider({ children }: { children: React.ReactNode }) {
  const [sessionToken, setSessionTokenState] = useState<string | null>(null);

  const setSessionToken = (token: string | null) => {
    setSessionTokenState(token);

    if (token) {
      localStorage.setItem("sessionToken", token);
    } else {
      localStorage.removeItem("sessionToken");
    }
  };

  const [isSessionLoading, setIsSessionLoading] = useState(true);
  const [sessionError, setSessionError] = useState<string | null>(null);

  const requestNewSessionToken = useCallback(async (): Promise<string> => {
    const response = await fetch(
      "http://localhost:8080/api/user/login/guest/validate",
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          ...(localStorage.getItem("sessionToken")
            ? { Authorization: localStorage.getItem("sessionToken") as string }
            : {}),
        },
      },
    );

    if (!response.ok) {
      throw new Error(await response.text());
    }

    const token = (await response.text()).trim();

    setSessionToken(token);
    setSessionError(null);

    return token;
  }, []);

  function closePopup() {
    setSessionError(null);
  }

  useEffect(() => {
    async function validate() {
      try {
        await requestNewSessionToken();
      } catch (err) {
        console.error("Failed to fetch session token:", err);
        setSessionToken(null);
        setSessionError(err instanceof Error ? err.message : "");
      } finally {
        setIsSessionLoading(false);
      }
    }

    void validate();
  }, [requestNewSessionToken]);

  if (isSessionLoading) {
    return <div>Loading...</div>;
  }

  if (sessionError || !sessionToken) {
    return (
      <div className="settings-alert">
        <p>{sessionError || "No session token available."}</p>
        <button onClick={closePopup}> OK </button>
      </div>
    );
  }

  return (
    <SessionContext.Provider
      value={{ sessionToken, setSessionToken, requestNewSessionToken }}
    >
      {children}
    </SessionContext.Provider>
  );
}
