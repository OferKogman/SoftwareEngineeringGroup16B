import { useState } from "react";
import { useApiFetch } from "../../apiFetch";
//String currency, , , ,
type PaymentFormData = {
  firstName: string; //String holder
  lastName: string;
  idNumber: string; // String id
  cardNumber: string; //String cardNumber,
  expiryDate: string; // int month, int year
  cvv: string; //String cvv
};

type PaymentPayload = {
  firstName: string;
  lastName: string;
  idNumber: string;
  cardNumber: string;
  expiryDate: string;
  cvv: string;
  amount: number;
};

type FormErrors = {
  submit?: string;
};

type PaymentFormProps = {
  initAmount: number;
  orderID: string;
  onPaymentSubmit: (paymentData: PaymentPayload) => Promise<void> | void;
};

export default function PaymentForm({
  initAmount,
  orderID,
  onPaymentSubmit,
}: PaymentFormProps) {
  const [formData, setFormData] = useState<PaymentFormData>({
    firstName: "",
    lastName: "",
    idNumber: "",
    cardNumber: "",
    expiryDate: "",
    cvv: "",
  });

  const [errors, setErrors] = useState<FormErrors>({});
  const [message, setMessage] = useState("");
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [isApplyingCoupon, setIsApplyingCoupon] = useState(false);
  const [couponCode, setCouponCode] = useState("");
  const [amount, setAmount] = useState(initAmount);

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setErrors((prev) => ({ ...prev, submit: undefined }));
  }

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name } = event.target;
    let value = event.target.value;

    if (name === "expiryDate") {
      const digits = value.replace(/\D/g, "").slice(0, 4);

      if (digits.length >= 3) {
        const firstTwoDigits = Number(digits.slice(0, 2));

        if (firstTwoDigits > 12) {
          value = `0${digits.slice(0, 1)}/${digits.slice(1, 3)}`;
        } else {
          value = `${digits.slice(0, 2)}/${digits.slice(2)}`;
        }
      } else {
        value = digits;
      }
    }

    if (name === "idNumber") {
      value = value.replace(/\D/g, "").slice(0, 9);
    }

    if (name === "cardNumber") {
      value = value.replace(/\D/g, "").slice(0, 16);
    }

    if (name === "cvv") {
      value = value.replace(/\D/g, "").slice(0, 3);
    }

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  async function handleApplyCoupon() {
    setIsApplyingCoupon(true);
    setMessage("");
    setErrors((prev) => ({ ...prev, submit: undefined }));

    try {
      const response = await apiFetch(
        `http://localhost:8080/api/orders/${orderID}/coupon`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            couponCode: couponCode,
          }),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }

      setAmount(Number(await response.json()));
      setMessage("Coupon applied successfully.");
    } catch (err) {
      setErrors({
        submit: err instanceof Error ? err.message : "",
      });
    } finally {
      setIsApplyingCoupon(false);
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setMessage("");

    setErrors({});

    setIsSubmitting(true);

    try {
      await onPaymentSubmit({
        firstName: formData.firstName,
        lastName: formData.lastName,
        idNumber: formData.idNumber,
        cardNumber: formData.cardNumber,
        expiryDate: formData.expiryDate,
        cvv: formData.cvv,
        amount: amount,
      });
      setMessage(
        `Payment successful. Thank you ${formData.firstName} ${formData.lastName} for paying ₪${amount}.`,
      );

      setFormData({
        firstName: "",
        lastName: "",
        idNumber: "",
        cardNumber: "",
        expiryDate: "",
        cvv: "",
      });

      setErrors({});
    } catch (error: unknown) {
      setErrors({
        submit: error instanceof Error ? error.message : "",
      });
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <h2>Payment Amount: ₪{amount}</h2>

      <input
        name="firstName"
        type="text"
        required
        value={formData.firstName}
        onChange={handleChange}
        placeholder="First Name"
      />

      <input
        name="lastName"
        type="text"
        required
        value={formData.lastName}
        onChange={handleChange}
        placeholder="Last Name"
      />

      <input
        name="idNumber"
        type="text"
        required
        inputMode="numeric"
        maxLength={9}
        pattern="[0-9]{9}"
        title="ID number must be exactly 9 digits"
        value={formData.idNumber}
        onChange={handleChange}
        placeholder="ID Number"
      />

      <input
        name="cardNumber"
        type="text"
        required
        inputMode="numeric"
        maxLength={16}
        pattern="[0-9]{16}"
        title="Card number must be exactly 16 digits"
        value={formData.cardNumber}
        onChange={handleChange}
        placeholder="Card Number"
      />

      <input
        name="expiryDate"
        type="text"
        required
        inputMode="numeric"
        maxLength={5}
        pattern="(0[1-9]|1[0-2])/[0-9]{2}"
        title="Expiry date must be in MM/YY format"
        value={formData.expiryDate}
        onChange={handleChange}
        placeholder="MM/YY"
      />

      <input
        name="cvv"
        type="text"
        required
        inputMode="numeric"
        maxLength={3}
        pattern="[0-9]{3}"
        title="CVV must be exactly 3 digits"
        value={formData.cvv}
        onChange={handleChange}
        placeholder="CVV"
      />

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button type="button" onClick={closePopup}>
            {" "}
            OK{" "}
          </button>
        </div>
      )}
      {errors.submit && (
        <div className="settings-alert">
          <p>{errors.submit}</p>
          <button type="button" onClick={closePopup}>
            {" "}
            OK{" "}
          </button>
        </div>
      )}

      <div className="payment-form-field">
        <label>Coupon Code</label>
        <input
          id="couponCode"
          type="text"
          value={couponCode}
          onChange={(e) => setCouponCode(e.target.value)}
        />
        <button
          type="button"
          disabled={isApplyingCoupon}
          onClick={() => void handleApplyCoupon()}
        >
          {isApplyingCoupon ? "Applying..." : "Apply"}
        </button>
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Processing..." : `Pay $${amount}`}
      </button>
    </form>
  );
}
