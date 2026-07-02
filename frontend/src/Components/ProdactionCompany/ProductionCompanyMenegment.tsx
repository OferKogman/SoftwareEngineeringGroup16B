import { useEffect, useState } from "react";
import {
  NavLink,
  Navigate,
  useNavigate,
  useOutlet,
  useParams,
} from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import {
  type ManagerPermissions,
  type ProductionCompanyDTO,
} from "../../DTOs/ProductionCompanyDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/ProductionCompanyManagement.css";

const API_BASE = "http://localhost:8080";

function getApiError(data: unknown): string {
  if (typeof data === "string" && data.trim()) return data;

  if (data && typeof data === "object") {
    if ("message" in data && typeof data.message === "string") {
      return data.message;
    }
    if ("error" in data && typeof data.error === "string") {
      return data.error;
    }
  }

  return "";
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
  if (!text) return null;

  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

export default function ProductionCompanyManagement() {
  const navigate = useNavigate();
  const { companyId } = useParams();
  const { sessionToken } = useSession();

  const [company, setCompany] = useState<ProductionCompanyDTO | null>(null);
  const [perms, setPerms] = useState<ManagerPermissions[]>([]);
  const [owner, setOwner] = useState<boolean>(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [shouldRedirect, setShouldRedirect] = useState(false);

  const apiFetch = useApiFetch();

  function closePopup() {
    setError("");
    if (shouldRedirect) {
      navigate("/", { replace: true });
    }
  }

  const hasPermission = (p: ManagerPermissions) => perms.includes(p);

  const outlet = useOutlet({
    company,
    perms,
    owner,
    hasPermission,
  });

  useEffect(() => {
    let cancelled = false;

    async function loadCompany() {
      setLoading(true);
      setError("");
      setCompany(null);
      setShouldRedirect(false);

      const fail = (msg: string, redirect = false) => {
        if (cancelled) return;

        setError(msg);
        setShouldRedirect(redirect);
        setLoading(false);
      };

      if (!companyId) {
        fail("Missing company id");
        return;
      }

      if (!sessionToken) return;

      try {
        const response = await apiFetch(
          `${API_BASE}/production-companies/${companyId}`,
          { method: "GET" },
        );

        const data = await readResponseBody(response);
        if (cancelled) return;

        if (!response.ok) {
          const msg = getApiError(data);
          const redirect =
            msg.toLowerCase().includes("not part of the company") ||
            msg.toLowerCase().includes("not part");

          fail(msg, redirect);
          return;
        }

        if (!isProductionCompanyDTO(data)) {
          fail("Invalid company response from server");
          return;
        }

        setCompany(data);

        const response2 = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/me/owner`,
          { method: "GET" },
        );

        const ownerData = await readResponseBody(response2);
        if (cancelled) return;

        if (!response2.ok) {
          const msg = getApiError(ownerData);
          const redirect =
            msg.toLowerCase().includes("not part of the company") ||
            msg.toLowerCase().includes("not part");

          fail(msg, redirect);
          return;
        }

        if (typeof ownerData !== "boolean") {
          fail("Invalid owner response from server");
          return;
        }

        setOwner(ownerData);

        const response3 = await apiFetch(
          `${API_BASE}/production-companies/${companyId}/me/permissions`,
          { method: "GET" },
        );

        const permissionsData = await readResponseBody(response3);
        if (cancelled) return;

        if (!response3.ok) {
          const msg = getApiError(permissionsData);
          const redirect =
            msg.toLowerCase().includes("not part of the company") ||
            msg.toLowerCase().includes("not part");

          fail(msg, redirect);
          return;
        }

        if (!Array.isArray(permissionsData)) {
          fail("Invalid permissions response from server");
          return;
        }

        setPerms(permissionsData as ManagerPermissions[]);
      } catch (err) {
        if (!cancelled) {
          fail(err instanceof Error ? err.message : String(err));
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    loadCompany();

    return () => {
      cancelled = true;
    };
  }, [companyId, sessionToken, apiFetch]);

  if (!sessionToken) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="management-page">
      <div className="management-header">
        <h1>
          {loading
            ? "Loading company..."
            : company
              ? company.name
              : "Company unavailable"}
        </h1>
        <p>Company ID: {companyId ?? "Missing"}</p>
      </div>

      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}>OK</button>
        </div>
      )}

      <div className="management-body">
        <aside className="management-sidebar">
          {hasPermission("SALES_REPORT") && (
            <NavLink to="total-revenue">Total Revenue</NavLink>
          )}
          {hasPermission("VIEW_PURCHASE_HISTORY") && (
            <NavLink to="sales-history">Sales History</NavLink>
          )}
          {hasPermission("PURCHASE_POLICY") && (
            <NavLink to="discount-policy">Discount Policy</NavLink>
          )}
          {hasPermission("PURCHASE_POLICY") && (
            <NavLink to="purchase-policy">Purchase Policy</NavLink>
          )}
          {hasPermission("EVENT_INVENTORY") && (
            <NavLink to="events">Events</NavLink>
          )}
          {hasPermission("VENUE_CONFIGURATION") && (
            <NavLink to="venue-config">Create Venue</NavLink>
          )}
          {owner && <NavLink to="members">Members & Permissions</NavLink>}
          {owner && <NavLink to="hierarchy">Hierarchy Tree</NavLink>}
          {owner && <NavLink to="settings">Resignation</NavLink>}
        </aside>

        <main className="management-content">
          {loading ? (
            <div className="management-default-content">
              <h2>Loading...</h2>
              <p>Loading company data from the server.</p>
            </div>
          ) : (
            (outlet ?? (
              <div className="management-default-content">
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
