import { useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

import NotificationsContainer from "./Components/Notification/NotificationContainer";
import { NotificationProvider } from "./Components/Notification/NotificationContext";
import { useGlobalNotifications } from "./Components/Notification/useGlobalNotifications";
import { AdminLoggedInProvider } from "./GlobalContext/AdminLoggedInProvider";
import { LoggedInProvider } from "./GlobalContext/LoggedInProvider";
import { SessionProvider } from "./GlobalContext/SessionProvider";

function GlobalBroadcastListener() {
  useGlobalNotifications();
  return null;
}

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");

  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
      <BrowserRouter>
        <SessionProvider>
          <LoggedInProvider>
            <AdminLoggedInProvider>
              <NotificationProvider>
                <GlobalBroadcastListener />
                <div className={`app ${theme}`}>
                  <NotificationsContainer />
                  <Header theme={theme} setTheme={setTheme} />
                  <AppRoutes />
                </div>
              </NotificationProvider>
            </AdminLoggedInProvider>
          </LoggedInProvider>
        </SessionProvider>
      </BrowserRouter>
  );
}

export default App;