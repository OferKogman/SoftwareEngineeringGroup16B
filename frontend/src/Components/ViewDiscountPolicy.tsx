import { useState } from "react";

export type DiscountPolicyDTO = {
  discountType: "SIMPLE" | "CONDITIONED" | "COUPON";
  discountPercentage: number;
  discountAmount: number;
  minTickets: number;
  code: string;
  expirationDate: string;
};

type ViewDiscountPolicyProps = {
  onSubmit: (policy: DiscountPolicyDTO) => void | Promise<void>;
  onCancel?: () => void;
};

const DISCOUNT_TYPES = [
  { value: "SIMPLE", label: "Simple discount" },
  { value: "CONDITIONED", label: "Conditioned discount" },
  { value: "COUPON", label: "Coupon discount" },
];

const initialFormData: DiscountPolicyDTO = {
  discountType: "SIMPLE",
  discountPercentage: 0,
  discountAmount: 0,
  minTickets: 2,
  code: "",
  expirationDate: "",
};

export default function ViewDiscountPolicy({
  onSubmit,
  onCancel,
}: ViewDiscountPolicyProps) {
  const [formData, setFormData] = useState<DiscountPolicyDTO>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof DiscountPolicyDTO>(
    field: K,
    value: DiscountPolicyDTO[K],
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
      await onSubmit({
        ...formData,
        code: formData.discountType === "COUPON" ? formData.code.trim() : "",
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to save discount policy.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const isSimple = formData.discountType === "SIMPLE";
  const isConditioned = formData.discountType === "CONDITIONED";
  const isCoupon = formData.discountType === "COUPON";

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Discount Policy</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Discount type
        <select
          required
          value={formData.discountType}
          onChange={(event) => {
            updateField(
              "discountType",
              event.target.value as DiscountPolicyDTO["discountType"],
            );
          }}
        >
          {DISCOUNT_TYPES.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </label>

      {/* All types share discount amount and percentage */}
      {/* <label>
        Discount percentage (%)
        <input
          type="number"
          min="0"
          max="100"
          step="0.01"
          required
          value={formData.discountPercentage}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Discount percentage must be between 0 and 100.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("discountPercentage", Number(event.target.value));
          }}
        />
      </label>

      <label>
        Discount amount
        <input
          type="number"
          min="0"
          step="0.01"
          required
          value={formData.discountAmount}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Discount amount must be 0 or greater.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("discountAmount", Number(event.target.value));
          }}
        />
      </label> */}

      {/* Conditioned: minimum ticket predicate */}
      {isConditioned && (
        <label>
          Minimum tickets required
          <input
            type="number"
            min="2"
            required
            value={formData.minTickets}
            onInvalid={(event) =>
              event.currentTarget.setCustomValidity(
                "Minimum tickets must be at least 2.",
              )
            }
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("minTickets", Number(event.target.value));
            }}
          />
        </label>
      )}

      {/* Coupon only */}
      {isCoupon && (
        <label>
          Coupon code
          <input
            type="text"
            required
            pattern=".*\S.*"
            value={formData.code}
            onInvalid={(event) =>
              event.currentTarget.setCustomValidity(
                "Coupon code cannot be empty or whitespace.",
              )
            }
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("code", event.target.value);
            }}
            placeholder="e.g. SUMMER25"
          />
        </label>
      )}

      <label>
        Expiration date
        <input
          type="datetime-local"
          required
          value={formData.expirationDate}
          min={getMinimumDateTime()}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid expiration date and time in the future.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("expirationDate", event.target.value);
          }}
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Saving..." : "Save policy"}
        </button>
      </div>
    </form>
  );
}

function getMinimumDateTime(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset();
  const localDate = new Date(now.getTime() - offset * 60 * 1000);
  return localDate.toISOString().slice(0, 16);
}