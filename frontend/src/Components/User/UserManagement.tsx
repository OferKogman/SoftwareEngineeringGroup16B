import { NavLink, useOutlet } from "react-router-dom";
import "./CSS/UserManagement.css";

export default function UserManagement() {
  const outlet = useOutlet();

  return (
    <div className="user-management-page">
      <div className="user-management-header">
        <h1>User Management</h1>
      </div>

      <div className="user-management-body">
        <aside className="user-management-sidebar">
          <NavLink to="change-password">Change Password</NavLink>
          <NavLink to="purchase-history">Purchase History</NavLink>
          <NavLink to="companies">My Production Companies</NavLink>
        </aside>

        <main className="user-management-content">
          {outlet ?? (
            <div className="user-management-default-content">
              <h2>User Management</h2>
              <p>Select an option from the sidebar.</p>
            </div>
          )}
        </main>
      </div>
    </div>
  );
}
