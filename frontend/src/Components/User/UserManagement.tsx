import { NavLink, useOutlet } from "react-router-dom";
import "../../CSS/Management.css";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";

export default function UserManagement() {
  const outlet = useOutlet();
  const isLoggedIn = useLoggedIn();

  if (!isLoggedIn.loggedIn) {
    return (
      <div className="management-page">
        <div className="management-header">
          <h1>User Management</h1>
        </div>
        <div className="management-body">
          <div className="management-content">
            <h2>Access Denied</h2>
            <p>You must be logged in to access this page.</p>
          </div>
        </div>
        s
      </div>
    );
  }

  return (
    <div className="management-page">
      <div className="management-header">
        <h1>User Management</h1>
      </div>

      <div className="management-body">
        <aside className="management-sidebar">
          <NavLink to="change-password">Change Password</NavLink>
          <NavLink to="purchase-history">Purchase History</NavLink>
          <NavLink to="companies">My Production Companies</NavLink>
        </aside>

        <main className="management-content">
          {outlet ?? (
            <div className="management-default-content">
              <h2>User Management</h2>
              <p>Select an option from the sidebar.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
