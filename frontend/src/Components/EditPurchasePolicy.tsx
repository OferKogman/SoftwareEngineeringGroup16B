import { useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";
import type { ProductionCompanyDTO } from "../DTOs/ProductionCompanyDTO";
import type { NullablePurchasePolicyDTO } from "../DTOs/PurchasePolicyDTO";
import { useApiFetch } from "../apiFetch";
import "./EditPurchasePolicy.css";
import PurchasePolicyTree from "./Shared/PurchasePolicyTree";

type EditPurchasePolicyProps = {
  type: "event" | "company";
};

type PurchasePolicyContext = {
  event?: EventDTO;
  company?: ProductionCompanyDTO;
};

export default function EditPurchasePolicy({ type }: EditPurchasePolicyProps) {
  const { eventID, companyId } = useParams();

  const { event, company } = useOutletContext<PurchasePolicyContext>();

  const [alert, setAlert] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  const apiFetch = useApiFetch();

  if (type === "event" && !event) {
    return <p>Event data not available.</p>;
  }

  if (type === "company" && !company) {
    return <p>Company data not available.</p>;
  }

  const policy: NullablePurchasePolicyDTO =
    type === "event"
      ? (event?.eventPurchasePolicy ?? null)
      : (company?.purchasePolicy ?? null);

  async function savePolicy(policy: NullablePurchasePolicyDTO) {
    const id = type === "event" ? eventID : companyId;

    if (!id) {
      setAlert({
        type: "error",
        message: `Missing ${type} ID`,
      });
      return;
    }

    try {
      const url =
        type === "event"
          ? `http://localhost:8080/api/events/${id}/purchase-policy`
          : `http://localhost:8080/api/production-companies/${id}/purchase-policy`;

      const response = await apiFetch(url, {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(policy),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setAlert({
        type: "success",
        message: "Purchase policy saved successfully",
      });
    } catch (err) {
      setAlert({
        type: "error",
        message:
          err instanceof Error ? err.message : "Failed saving purchase policy",
      });
    }
  }

  return (
    <main>
      <h2>Edit {type === "event" ? "Event" : "Company"} Purchase Policy</h2>

      <PurchasePolicyTree policy={policy} onSave={savePolicy} />

      {alert && (
        <div className="settings-alert">
          <p>{alert.message}</p>

          <button onClick={() => setAlert(null)}>OK</button>
        </div>
      )}
    </main>
  );
}
