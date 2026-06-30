import { useEffect, useState, type ReactNode } from "react";
import { useApiFetch } from "../apiFetch";
import { LoggedInContext } from "./LoggedInContext";
import { useSession } from "./SessionContext";

export function LoggedInProvider({ children }: { children: ReactNode }) {
  const [loggedIn, setLoggedInState] = useState<boolean>(false);
  const { sessionToken } = useSession();

  const apiFetch = useApiFetch();

  const setLoggedIn = (val: boolean) => {
    setLoggedInState(val);
    localStorage.setItem("isLoggedIn", String(val));
  };

  useEffect(() => {
    async function isLogged() {
      if (!sessionToken) {
        setLoggedIn(false);
        return;
      }

      try {
        const response = await apiFetch(
          "http://localhost:8080/api/user/role/signed",
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const result = (await response.text()).trim();
        setLoggedIn(result === "true");
      } catch (err) {
        console.error("Failed to check logged-in status:", err);
        setLoggedIn(false);
      }
    }

    void isLogged();
  }, [sessionToken, apiFetch]);

  return (
    <LoggedInContext.Provider value={{ loggedIn, setLoggedIn }}>
      {children}
    </LoggedInContext.Provider>
  );
}
