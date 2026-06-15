import { useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

import NotificationsContainer from "./Components/Notification/NotificationContainer";
import { NotificationProvider } from "./Components/Notification/NotificationContext";
import { LoggedInContext } from "./GlobalContext/LoggedInContext";
import { SessionContext } from "./GlobalContext/SessionContext";

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");

  const [sessionToken, setSessionToken] = useState<string | null>(
    localStorage.getItem("sessionToken"),
  );
  const [isSessionLoading, setIsSessionLoading] = useState(true);
  const [sessionError, setSessionError] = useState<string | null>(null);

  const [loggedIn, setLoggedIn] = useState<boolean>(
    localStorage.getItem("isLoggedIn") === "true",
  );

  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  useEffect(() => {
    if (sessionToken) {
      localStorage.setItem("sessionToken", sessionToken);
    } else {
      localStorage.removeItem("sessionToken");
    }
  }, [sessionToken]);

  useEffect(() => {
    localStorage.setItem("isLoggedIn", String(loggedIn));
  }, [loggedIn]);

  useEffect(() => {
    async function fetchSessionToken() {
      try {
        const response = await fetch(
          "http://localhost:8080/api/user/login/guest/validate",
          {
            method: "POST",
            headers: {
              "Content-Type": "application/json",
              ...(localStorage.getItem("sessionToken")
                ? {
                    Authorization: localStorage.getItem(
                      "sessionToken",
                    ) as string,
                  }
                : {}),
            },
          },
        );

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
        setSessionError(null);
      } catch (err) {
        console.error("Failed to fetch session token:", err);
        localStorage.removeItem("sessionToken");
        localStorage.setItem("isLoggedIn", "false");
        setLoggedIn(false);
        setSessionToken(null);
        setSessionError(
          "Server is unavailable. Could not create a guest session.",
        );
      } finally {
        setIsSessionLoading(false);
      }
    }

    fetchSessionToken();
  }, []);

  if (isSessionLoading) {
    return <div>Loading...</div>;
  }

  if (sessionError || !sessionToken) {
    return <div>{sessionError ?? "No session token available."}</div>;
  }

  return (
    <BrowserRouter>
      <SessionContext.Provider value={{ sessionToken, setSessionToken }}>
        <LoggedInContext.Provider value={{ loggedIn, setLoggedIn }}>
          <NotificationProvider>
            <div className={`app ${theme}`}>
              <NotificationsContainer />

              <Header theme={theme} setTheme={setTheme} />
              <AppRoutes />
            </div>
          </NotificationProvider>
        </LoggedInContext.Provider>
      </SessionContext.Provider>
    </BrowserRouter>
  );
}
export default App;
