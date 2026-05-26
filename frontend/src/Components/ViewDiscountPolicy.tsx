import { useState } from "react";

export type DiscountPolicyDTO = {
  discountType: string;
  ticketCount: number;
  buyX: number;
  getY: number;
  discountPercentage: number;
  discountAmount: number;
  code: string;
  expirationDate: string;
};

type ViewDiscountPolicyProps = {
  onSubmit: (policy: DiscountPolicyDTO) => void | Promise<void>;
  onCancel?: () => void;
};

const DISCOUNT_TYPES = [
  { value: "PERCENTAGE", label: "Percentage off" },
  { value: "FIXED_AMOUNT", label: "Fixed amount off" },
  { value: "BUY_X_GET_Y", label: "Buy X get Y" },
  { value: "BULK", label: "Bulk / ticket count" },
];

const initialFormData: DiscountPolicyDTO = {
  discountType: "",
  ticketCount: 0,
  buyX: 0,
  getY: 0,
  discountPercentage: 0,
  discountAmount: 0,
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
        discountType: formData.discountType.trim(),
        code: formData.code.trim(),
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

  const isBuyXGetY = formData.discountType === "BUY_X_GET_Y";
  const isBulk = formData.discountType === "BULK";
  const isPercentage = formData.discountType === "PERCENTAGE";
  const isFixed = formData.discountType === "FIXED_AMOUNT";

  return (
    <form className="event-creation-form" onSubmit={handleSubmit}>
      <h2>Discount Policy</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Discount type
        <select
          required
          value={formData.discountType}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Please select a discount type.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("discountType", event.target.value);
          }}
        >
          <option value="" disabled>
            Select a discount type
          </option>
          {DISCOUNT_TYPES.map(({ value, label }) => (
            <option key={value} value={value}>
              {label}
            </option>
          ))}
        </select>
      </label>

      <label>
        Promo code
        <input
          type="text"
          required
          pattern=".*\S.*"
          value={formData.code}
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Promo code cannot be empty or whitespace.",
            )
          }
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("code", event.target.value);
          }}
          placeholder="e.g. SUMMER25"
        />
      </label>

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

      {isBulk && (
        <label>
          Minimum ticket count
          <input
            type="number"
            min="1"
            required
            value={formData.ticketCount}
            onInvalid={(event) =>
              event.currentTarget.setCustomValidity(
                "Ticket count must be at least 1.",
              )
            }
            onChange={(event) => {
              event.currentTarget.setCustomValidity("");
              updateField("ticketCount", Number(event.target.value));
            }}
          />
        </label>
      )}

      {isBuyXGetY && (
        <>
          <label>
            Buy X (tickets to purchase)
            <input
              type="number"
              min="1"
              required
              value={formData.buyX}
              onInvalid={(event) =>
                event.currentTarget.setCustomValidity(
                  "Buy X must be at least 1.",
                )
              }
              onChange={(event) => {
                event.currentTarget.setCustomValidity("");
                updateField("buyX", Number(event.target.value));
              }}
            />
          </label>

          <label>
            Get Y (free tickets)
            <input
              type="number"
              min="1"
              required
              value={formData.getY}
              onInvalid={(event) =>
                event.currentTarget.setCustomValidity(
                  "Get Y must be at least 1.",
                )
              }
              onChange={(event) => {
                event.currentTarget.setCustomValidity("");
                updateField("getY", Number(event.target.value));
              }}
            />
          </label>
        </>
      )}

      {(isPercentage || isBulk) && (
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
      )}

      {isFixed && (
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