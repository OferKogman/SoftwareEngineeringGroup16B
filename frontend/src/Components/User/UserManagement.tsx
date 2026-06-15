import { NavLink, useOutlet } from "react-router-dom";
import "../../CSS/Management.css";

export default function UserManagement() {
  const outlet = useOutlet();

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
