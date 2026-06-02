import { useEffect, useState } from "react";
import { useSession } from "../../App";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";

export default function ViewAdminCompanyList() {
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);
  const { sessionToken } = useSession();

  useEffect(() => {
    async function loadCompanies() {
      try {
        const response = await fetch(
          `http://localhost:8080/api/admin-management/companies`,
          {
            method: "GET",
            headers: {
              "Content-Type": "application/json",
              Authorization: sessionToken,
            },
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
  }, [sessionToken]);

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
              <tr key={company.id}>
                <td>{company.id}</td>
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
