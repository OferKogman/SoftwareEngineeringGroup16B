// src/Components/CreateProductionCompany.tsx

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";

const API_BASE = "http://localhost:8080";

export default function CreateProductionCompany() {
  const navigate = useNavigate();

  const [companyName, setCompanyName] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const apiFetch = useApiFetch();

  async function handleCreateCompany(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");
    setMessage("");

    if (!companyName.trim()) {
      setError("Please enter a production company name.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await apiFetch(`${API_BASE}/production-companies`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          companyName: companyName.trim(),
        }),
      });

      if (!response.ok) {
        throw new Error(
          (await response.text()) || "Failed to create production company.",
        );
      }
      const data = await response.json();

      setMessage("Production company created successfully.");
      setCompanyName("");

      const createdCompany = data.data ?? data;

      if (createdCompany.companyId || createdCompany.id) {
        const newCompanyId = createdCompany.companyId ?? createdCompany.id;
        navigate(`/companies/${newCompanyId}`);
      }
    } catch (err: unknown) {
      if (err instanceof Error) {
        setError(err.message);
      } else {
        setError("Something went wrong.");
      }
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <div style={{ padding: "24px" }}>
      {error && <p style={{ color: "red" }}>{error}</p>}
      {message && <p style={{ color: "green" }}>{message}</p>}

      <form onSubmit={handleCreateCompany}>
        <div>
          <p
            style={{
              fontSize: "1.25rem",
              fontWeight: "bold",
              marginBottom: "4px",
            }}
          >
            Create New Production Company
          </p>
          <label style={{ marginRight: "8px" }}>Company Name</label>

          <input
            style={{ padding: "4px", width: "12rem", marginBottom: "8px" }}
            type="text"
            value={companyName}
            onChange={(event) => setCompanyName(event.target.value)}
            placeholder="Enter production company name"
          />
        </div>

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create Company"}
        </button>
      </form>
    </div>
  );
}
