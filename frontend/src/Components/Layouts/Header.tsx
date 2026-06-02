import { BiSearchAlt } from "react-icons/bi";
import { useNavigate } from "react-router-dom";
import { useLoggedIn, useSession } from "../../App";
import SystemLogo from "../../Assets/SystemLogo.png";
import ThemeToggle from "../Shared/ThemeToggle";
import "./CSS/Header.css";

type ThemeToggleProps = {
  theme: string;
  setTheme: React.Dispatch<React.SetStateAction<string>>;
};

export default function Header({ theme, setTheme }: ThemeToggleProps) {
  const { sessionToken, setSessionToken } = useSession();
  const { loggedIn, setLoggedIn } = useLoggedIn();
  const navigate = useNavigate();

  async function handleLogout() {
    try {
      const response = await fetch(
        "http://localhost:8080/api/user/login/logout",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: sessionToken,
          },
        },
      );

      if (!response.ok) {
        throw new Error("Failed to create guest session");
      }

      const token = await response.text();
      if (!token) {
        throw new Error("Guest session returned an empty token");
      }
      setSessionToken(token);
      setLoggedIn(false);
    } catch (err) {
      console.error("Failed to fetch session token, please logout again:", err);
    }
  }

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
        {!loggedIn && (
          <button className="login-button" onClick={() => navigate("/login")}>
            Login
          </button>
        )}
        {loggedIn && (
          <>
            <button className="login-button" onClick={() => navigate("/users")}>
              My Profile
            </button>
            <button className="login-button" onClick={() => handleLogout()}>
              logout
            </button>
          </>
        )}
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
