import { useEffect, useState, type ReactNode } from "react";
import { useApiFetch } from "../apiFetch";
import { AdminLoggedInContext } from "./AdminLoggedInContext";
import { useSession } from "./SessionContext";

export function AdminLoggedInProvider({ children }: { children: ReactNode }) {
  const [adminLoggedIn, setAdminLoggedInState] = useState<boolean>(false);
  const { sessionToken } = useSession();

  const apiFetch = useApiFetch();

  const setAdminLoggedIn = (val: boolean) => {
    setAdminLoggedInState(val);
    localStorage.setItem("isAdminLoggedIn", String(val));
  };

  useEffect(() => {
    async function isLogged() {
      if (!sessionToken) {
        setAdminLoggedIn(false);
        return;
      }

      try {
        const response = await apiFetch(
          "http://localhost:8080/api/user/role/admin",
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const result = (await response.text()).trim();
        setAdminLoggedIn(result === "true");
      } catch (err) {
        console.error("Failed to check logged-in status:", err);
        setAdminLoggedIn(false);
      }
    }

    void isLogged();
  }, [sessionToken, apiFetch]);

  return (
    <AdminLoggedInContext.Provider value={{ adminLoggedIn, setAdminLoggedIn }}>
      {children}
    </AdminLoggedInContext.Provider>
  );
}
