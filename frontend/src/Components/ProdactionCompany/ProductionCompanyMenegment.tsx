import { useEffect, useState } from "react";
import { NavLink, useOutlet, useParams } from "react-router-dom";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/ProductionCompanyManagement.css";

const API_BASE = "http://localhost:8080";

function getApiError(data: unknown): string {
  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data && typeof data === "object") {
    if ("message" in data && typeof data.message === "string") {
      return data.message;
    }

    if ("error" in data && typeof data.error === "string") {
      return data.error;
    }
  }

  return "Company not found";
}

function isProductionCompanyDTO(data: unknown): data is ProductionCompanyDTO {
  return (
    !!data &&
    typeof data === "object" &&
    "name" in data &&
    typeof data.name === "string"
  );
}

async function readResponseBody(response: Response): Promise<unknown> {
  const text = await response.text();

  if (!text) {
    return null;
  }

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export default function ProductionCompanyManagement() {
  const { companyId } = useParams();
  const { sessionToken } = useSession();
  const outlet = useOutlet();

  const [company, setCompany] = useState<ProductionCompanyDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let cancelled = false;

    async function loadCompany() {
      setLoading(true);
      setError("");
      setCompany(null);

      if (!companyId) {
        setError("Missing company id");
        setLoading(false);
        return;
      }

      if (!sessionToken) {
        return;
      }

      try {
        const response = await fetch(
          `${API_BASE}/production-companies/${companyId}`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: sessionToken,
            },
          },
        );

        const data = await readResponseBody(response);

        if (cancelled) {
          return;
        }

        if (!response.ok) {
          throw new Error(getApiError(data));
        }

        if (!isProductionCompanyDTO(data)) {
          throw new Error("Invalid company response from server");
        }

        setCompany(data);
      } catch (error) {
        if (!cancelled) {
          setError(
            error instanceof Error ? error.message : "Company not found",
          );
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadCompany();

    return () => {
      cancelled = true;
    };
  }, [companyId, sessionToken]);

  return (
    <div className="company-management-page">
      <div className="company-management-header">
        <h1>
          {loading
            ? "Loading company..."
            : company
              ? company.name
              : "Company unavailable"}
        </h1>
        <p>Company ID: {companyId ?? "Missing"}</p>
      </div>

      {error && <p className="form-error">{error}</p>}

      <div className="company-management-body">
        <aside className="company-management-sidebar">
          <NavLink to="total-revenue">Total Revenue</NavLink>
          <NavLink to="sales-history">Sales History</NavLink>
          <NavLink to="events">Events</NavLink>
          <NavLink to="venue-config">Create Venue</NavLink>
          <NavLink to="members">Members & Permissions</NavLink>
          <NavLink to="hierarchy">Hierarchy Tree</NavLink>
          <NavLink to="settings">Company Settings</NavLink>
        </aside>

        <main className="company-management-content">
          {loading ? (
            <div className="company-management-default-content">
              <h2>Loading...</h2>
              <p>Loading company data from the server.</p>
            </div>
          ) : error ? (
            <div className="company-management-default-content">
              <h2>Cannot load company management</h2>
              <p>{error}</p>
            </div>
          ) : (
            (outlet ?? (
              <div className="company-management-default-content">
                <h2>Company Management</h2>
                <p>Select an option from the sidebar.</p>
              </div>
            ))
          )}
        </main>
      </div>
    </div>
  );
}
