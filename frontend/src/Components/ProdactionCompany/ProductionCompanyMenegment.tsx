import { NavLink, Outlet, useParams } from "react-router-dom";
import "./CSS/ProductionCompanyManagement.css";

export default function ProductionCompanyManagement() {
  const { companyId } = useParams();

  // later we'll fetch this from backend
  const companyName = "Company Name";

  return (
    <div className="company-management-page">
      <div className="company-management-header">
        <h1>{companyName}</h1>
        <p>Company ID: {companyId}</p>
      </div>

      <div className="company-management-body">
        <aside className="company-management-sidebar">
          <NavLink to="total-revenue">Total Revenue</NavLink>

          <NavLink to="sales-history">Sales History</NavLink>

          <NavLink to="venue-config">Event Venue Configuration</NavLink>

          <NavLink to="members">Members & Permissions</NavLink>

          <NavLink to="hierarchy">Hierarchy Tree</NavLink>

          <NavLink to="settings">Company Settings</NavLink>
        </aside>

        <main className="company-management-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}
