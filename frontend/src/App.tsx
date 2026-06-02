import { createContext, useContext, useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

type SessionContextType = {
  sessionToken: string;
  setSessionToken: React.Dispatch<React.SetStateAction<string | null>>;
};
type LoggedInContextType = {
  loggedIn: boolean;
  setLoggedIn: React.Dispatch<React.SetStateAction<boolean>>;
};

const SessionContext = createContext<SessionContextType | null>(null);
const LoggedInContext = createContext<LoggedInContextType | null>(null);

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error("useSession must be used inside SessionContext.Provider");
  }

  return context;
}

export function useLoggedIn() {
  const context = useContext(LoggedInContext);

  if (!context) {
    throw new Error("Is Logged in must be valid");
  }

  return context;
}

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  const apiBaseUrl = "http://localhost:8080";

  const getInitialSessionToken = () => {
    const storedToken = localStorage.getItem("sessionToken")?.trim();

    if (
      !storedToken ||
      storedToken === "null" ||
      storedToken === "undefined" ||
      storedToken.startsWith("<!doctype html>") ||
      storedToken.startsWith("<html")
    ) {
      localStorage.removeItem("sessionToken");
      return null;
    }

    return storedToken;
  };

  const [sessionToken, setSessionToken] = useState<string | null>(
    getInitialSessionToken,
  );

  const [loggedIn, setLoggedIn] = useState<boolean>(
    localStorage.getItem("isLoggedIn") === "true",
  );
  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  useEffect(() => {
    async function fetchSessionToken() {
      try {
        const storedToken = localStorage.getItem("sessionToken")?.trim();

        if (
          storedToken &&
          storedToken !== "null" &&
          storedToken !== "undefined" &&
          !storedToken.startsWith("<!doctype html>") &&
          !storedToken.startsWith("<html")
        ) {
          setSessionToken(storedToken);
          return;
        }

        const response = await fetch(`${apiBaseUrl}/api/user/login/guest`, {
          method: "POST",
        });

        if (!response.ok) {
          throw new Error("Failed to create guest session");
        }

        const token = (await response.text()).trim();
        if (
          !token ||
          token.startsWith("<!doctype html>") ||
          token.startsWith("<html")
        ) {
          throw new Error("Guest session did not return a valid token");
        }

        setSessionToken(token);
        localStorage.setItem("sessionToken", token);
      } catch (err) {
        console.error("Failed to fetch session token:", err);
        localStorage.removeItem("sessionToken");
        setSessionToken(null);
      }
    }

    fetchSessionToken();
  }, []);

  if (!sessionToken) {
    return <div>Loading...</div>;
  }

  return (
    <BrowserRouter>
      <SessionContext.Provider value={{ sessionToken, setSessionToken }}>
        <LoggedInContext.Provider value={{ loggedIn, setLoggedIn }}>
          <div className={`app ${theme}`}>
            <Header theme={theme} setTheme={setTheme} />
            <AppRoutes />
          </div>
        </LoggedInContext.Provider>
      </SessionContext.Provider>
    </BrowserRouter>
  );
}
export default App;
