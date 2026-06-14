import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSession } from "../../App";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import CreateProductionCompany from "../ProdactionCompany/CreateProductionCompany";

export default function ViewUserCompanyList() {
  const { sessionToken } = useSession();
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);
  const navigate = useNavigate();

  useEffect(() => {
    async function loadCompanies() {
      if (!sessionToken) {
        setError("Missing session token.");
        setCompanyDTOList([]);
        return;
      }

      try {
        setError("");

        const response = await fetch(
          `http://localhost:8080/api/user/me/companies`,
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
        setCompanyDTOList([]);
      }
    }

    void loadCompanies();
  }, [sessionToken]);

  if (companyDTOList === null) {
    return <div>Loading companies...</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      {!error ? (
        <div>
          <CreateProductionCompany />
          {companyDTOList.length === 0 ? (
            <p>No companies found</p>
          ) : (
            <table style={{ borderCollapse: "collapse", width: "100%" }}>
              <thead>
                <tr>
                  <th>ID</th>
                  <th>Name</th>
                  <th>Rating</th>
                  <th>Actions</th>
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
                        onClick={() => navigate(`/companies/${company.id}`)}
                      >
                        Manage
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      ) : (
        <></>
      )}
    </div>
  );
}
