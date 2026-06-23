import { useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";
import type { NullablePurchasePolicyDTO } from "../DTOs/PurchasePolicyDTO";
import { useApiFetch } from "../apiFetch";
import "./EditPurchasePolicy.css";
import PurchasePolicyTree from "./Shared/PurchasePolicyTree";

export default function EditPurchasePolicy() {
  const { eventID } = useParams();
  const { event } = useOutletContext<{ event: EventDTO }>();
  const [alert, setAlert] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);

  const apiFetch = useApiFetch();

  async function savePolicy(policy: NullablePurchasePolicyDTO) {
    if (!eventID) {
      return;
    }

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/events/${eventID}/purchase-policy`,
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(policy),
        },
      );

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
      <h2>Edit Purchase Policy</h2>

      <PurchasePolicyTree
        policy={event.eventPurchasePolicy}
        onSave={savePolicy}
      />

      {alert && (
        <div className={`settings-alert`}>
          <p>{alert.message}</p>

          <button onClick={() => setAlert(null)}>OK</button>
        </div>
      )}
    </main>
  );
}
