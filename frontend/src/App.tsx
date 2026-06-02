import { createContext, useContext, useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

type SessionContextType = {
  sessionToken: string;
  setSessionToken: React.Dispatch<React.SetStateAction<string | null>>;
};

const SessionContext = createContext<SessionContextType | null>(null);

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error("useSession must be used inside SessionContext.Provider");
  }

  return context;
}

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  const [sessionToken, setSessionToken] = useState<string | null>(
    localStorage.getItem("sessionToken"),
  );
  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  useEffect(() => {
    if (sessionToken) return;

    async function fetchSessionToken() {
      try {
        const response = await fetch("/api/user/login/guest");

        if (!response.ok) {
          throw new Error("Failed to create guest session");
        }

        const token = await response.text();
        if (!token) {
          throw new Error("Guest session returned an empty token");
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
  });

  useEffect(() => {
    if (sessionToken) {
      localStorage.setItem("sessionToken", sessionToken);
    } else {
      localStorage.removeItem("sessionToken");
    }
  }, [sessionToken]);

  if (!sessionToken) {
    return <div>Loading...</div>;
  }

  return (
    <BrowserRouter>
      <SessionContext.Provider value={{ sessionToken, setSessionToken }}>
        <div className={`app ${theme}`}>
          <Header theme={theme} setTheme={setTheme} />
          <AppRoutes />
        </div>
      </SessionContext.Provider>
    </BrowserRouter>
  );
}
export default App;
