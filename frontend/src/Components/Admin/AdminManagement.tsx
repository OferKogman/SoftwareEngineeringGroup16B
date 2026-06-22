import { useCallback, useEffect, useRef } from "react";
import { NavLink, useNavigate, useOutlet } from "react-router-dom";
import { useAdminLoggedIn } from "../../GlobalContext/AdminLoggedInContext";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";

export default function AdminManagement() {
  const outlet = useOutlet();
  const { loggedIn, setLoggedIn } = useLoggedIn();
  const { adminLoggedIn, setAdminLoggedIn } = useAdminLoggedIn();
  const { setSessionToken } = useSession();

  const apiFetch = useApiFetch();
  const navigate = useNavigate();

  const adminLoggedInRef = useRef(adminLoggedIn);

  useEffect(() => {
    adminLoggedInRef.current = adminLoggedIn;
  }, [adminLoggedIn]);

  const handleLogout = useCallback(async () => {
    try {
      const response = await apiFetch(
        "http://localhost:8080/api/user/login/logout",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
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
  }, [apiFetch, setLoggedIn, setSessionToken]);

  const handleAdminLogout = useCallback(async () => {
    try {
      const response = await apiFetch(
        "http://localhost:8080/api/admin/logout",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
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
      setAdminLoggedIn(false);
    } catch (err) {
      console.error("Failed to fetch session token, please logout again:", err);
    }
  }, [apiFetch, setAdminLoggedIn, setSessionToken]);

  useEffect(() => {
    if (loggedIn) {
      void handleLogout();
    }

    if (!adminLoggedIn) {
      navigate("login");
    }
  }, [loggedIn, adminLoggedIn, navigate, handleLogout]);

  useEffect(() => {
    return () => {
      if (adminLoggedInRef.current) {
        void handleAdminLogout();
      }
    };
  }, [handleAdminLogout]);

  return (
    <div className="management-page">
      <div className="management-header">
        <h1>Admin Management</h1>
      </div>

      <div className="management-body">
        <aside className="management-sidebar">
          {adminLoggedIn ? (
            <>
              <NavLink to="register">Register New Admin</NavLink>
              <NavLink to="purchase-history">Purchase History</NavLink>
              <NavLink to="users">Users</NavLink>
              <NavLink to="companies">Companies</NavLink>
            </>
          ) : (
            <NavLink to="login">Login</NavLink>
          )}
        </aside>

        <main className="management-content">
          {outlet ?? (
            <div className="management-default-content">
              <h2>Admin Management</h2>
              <p>Select an option from the sidebar.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
