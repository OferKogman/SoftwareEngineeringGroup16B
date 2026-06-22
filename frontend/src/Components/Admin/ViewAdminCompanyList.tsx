import { useEffect, useState } from "react";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import { useApiFetch } from "../../apiFetch";

export default function ViewAdminCompanyList() {
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);

  const apiFetch = useApiFetch();

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
          throw new Error("Failed to load companies.");
        }

        const companies: ProductionCompanyDTO[] = await response.json();

        setCompanyDTOList(companies);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load companies.",
        );
      }
    }

    void loadCompanies();
  }, [apiFetch]);

  if (!companyDTOList) {
    return <div>No companies found</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      {companyDTOList.length === 0 ? (
        <p>No companies found</p>
      ) : (
        <table
          style={{
            width: "75%",
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
                  <button onClick={() => {}}>Close Company</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
