import { BiSearchAlt } from "react-icons/bi";
import { useNavigate } from "react-router-dom";
import SystemLogo from "../../Assets/SystemLogo.png";
import ThemeToggle from "../Shared/ThemeToggle";
import "./CSS/Header.css";

type ThemeToggleProps = {
  theme: string;
  setTheme: React.Dispatch<React.SetStateAction<string>>;
};

export default function Header({ theme, setTheme }: ThemeToggleProps) {
  const navigate = useNavigate();
  return (
    <header className="header">
      <div className="header-toggle">
        <ThemeToggle theme={theme} setTheme={setTheme} />
      </div>
      <img
        src={SystemLogo}
        alt="Header Logo"
        className="header-logo"
        onClick={() => navigate("/")}
      />
      <div className="header-actions">
        <button className="login-button" onClick={() => navigate("/login")}>
          Login
        </button>
        <button
          className="search-button"
          onClick={() => navigate("/events/search")}
        >
          <BiSearchAlt className="search-icon" />
        </button>
      </div>
    </header>
  );
}
