import { useState } from "react";

export type PurchaseHistorySearchData = {
  companyId: string
  subjectId: string;
  eventId: string;
};

type AdminViewPurchaseHistoryProps = {
  title: string;
  onSearch: (searchData: PurchaseHistorySearchData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: PurchaseHistorySearchData = {
  companyId: "",
  subjectId: "",
  eventId: "",
};

export default function AdminViewPurchaseHistory({
    // should expect the admin's Stocken
  title,
  onSearch,
  onCancel,
}: AdminViewPurchaseHistoryProps) {
  const [formData, setFormData] = useState<PurchaseHistorySearchData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof PurchaseHistorySearchData>(
    field: K,
    value: PurchaseHistorySearchData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await onSearch({
        companyId: formData.companyId.trim(),
        subjectId: formData.subjectId.trim(),
        eventId: formData.eventId.trim(),
      });

      setFormData(initialFormData);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to search purchase history.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Subject ID
        <input
          type="text"
          value={formData.subjectId}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("subjectId", event.target.value);
          }}
          placeholder="Optional subject ID"
        />
      </label>
       <label>
        Company ID
        <input
          type="text"
          value={formData.companyId}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("companyId", event.target.value);
          }}
          placeholder="Optional company ID"
        />
      </label>

      <label>
        Event ID
        <input
          type="text"
          value={formData.eventId}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("eventId", event.target.value);
          }}
          placeholder="Optional event ID"
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}

        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Searching..." : "Search"}
        </button>
      </div>
    </form>
  );
}