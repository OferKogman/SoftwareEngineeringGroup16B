import { useState } from "react";

type PolicyType = "AGE_LIMIT" | "TICKET_LIMIT";
type TicketLimitType = "MAXIMUM" | "MINIMUM";

export type EditPurchasePolicyData =
  | { policyType: "AGE_LIMIT"; age: number }
  | { policyType: "TICKET_LIMIT"; limitType: TicketLimitType; limit: number };

export default function EditPurchasePolicy() {
  const [policyType, setPolicyType] = useState<PolicyType>("AGE_LIMIT");
  const [age, setAge] = useState("");
  const [limit, setLimit] = useState("");
  const [limitType, setLimitType] = useState<TicketLimitType>("MAXIMUM");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError("");
    setIsSubmitting(true);

    try {
      const payload: EditPurchasePolicyData =
        policyType === "AGE_LIMIT"
          ? { policyType: "AGE_LIMIT", age: Number(age) }
          : { policyType: "TICKET_LIMIT", limitType, limit: Number(limit) };

      async function onSubmit(data: EditPurchasePolicyData) {}

      await onSubmit(payload);
      setAge("");
      setLimit("");
    } catch (err) {
      setSubmitError(
        err instanceof Error
          ? err.message
          : "Failed to update purchase policy.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const isAgeLimit = policyType === "AGE_LIMIT";

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Edit Purchase Policy</h2>

      {submitError && <p className="form-error">{submitError}</p>}

      <label>
        Policy Type
        <select
          value={policyType}
          onChange={(e) => {
            setPolicyType(e.target.value as PolicyType);
            setSubmitError("");
          }}
        >
          <option value="AGE_LIMIT">Age Limit</option>
          <option value="TICKET_LIMIT">Ticket Limit</option>
        </select>
      </label>

      {isAgeLimit && (
        <label>
          Minimum Age
          <input
            type="number"
            required
            min="0"
            value={age}
            onChange={(e) => setAge(e.target.value)}
            placeholder="Minimum Age"
          />
        </label>
      )}

      {!isAgeLimit && (
        <>
          <label>
            Limit Type
            <select
              value={limitType}
              onChange={(e) => setLimitType(e.target.value as TicketLimitType)}
            >
              <option value="MAXIMUM">Maximum</option>
              <option value="MINIMUM">Minimum</option>
            </select>
          </label>

          <label>
            Ticket Limit
            <input
              type="number"
              required
              min="0"
              value={limit}
              onChange={(e) => setLimit(e.target.value)}
              placeholder="Ticket Limit"
            />
          </label>
        </>
      )}

      <div className="form-actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Update Policy"}
        </button>
      </div>
    </form>
  );
}
