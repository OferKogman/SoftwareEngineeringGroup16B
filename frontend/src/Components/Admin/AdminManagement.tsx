import { useState } from "react";
import { useNavigate } from "react-router-dom";
import AdminRegisterForm from "./AdminRegisterForm";
import ViewAdminCompanyList from "./ViewAdminCompanyList";
import ViewAdminPurchaseHistory from "./ViewAdminPurchaseHistory";
import ViewUsersList from "./ViewUsersList";

export default function AdminManagement() {
  const navigate = useNavigate();

  const [showAdminRegisterForm, setShowAdminRegisterForm] = useState(false);
  const [showUsersList, setShowUsersList] = useState(false);
  const [showPurchaseHistory, setShowPurchaseHistory] = useState(false);
  const [showCompanies, setShowCompanies] = useState(false);

  return (
    <div>
      <h1>Admin Management</h1>

      <div>
        <button
          onClick={() => setShowAdminRegisterForm(!showAdminRegisterForm)}
        >
          Register New Admin
        </button>
      </div>

      {showAdminRegisterForm && (
        <AdminRegisterForm title="Register New Admin" />
      )}

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
        }}
      >
        <button onClick={() => setShowUsersList(!showUsersList)}>
          {showUsersList ? "▼" : "▶"} Users
        </button>
        {showUsersList && <ViewUsersList />}
      </div>

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
