import { NavLink, Outlet, useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import "./CSS/ProductionCompanyManagement.css";

export default function ProductionCompanyManagement() {
  const { companyId } = useParams();

const [companyName, setCompanyName] = useState("Loading...");

  useEffect(() => {
  if (!companyId) {
    return;
  }

  console.warn("=================================");
  console.warn("USING TEMPORARY MOCK DATA");
  console.warn("REMOVE WHEN API IS READY");
  console.warn("=================================");

  setCompanyName("Rock Productions Ltd.");

  /*
  const API_BASE = "http://localhost:8080";

  async function loadCompany() {
    try {
      const response = await fetch(
        `${API_BASE}/production-companies/${companyId}`
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message);
      }

      setCompanyName(data.data.companyName);
    } catch (error) {
      console.error(error);
      setCompanyName("Unknown Company");
    }
  }

  loadCompany();
  */
}, [companyId]);

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
