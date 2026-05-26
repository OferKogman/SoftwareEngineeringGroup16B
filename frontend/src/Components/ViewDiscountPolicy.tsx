import { useState } from "react";

export type DiscountPolicyDTO = {
  maxTickets: number;
  effectiveFrom: string;
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
  effectiveFrom: "",
  maxTickets: 1000,
};

export default function ViewDiscountPolicy({
  onSubmit,
  onCancel,
}: ViewDiscountPolicyProps) {
  const [formData, setFormData] = useState<DiscountPolicyDTO>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [noExpiration, setNoExpiration] = useState(false);

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

    if (!noExpiration && !formData.expirationDate) {
      setError("Please enter a valid expiration date, or check 'No expiration date'.");
      setIsSubmitting(false);
      return;
    }

    try {
      await onSubmit({
        ...formData,
        code: formData.discountType === "COUPON" ? formData.code.trim() : "",
      });
      setFormData(initialFormData);
      setNoExpiration(false);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to save discount policy.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  const isConditioned = formData.discountType === "CONDITIONED";
  const isCoupon = formData.discountType === "COUPON";

  return (
    <form className="event-creation-form" onSubmit={handleSubmit} noValidate>
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

      <label>
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
      </label>

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

      {isConditioned && (
        <label>
          Maximum tickets required
          <input
            type="number"
            max="1000"
            required
            value={formData.maxTickets}
            onInvalid={(event) =>
              event.currentTarget.setCustomValidity(
                "Maximum tickets must be at most 1000.",
              )
            }
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("maxTickets", Number(event.target.value));
            }}
          />
        </label>
      )}

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
        Effective from date
        <input
          type="datetime-local"
          required
          value={formData.effectiveFrom}
          min={getMinimumDateTime()}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please enter a valid effective from date and time in the future.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("effectiveFrom", event.target.value);
          }}
        />
      </label>

      <label>
        <input
          type="checkbox"
          checked={noExpiration}
          onChange={(event) => {
            setNoExpiration(event.target.checked);
            if (event.target.checked) {
              updateField("expirationDate", "");
            }
          }}
        />
        No expiration date
      </label>

      {!noExpiration && (
        <label>
          Expiration date
          <input
            type="datetime-local"
            value={formData.expirationDate}
            min={getMinimumDateTime()}
            onChange={(event) => {
              updateField("expirationDate", event.target.value);
            }}
          />
        </label>
      )}

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