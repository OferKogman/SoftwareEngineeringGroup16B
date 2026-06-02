import { useEffect, useState } from "react";
import type { ProductionCompanyDTO } from "./ProdactionCompany/ProdctionCompanyForm";
import { useNavigate, useParams } from "react-router-dom";


export default function ViewUserCompanyList() {
  const { userID } = useParams();
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!userID) {
      return;
    }

    async function loadCompanies() {
      try {
        const response = await fetch(`/api/user/me/companies`);

        if (!response.ok) {
         throw new Error("Failed to load companies.");
        }

        const event: ProductionCompanyDTO = await response.json();

  
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load companies.",
        );
      }
    }

    void loadCompanies();
  }, [userID]);

  if (!userID) {
    return <div>User is not logged in</div>;
  }

  if (!companyDTOList) {
    return <div>No companies found for user</div>;
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
              <th>Actions</th>
            </tr>
          </thead>

          <tbody>
            {companyDTOList.map((company) => (
              <tr key={company.productionCompanyID}>
                <td>{company.productionCompanyID}</td>
                <td>{company.name}</td>
                <td>{company.rating}</td>
                <td>
                  <button
                    onClick={() =>
                      navigate(
                        `/production-company-menegment/${company.productionCompanyID}`,
                      )
                    }
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
  );
}
