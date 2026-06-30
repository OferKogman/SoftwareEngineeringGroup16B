import { useEffect, useState } from "react";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import { useApiFetch } from "../../apiFetch";

export default function ViewAdminCompanyList() {
  const [error, setError] = useState<string>("");
  const [message, setMessage] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<ProductionCompanyDTO[]>(
    [],
  );
  const [isLoading, setIsLoading] = useState(true);
  const [closingCompanyID, setClosingCompanyID] = useState<number | null>(null);

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  async function handleCloseCompany(companyID: number) {
    setClosingCompanyID(companyID);
    setMessage("");
    setError("");

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/admin-management/deleteProductionCompany/${companyID}`,
        {
          method: "DELETE",
        },
      );
      const responseText = await response.text();
      if (!response.ok) {
        throw new Error(responseText);
      }
      setCompanyDTOList((prev) =>
        prev.filter((company) => company.id !== companyID),
      );
      setMessage(responseText || "Company closed successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setClosingCompanyID(null);
    }
  }

  useEffect(() => {
    async function loadCompanies() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/api/admin-management/companies`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const companies: ProductionCompanyDTO[] = await response.json();

        setCompanyDTOList(companies);
      } catch (err) {
        setError(err instanceof Error ? err.message : "");
      } finally {
        setIsLoading(false);
      }
    }

    void loadCompanies();
  }, [apiFetch]);

  if (isLoading) {
    return <p>Loading companies...</p>;
  }

  return (
    <div>
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

      {companyDTOList.length === 0 ? (
        <p>No companies found</p>
      ) : (
        <table
          style={{
            width: "90%",
            tableLayout: "fixed",
            marginLeft: "auto",
            marginRight: "auto",
          }}
        >
          <thead>
            <tr>
              <th style={{ width: "20%" }}>ID</th>
              <th style={{ width: "25%" }}>Name</th>
              <th style={{ width: "20%" }}>Rating</th>
              <th style={{ width: "10%" }}>Actions</th>
            </tr>
          </thead>

          <tbody>
            {companyDTOList.map((company) => (
              <tr key={company.id}>
                <td>{company.id}</td>
                <td>{company.name}</td>
                <td>{company.rating}</td>
                <td>
                  <button
                    disabled={closingCompanyID === company.id}
                    onClick={() => handleCloseCompany(company.id)}
                  >
                    {closingCompanyID === company.id
                      ? "Closing..."
                      : "Close Company"}
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
