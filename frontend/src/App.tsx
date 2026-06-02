import { BrowserRouter } from "react-router-dom";
import { useEffect, useState } from "react";
import AppRoutes from "./AppRoutes";
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
