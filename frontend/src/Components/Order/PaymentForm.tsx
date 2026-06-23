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
  firstName?: string;
  lastName?: string;
  idNumber?: string;
  cardNumber?: string;
  expiryDate?: string;
  cvv?: string;
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
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [couponCode, setCouponCode] = useState("");
  const [amount, setAmount] = useState(initAmount);

  const apiFetch = useApiFetch();

  function handleChange(event: React.ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target;

    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  }

  function validateForm(): FormErrors {
    const newErrors: FormErrors = {};

    if (!formData.firstName.trim()) {
      newErrors.firstName = "First name is required";
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = "Last name is required";
    }

    if (!formData.idNumber.trim()) {
      newErrors.idNumber = "ID number is required";
    } else if (!/^\d{9}$/.test(formData.idNumber)) {
      newErrors.idNumber = "ID number must contain only digits";
    }

    if (!formData.cardNumber.trim()) {
      newErrors.cardNumber = "Card number is required";
    } else if (!/^\d{16}$/.test(formData.cardNumber)) {
      newErrors.cardNumber = "Card number must be 16 digits";
    }

    if (!formData.expiryDate.trim()) {
      newErrors.expiryDate = "Expiry date is required";
    } else if (!/^(0[1-9]|1[0-2])\/\d{2}$/.test(formData.expiryDate)) {
      newErrors.expiryDate = "Expiry date must be MM/YY";
    }

    if (!formData.cvv.trim()) {
      newErrors.cvv = "CVV is required";
    } else if (!/^\d{3}$/.test(formData.cvv)) {
      newErrors.cvv = "CVV must be 3 digits";
    }

    return newErrors;
  }

  async function handleApplyCoupon() {
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
        throw new Error((await response.text()) || "Failed to reserve field.");
      }

      setAmount(Number(response.json()));
    } catch (err) {
      setErrors({
        submit: err instanceof Error ? err.message : "Failed apply discount.",
      });
    }
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    const validationErrors = validateForm();
    setErrors(validationErrors);

    if (Object.keys(validationErrors).length > 0) {
      return;
    }

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
      alert(
        `Payment successful!\n\nThank you ${formData.firstName} ${formData.lastName} for paying ₪${amount}`,
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
        submit: error instanceof Error ? error.message : "Payment failed",
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
        value={formData.firstName}
        onChange={handleChange}
        placeholder="First Name"
      />
      {errors.firstName && <p>{errors.firstName}</p>}

      <input
        name="lastName"
        value={formData.lastName}
        onChange={handleChange}
        placeholder="Last Name"
      />
      {errors.lastName && <p>{errors.lastName}</p>}

      <input
        name="idNumber"
        value={formData.idNumber}
        onChange={handleChange}
        placeholder="ID Number"
      />
      {errors.idNumber && <p>{errors.idNumber}</p>}

      <input
        name="cardNumber"
        value={formData.cardNumber}
        onChange={handleChange}
        placeholder="Card Number"
      />
      {errors.cardNumber && <p>{errors.cardNumber}</p>}

      <input
        name="expiryDate"
        value={formData.expiryDate}
        onChange={handleChange}
        placeholder="MM/YY"
      />
      {errors.expiryDate && <p>{errors.expiryDate}</p>}

      <input
        name="cvv"
        value={formData.cvv}
        onChange={handleChange}
        placeholder="CVV"
      />
      {errors.cvv && <p>{errors.cvv}</p>}

      {errors.submit && <p>{errors.submit}</p>}

      <div className="payment-form-field">
        <label>Coupon Code</label>
        <input
          id="couponCode"
          type="text"
          value={couponCode}
          onChange={(e) => setCouponCode(e.target.value)}
        />
        <button onClick={() => handleApplyCoupon()}>Apply</button>
      </div>

      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? "Processing..." : `Pay $${amount}`}
      </button>
    </form>
  );
}
