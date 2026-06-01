import { BrowserRouter } from "react-router-dom";
import AppRoutes from "./AppRoutes";

import { useEffect, useState } from "react";
import Header from "./Components/Layouts/Header";
import "./CSS/App.css";

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  return (
    <BrowserRouter>
      <div className={`app ${theme}`}>
        <Header theme={theme} setTheme={setTheme} />
        <AppRoutes />
      </div>
    </BrowserRouter>
  );
}
export default App;
