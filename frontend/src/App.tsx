import { useEffect, useState } from "react";
import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

import NotificationsContainer from "./Components/Notification/NotificationContainer";
import { NotificationProvider } from "./Components/Notification/NotificationContext";
import { LoggedInProvider } from "./GlobalContext/LoggedInProvider";
import { SessionProvider } from "./GlobalContext/SessionProvider";

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");

  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <BrowserRouter>
      <SessionProvider>
        <LoggedInProvider>
          <NotificationProvider>
            <div className={`app ${theme}`}>
              <NotificationsContainer />
              <Header theme={theme} setTheme={setTheme} />
              <AppRoutes />
            </div>
          </NotificationProvider>
        </LoggedInProvider>
      </SessionProvider>
    </BrowserRouter>
  );
}
export default App;
