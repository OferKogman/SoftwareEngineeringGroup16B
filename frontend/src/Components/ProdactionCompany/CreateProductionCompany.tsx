// src/Components/CreateProdactionCompany.tsx

import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSession } from "../../App";

const API_BASE = "http://localhost:8080";

export default function CreateProdactionCompany() {
  const navigate = useNavigate();

  const [companyName, setCompanyName] = useState("");
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { sessionToken } = useSession();

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
      const response = await fetch(`${API_BASE}/production-companies`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: sessionToken,
        },
        body: JSON.stringify({
          companyName: companyName.trim(),
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Failed to create production company.");
      }

      setMessage("Production company created successfully.");
      setCompanyName("");

      const createdCompany = data.data ?? data;

      if (createdCompany.companyId || createdCompany.id) {
        const newCompanyId = createdCompany.companyId ?? createdCompany.id;
        navigate(`/production-company/${newCompanyId}`);
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
      <h1>Create Production Company</h1>

      {error && <p style={{ color: "red" }}>{error}</p>}
      {message && <p style={{ color: "green" }}>{message}</p>}

      <form onSubmit={handleCreateCompany}>
        <div>
          <label>Company Name</label>
          <br />

          <input
            type="text"
            value={companyName}
            onChange={(event) => setCompanyName(event.target.value)}
            placeholder="Enter production company name"
          />
        </div>

        <br />

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create Company"}
        </button>
      </form>
    </div>
  );
}
