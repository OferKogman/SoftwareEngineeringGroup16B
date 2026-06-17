import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import "./CSS/CompanySettings.css";

export default function CompanySettings() {
  const { companyId } = useParams();

  const [, setConfirmationText] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();
  async function handleForfeitOwnership() {
    setMessage("");
    setError("");

    if (!companyId) {
      setError("Missing company ID.");
      return;
    }

    setIsSubmitting(true);

    try {
      console.warn("=================================");
      console.warn("MOCK FORFEIT OWNERSHIP");
      console.warn("REMOVE MOCK WHEN API READY");
      console.warn("=================================");

      setMessage("Mock success: ownership forfeited.");
      setConfirmationText("");

      /*
      const authToken = localStorage.getItem("authToken") || "";

      const response = await fetch(
        `${API_BASE}/production-companies/${companyId}/owners/me`,
        {
          method: "DELETE",
          headers: {
            Authorization: authToken,
          },
        },
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Failed to forfeit ownership.");
      }

      setMessage("Ownership successfully forfeited.");
      setConfirmationText("");
      */
      // route user back to main page
      navigate("/");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to forfeit ownership.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="company-settings-page">
      <header className="settings-header">
        <div>
          <h2>Company Settings</h2>
          <p>Manage company-level actions and ownership settings.</p>
        </div>
      </header>

      {message && (
        <p className="settings-alert settings-alert-success">{message}</p>
      )}
      {error && <p className="settings-alert settings-alert-error">{error}</p>}

      <section className="settings-card danger-zone">
        <div className="danger-zone-header">
          <div>
            <h3>Forfeit Ownership</h3>
            <p>
              This removes your ownership role from this company. You may lose
              access to owner-only actions.
            </p>
          </div>
        </div>

        <button
          className="danger-button"
          type="button"
          onClick={handleForfeitOwnership}
        >
          {isSubmitting ? "Forfeiting..." : "Forfeit Ownership"}
        </button>
      </section>
    </main>
  );
}
