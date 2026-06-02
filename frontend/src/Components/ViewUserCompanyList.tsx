import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { ProductionCompanyDTO } from "./ProdactionCompany/ProdctionCompanyForm";

type CompanyListProps = {
  onManageCompany: (companyID: number) => void | Promise<void>;
};

export default function ViewUserCompanyList({
  onManageCompany,
}: CompanyListProps) {
  const { userID } = useParams();
  const [error, setError] = useState<string>("");
  const [companyDTOList, setCompanyDTOList] = useState<
    ProductionCompanyDTO[] | null
  >(null);

  useEffect(() => {
    if (!userID) {
      return;
    }

    async function loadCompanies() {
      try {
        //const response = await fetch(`/api/companies/${userID}`);

        //if (!response.ok) {
        //  throw new Error("Failed to load companies.");
        //}

        //const event: ProductionCompanyDTO = await response.json();

        const companyList: ProductionCompanyDTO[] = [
          {
            productionCompanyID: 0,
            name: "Disney",
            rating: 5,
            founderID: "a@c",
            members: [],
            invites: [],
            childrenByUser: [],
          },
          {
            productionCompanyID: 1,
            name: "Pixar",
            rating: 5,
            founderID: "b@c",
            members: [],
            invites: [],
            childrenByUser: [],
          },
        ];
        setCompanyDTOList(companyList);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Failed to load companies.",
        );
      }
    }

    void loadCompanies();
  }, [userID]);

  if (!userID) {
    return <div>Missing user id</div>;
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
                    onClick={() => onManageCompany(company.productionCompanyID)}
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
