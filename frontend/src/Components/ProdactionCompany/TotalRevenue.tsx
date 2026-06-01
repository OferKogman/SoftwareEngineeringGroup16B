import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import "./CSS/TotalRevenue.css";

export default function TotalRevenue() {
  const { companyId } = useParams();

  const [revenue, setRevenue] = useState<number>(0);

  useEffect(() => {
    console.warn("=================================");
    console.warn("USING TEMPORARY MOCK DATA");
    console.warn("FOR TotalRevenue");
    console.warn("REMOVE THIS MOCK");
    console.warn("WHEN BACKEND IS READY");
    console.warn("=================================");

    // MOCK DATA
    setRevenue(125430.75);

    /*
    const API_BASE = "http://localhost:8080";
    const authToken = localStorage.getItem("authToken") || "";

    async function loadRevenue() {
      try {
        const response = await fetch(
          `${API_BASE}/production-companies/${companyId}/total-revenue`,
          {
            method: "GET",
            headers: {
              Authorization: authToken,
            },
          }
        );

        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.message);
        }

        setRevenue(data.data);
      } catch (error) {
        console.error(error);
      }
    }

    loadRevenue();
    */
  }, [companyId]);

  return (
  <div className="total-revenue-page">
    <h2 className="total-revenue-title">
      Total Revenue
    </h2>

    <div className="total-revenue-card">
      <div className="total-revenue-label">
        Total Revenue Generated
      </div>

      <h1 className="total-revenue-value">
        ₪ {revenue.toLocaleString()}
      </h1>

      <div className="total-revenue-company">
        Company ID: {companyId}
      </div>
    </div>

    <div className="revenue-stats-grid">
      <div className="revenue-stat-card">
        <h3>Completed Orders</h3>
        <p>438</p>
      </div>

      <div className="revenue-stat-card">
        <h3>Events</h3>
        <p>12</p>
      </div>

      <div className="revenue-stat-card">
        <h3>Average Order</h3>
        <p>₪286</p>
      </div>
    </div>
  </div>
);
}

