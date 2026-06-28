import { useState, useRef, useEffect } from "react";
import { BiSearchAlt, BiBell } from "react-icons/bi";
import { useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import SystemLogo from "../../Assets/SystemLogo.png";
import { useAdminLoggedIn } from "../../GlobalContext/AdminLoggedInContext";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useNotifications } from "../Notification/NotificationContext";
import ThemeToggle from "../Shared/ThemeToggle";
import "./CSS/Header.css";

type ThemeToggleProps = {
  theme: string;
  setTheme: React.Dispatch<React.SetStateAction<string>>;
};

export default function Header({ theme, setTheme }: ThemeToggleProps) {
  const { setSessionToken } = useSession();
  const { loggedIn, setLoggedIn } = useLoggedIn();
  const { adminLoggedIn, setAdminLoggedIn } = useAdminLoggedIn();
  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  const { inbox, unreadCount, markInboxAsRead, clearInbox } = useNotifications();
  const [isBellOpen, setIsBellOpen] = useState(false);
  const bellRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (bellRef.current && !bellRef.current.contains(event.target as Node)) {
        setIsBellOpen(false);
      }
    }
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  function toggleBell() {
    setIsBellOpen(!isBellOpen);
    if (!isBellOpen && unreadCount > 0) {
      markInboxAsRead();
    }
  }

  async function handleLogout() {
    try {
      const response = await apiFetch("http://localhost:8080/api/user/login/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) throw new Error("Failed to create guest session");
      
      const token = await response.text();
      if (!token) throw new Error("Guest session returned an empty token");
      
      setSessionToken(token);
      setLoggedIn(false);
    } catch (err) {
      console.error("Failed to fetch session token, please logout again:", err);
    }
  }

  async function handleAdminLogout() {
    try {
      const response = await apiFetch("http://localhost:8080/api/admin/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
      });

      if (!response.ok) throw new Error("Failed to create guest session");

      const token = await response.text();
      if (!token) throw new Error("Guest session returned an empty token");

      setSessionToken(token);
      setAdminLoggedIn(false);
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
        {!loggedIn && !adminLoggedIn && (
          <button className="login-button" onClick={() => navigate("/login")}>
            Login
          </button>
        )}
        
        {loggedIn && (
          <>
            <div className="notification-bell-wrapper" ref={bellRef}>
              <button className="bell-button" onClick={toggleBell}>
                <BiBell className="bell-icon" />
                {unreadCount > 0 && (
                  <span className="bell-badge">{unreadCount}</span>
                )}
              </button>

              {isBellOpen && (
                <div className={`bell-dropdown ${theme}`}>
                  <div className="bell-dropdown-header">
                    <h4>Notifications</h4>
                    {inbox.length > 0 && (
                      <button className="bell-clear-btn" onClick={clearInbox}>
                        Clear All
                      </button>
                    )}
                  </div>
                  
                  {inbox.length === 0 ? (
                    <p className="bell-empty-text">No new notifications.</p>
                  ) : (
                    inbox.map((notif) => (
                      <div 
                        key={notif.id} 
                        className={`inbox-item notify-${notif.type}`}
                      >
                        {notif.message}
                      </div>
                    ))
                  )}
                </div>
              )}
            </div>

            <button className="login-button" onClick={() => navigate("/users/management")}>
              My Profile
            </button>
            <button className="login-button" onClick={() => handleLogout()}>
              logout
            </button>
          </>
        )}
        {adminLoggedIn && (
          <button className="login-button" onClick={() => handleAdminLogout()}>
            logout
          </button>
        )}
        
        <button className="search-button" onClick={() => navigate("/events/search")}>
          <BiSearchAlt className="search-icon" />
        </button>
      </div>
    </header>
  );
}