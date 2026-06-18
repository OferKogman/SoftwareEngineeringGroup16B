import { useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import "./CSS/Resignation.css";

export default function Resignation() {
  const { companyId } = useParams();

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  async function handleForfeitOwnership() {
    if (isSubmitting) return;

    setIsSubmitting(true);
    setMessage("");

    if (!companyId) {
      setError("Missing company ID.");
      setIsSubmitting(false);
      return;
    }

    try {
      const response = await apiFetch(
        `http://localhost:8080/production-companies/${companyId}/owners/me`,
        {
          method: "DELETE",
        },
      );

      if (!response.ok) {
        const text = await response.text();
        throw new Error(text || "Failed to forfeit ownership.");
      }

      setError("");
      setMessage("Ownership forfeited successfully.");

      setTimeout(() => {
        navigate("/");
      }, 3000);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to forfeit ownership.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <main className="resignation-page">
      <header className="settings-header">
        <div>
          <h2>Resignation</h2>
          <p>Say goodbye to your ownership settings!</p>
        </div>
      </header>

      <section className="settings-card">
        <div>
          <button
            onClick={handleForfeitOwnership}
            disabled={!!message || !!error || isSubmitting}
          >
            {isSubmitting ? "Forfeiting..." : "Forfeit Ownership"}
          </button>
          <p>
            This removes your ownership role from this company.
            <br /> You may lose access to owner-only actions.
          </p>
        </div>
      </section>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
    </main>
  );
}
