import React, { useState } from "react";

type PurchasePolicyType = "MIN_ITEMS" | "MAX_ITEMS" | "MIN_AGE" | "MAX_AGE";
type CompositionOptions = "AND" | "OR";

export type PurchasePolicyCreationData = {
  name: string;
  type: PurchasePolicyType;
  value: number;
  composition: CompositionOptions;
};

type PurchasePolicyCreationFormProps = {
  onCreatePolicy: (policy: PurchasePolicyCreationData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: PurchasePolicyCreationData = {
  name: "",
  type: "MIN_ITEMS",
  value: 0,
  composition: "AND",
};

export default function PurchasePolicyCreationForm({
  onCreatePolicy,
  onCancel,
}: PurchasePolicyCreationFormProps) {
  const [formData, setFormData] =
    useState<PurchasePolicyCreationData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof PurchasePolicyCreationData>(
    field: K,
    value: PurchasePolicyCreationData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  function validateForm() {
    if (formData.name.trim().length === 0) {
      return "Policy name is required.";
    }

    if (!Number.isFinite(formData.value) || formData.value < 0) {
      return "Policy value must be a non-negative number.";
    }

    if (formData.type === "MAX_AGE" && formData.value === 0) {
      return "Maximum age can't be 0.";
    }

    if (formData.type === "MAX_ITEMS" && formData.value === 0) {
      return "Maximum items can't be 0.";
    }

    return "";
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationError = validateForm();
    if (validationError) {
      setError(validationError);
      return;
    }

    setError("");
    setIsSubmitting(true);

    try {
      await onCreatePolicy({
        ...formData,
        name: formData.name.trim(),
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to create purchase policy.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="purchase-policy-creation-form" onSubmit={handleSubmit}>
      <h2>Create Purchase Policy</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Policy name
        <input
          type="text"
          value={formData.name}
          onChange={(event) => updateField("name", event.target.value)}
          placeholder="Name"
        />
      </label>

      <label>
        Policy type
        <select
          value={formData.type}
          onChange={(event) =>
            updateField("type", event.target.value as PurchasePolicyType)
          }
        >
          <option value="MIN_ITEMS">Minimum item count</option>
          <option value="MAX_ITEMS">Maximum item count</option>
          <option value="MIN_AGE">Minimum total price</option>
          <option value="MAX_AGE">Maximum total price</option>
        </select>
      </label>

      <label>
        Value
        <input
          type="number"
          min="0"
          value={formData.value}
          onChange={(event) => updateField("value", Number(event.target.value))}
        />
      </label>

      <label>
        Composition
        <select
          value={formData.composition}
          onChange={(event) =>
            updateField("composition", event.target.value as CompositionOptions)
          }
        >
          <option value="AND">And</option>
          <option value="OR">Or</option>
        </select>
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create policy"}
        </button>
      </div>
    </form>
  );
}
