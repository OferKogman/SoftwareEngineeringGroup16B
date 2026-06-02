import { useState } from "react";
import { useNavigate } from "react-router-dom";
import ViewUserCompanyList from "../ViewUserCompanyList";
import ViewUserPurchaseHistory from "../ViewUserPurchaseHistory";
import ChangePasswordForm from "./ChangePasswordForm";

export default function UserManagement() {
  const navigate = useNavigate();

  const [showChangePassword, setShowChangePassword] = useState(false);
  const [showPurchaseHistory, setShowPurchaseHistory] = useState(false);
  const [showCompanies, setShowCompanies] = useState(false);

  return (
    <div>
      <h1>User Management</h1>

      <div>
        <button onClick={() => setShowChangePassword(!showChangePassword)}>
          Change Password
        </button>
      </div>

      {showChangePassword && <ChangePasswordForm title="Change Password" />}

      {/* Purchase History */}
      <div>
        <button onClick={() => setShowPurchaseHistory(!showPurchaseHistory)}>
          {showPurchaseHistory ? "▼" : "▶"} Purchase History
        </button>

        {showPurchaseHistory && (
          <div>
            <ViewUserPurchaseHistory />
          </div>
        )}
      </div>

      {/* Companies */}
      <div>
        <button onClick={() => setShowCompanies(!showCompanies)}>
          {showCompanies ? "▼" : "▶"} My Production Companies
        </button>

        {showCompanies && (
          <div style={{ marginTop: "10px" }}>
            <button onClick={() => navigate("/companies/create")}>
              Create Company
            </button>

            <ViewUserCompanyList />
          </div>
        )}
      </div>
    </div>
  );
}
