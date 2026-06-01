import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import ProductionCompanyPurchaseHistory from "./ViewProductionCompanyPurchaseHistory";

const API_BASE = "http://10.100.102.14:8080";

export default function ProductionCompanyMenegment() {
    const { companyId } = useParams<{ companyId: string }>();
    console.log("ProductionCompanyMenegment rendered, companyId =", companyId);


    const [loadingRevenue, setLoadingRevenue] = useState<boolean>(true);
    const [totalRevenue, setTotalRevenue] = useState<number | null>(null);
    const [error, setError] = useState("");

  const authToken = localStorage.getItem("authToken") || "";

  useEffect(() => {
  async function loadTotalRevenue() {
    if (!companyId) {
      setError("Missing production company ID.");
      setLoadingRevenue(false);
      return;
    }

    try {
      setLoadingRevenue(true);

        const authToken = localStorage.getItem("authToken") || "";

        const response = await fetch(`${API_BASE}/production-companies/${companyId}/total-revenue`,{method: "GET",headers: {Authorization: authToken, },});

      if (!response.ok) {
        throw new Error(await response.text());
      }

      const data = await response.json();

      setTotalRevenue(data.data ?? data);
      setError("");
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to load total revenue."
      );

      // TEMPORARY fallback so the page does not stay stuck while backend is broken
      console.warn("USING TEMPORARY MOCK REVENUE. REMOVE LATER.");
      setTotalRevenue(1650);
    } finally {
      setLoadingRevenue(false);
    }
  }

  void loadTotalRevenue();
}, [companyId]);
  if (!companyId) {
    return <p className="form-error">Missing production company ID.</p>;
  }

  return (
    <div>
      <h1>Production Company Management</h1>

      {error && <p className="form-error">{error}</p>}

      <section>
  <h2>Total Revenue</h2>

  {loadingRevenue ? (
    <p>Loading revenue...</p>
  ) : (
    <p>{totalRevenue}</p>
  )}
</section>

      <ProductionCompanyPurchaseHistory productionCompanyID={companyId} />
    </div>
  );
}