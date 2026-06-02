import { useParams } from "react-router-dom";
import { useState } from "react";
import "./CSS/CompanySettings.css";

const API_BASE = "http://localhost:8080";

export default function CompanySettings() {
  const { companyId } = useParams();

  const [confirmationText, setConfirmationText] = useState("");
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  async function handleForfeitOwnership() {
    setMessage("");
    setError("");

    if (confirmationText !== "FORFEIT") {
      setError('Type "FORFEIT" before continuing.');
      return;
    }

    console.warn("=================================");
    console.warn("MOCK FORFEIT OWNERSHIP");
    console.warn("REMOVE MOCK WHEN API READY");
    console.warn("=================================");

    setMessage(
      "Mock success: ownership forfeited."
    );

    /*
    const authToken =
      localStorage.getItem("authToken") || "";

    const response = await fetch(
      `${API_BASE}/production-companies/${companyId}/owners/me`,
      {
        method: "DELETE",
        headers: {
          Authorization: authToken,
        },
      }
    );

    const data = await response.json();

    if (!response.ok) {
      setError(data.message);
      return;
    }

    setMessage(
      "Ownership successfully forfeited."
    );
    */
  }

  return (
    <div className="company-settings-page">
      <h2>Company Settings</h2>

      {message && (
        <p className="settings-success">
          {message}
        </p>
      )}

      {error && (
        <p className="settings-error">
          {error}
        </p>
      )}

      <div className="danger-zone">
        <h3>Danger Zone</h3>

        <p>
          Forfeiting ownership removes your
          ownership role in this company.
        </p>

        <p>
          Type <strong>FORFEIT</strong> to
          continue.
        </p>

        <input
          type="text"
          value={confirmationText}
          onChange={(event) =>
            setConfirmationText(
              event.target.value
            )
          }
          placeholder="FORFEIT"
        />

        <button
          className="danger-button"
          onClick={handleForfeitOwnership}
        >
          Forfeit Ownership
        </button>
      </div>
    </div>
  );
}