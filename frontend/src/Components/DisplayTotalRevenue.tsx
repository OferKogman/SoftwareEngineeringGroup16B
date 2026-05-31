import { useState } from "react";

export default function DisplayTotalRevenue() {
  const [companyID, setCompanyID] = useState("");
  const [totalRevenue, setTotalRevenue] = useState<number | null>(null);
  const [error, setError] = useState("");

  async function handleDisplayRevenue() {
    try {
      setError("");

      const companyId = Number(companyID);

      if (Number.isNaN(companyId)) {
        throw new Error("Invalid company ID.");
      }

      // FAKE API CALL
      const revenue = await getTotalRevenue(companyId);

      setTotalRevenue(revenue);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : `Failed to get company revenue for company ${companyID}.`
      );
    }
  }

  return (
    <div>
      <h2>Display Total Revenue</h2>

      <input
        type="number"
        placeholder="Company ID"
        value={companyID}
        onChange={(e) => setCompanyID(e.target.value)}
      />

      <button onClick={handleDisplayRevenue}>
        Display Revenue
      </button>

      {error && <p>{error}</p>}

      {totalRevenue !== null && (
        <p>Total Revenue: ${totalRevenue.toFixed(2)}</p>
      )}
    </div>
  );
}


async function getTotalRevenue(
  companyID: number
): Promise<number> {
  console.log("Getting revenue for company:", companyID);

  return Promise.resolve(1234567.89);
}