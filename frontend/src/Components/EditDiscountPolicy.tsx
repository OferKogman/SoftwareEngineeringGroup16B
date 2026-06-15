import { useState } from "react";

export default function EditDiscountPolicy() {
  const [discountPercentage, setDiscountPercentage] = useState("");
  const [submitError, setSubmitError] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  async function onSubmit(discountPercentage: number) {}

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSubmitError("");
    setIsSubmitting(true);

    try {
      await onSubmit(Number(discountPercentage));
      setDiscountPercentage("");
    } catch (err) {
      setSubmitError(
        err instanceof Error
          ? err.message
          : "Failed to update discount policy.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Edit Discount Policy</h2>

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
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Update Policy"}
        </button>
      </div>
    </form>
  );
}
