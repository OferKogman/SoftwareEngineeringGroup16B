import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ViewUsersList from "../ViewUsersList";
import ViewAdminCompanyList from "./ViewAdminCompanyList";
import ViewAdminPurchaseHistory from "./ViewAdminPurchaseHistory";

export default function AdminManagement() {
  const navigate = useNavigate();

  const [showUsersList, setShowUsersList] = useState(false);
  const [showPurchaseHistory, setShowPurchaseHistory] = useState(false);
  const [showCompanies, setShowCompanies] = useState(false);

  return (
    <div>
      <h1>Admin Management</h1>

      <div>
        <button onClick={() => setShowUsersList(!showUsersList)}>
          {showUsersList ? "▼" : "▶"} Users
        </button>
      </div>

      {showUsersList && <ViewUsersList />}

      <div>
        <button onClick={() => setShowPurchaseHistory(!showPurchaseHistory)}>
          {showPurchaseHistory ? "▼" : "▶"} Purchase History
        </button>

        {showPurchaseHistory && (
          <div>
            <ViewAdminPurchaseHistory />
          </div>
        )}
      </div>

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <button onClick={() => setShowCompanies(!showCompanies)}>
          {showCompanies ? "▼" : "▶"} Production Companies
        </button>

        {showCompanies && (
          <div style={{ marginTop: "10px" }}>
            <ViewAdminCompanyList />
          </div>
        )}
      </div>
    </div>
  );
}
