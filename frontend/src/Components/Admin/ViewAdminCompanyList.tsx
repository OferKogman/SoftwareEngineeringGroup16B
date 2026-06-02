import { useEffect, useState } from "react";
import type { ProductionCompanyDTO } from "../ProdactionCompany/ProdctionCompanyForm";

export default function ViewAdminCompanyList() {
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);

  useEffect(() => {
    async function loadCompanies() {
      try {
        const response = await fetch(`http://localhost:8080/companies`);

        if (!response.ok) {
          throw new Error("Failed to load companies.");
        }

        const event: ProductionCompanyDTO = await response.json();

        setCompanyDTOList([event]);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load companies.",
        );
      }
    }

    void loadCompanies();
  });

  if (!companyDTOList) {
    return <div>No companies found</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      {companyDTOList.length === 0 ? (
        <p>No companies found</p>
      ) : (
        <table>
          <thead>
            <tr>
              <th>ID</th>
              <th>Name</th>
              <th>Rating</th>
            </tr>
          </thead>

          <tbody>
            {companyDTOList.map((company) => (
              <tr key={company.productionCompanyID}>
                <td>{company.productionCompanyID}</td>
                <td>{company.name}</td>
                <td>{company.rating}</td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
}
