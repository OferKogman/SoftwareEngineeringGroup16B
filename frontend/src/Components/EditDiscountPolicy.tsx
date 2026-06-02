import { useState } from "react";

type EditPurchasePolicyProps = {
  onSubmit: (discountPercentage: number) => void | Promise<void>;
  onCancel?: () => void;
};

export default function EditPurchasePolicy({ onSubmit, onCancel }: EditPurchasePolicyProps) {
  const [discountPercentage, setDiscountPercentage] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError("");
    setIsSubmitting(true);

    try {
      await onSubmit(Number(discountPercentage));
      setDiscountPercentage("");
    } catch (err) {
      setSubmitError(err instanceof Error ? err.message : "Failed to update purchase policy.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Edit Purchase Policy</h2>

      {submitError && <p className="form-error">{submitError}</p>}

      <label>
        Discount Percentage (%)
        <input
          type="number"
          required
          min="0"
          max="100"
          step="0.01"
          value={discountPercentage}
          onChange={(e) => setDiscountPercentage(e.target.value)}
          placeholder="e.g. 15"
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Update Policy"}
        </button>
      </div>
    </form>
  );
}