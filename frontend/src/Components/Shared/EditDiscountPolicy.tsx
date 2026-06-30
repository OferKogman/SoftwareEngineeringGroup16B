import { useState } from "react";
import { useOutletContext, useParams } from "react-router-dom";
import type { NullableDiscountPolicyDTO } from "../../DTOs/DiscountPolicyDTO";
import type { EventDTO } from "../../DTOs/EventDTO";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import { useApiFetch } from "../../apiFetch";
import DiscountPolicyTree from "./DiscountPolicyTree";

type EditDiscountPolicyProps = {
  type: "event" | "company";
};

type DiscountPolicyContext = {
  event?: EventDTO;
  company?: ProductionCompanyDTO;
};

export default function EditDiscountPolicy({ type }: EditDiscountPolicyProps) {
  const { eventID, companyId } = useParams();

  const { event, company } = useOutletContext<DiscountPolicyContext>();

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  if (type === "event" && !event) {
    return <p>Event data not available.</p>;
  }

  if (type === "company" && !company) {
    return <p>Company data not available.</p>;
  }

  const policy: NullableDiscountPolicyDTO =
    type === "event"
      ? (event?.eventDiscountPolicy ?? null)
      : (company?.discountPolicy ?? null);

  async function savePolicy(policy: NullableDiscountPolicyDTO) {
    const id = type === "event" ? eventID : companyId;

    setMessage("");
    setError("");

    if (!id) {
      setError(`Missing ${type} ID`);
      return;
    }

    try {
      const url =
        type === "event"
          ? `http://localhost:8080/api/events/${id}/discount-policy`
          : `http://localhost:8080/api/production-companies/${id}/discount-policy`;

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

      setMessage("Discount policy saved successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    }
  }

  return (
    <main>
      <h2>Edit {type === "event" ? "Event" : "Company"} Discount Policy</h2>

      <DiscountPolicyTree policy={policy} onSave={savePolicy} />

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
    </main>
  );
}
