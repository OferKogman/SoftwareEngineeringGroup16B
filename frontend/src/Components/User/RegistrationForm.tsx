import { useState } from "react";
import { NavLink } from "react-router-dom";
import "./CSS/RegistrationForm.css";

export type RegistrationData = {
  email: string;
  password: string;
};

type RegistrationFormProps = {
  title: string;
  onRegistration: (event: RegistrationData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: RegistrationData = {
  email: "",
  password: "",
};

export default function RegistrationForm({
  title,
  onRegistration,
  onCancel,
}: RegistrationFormProps) {
  const [formData, setFormData] = useState<RegistrationData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof RegistrationData>(
    field: K,
    value: RegistrationData[K],
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
      await onRegistration({
        ...formData,
        email: formData.email.trim(),
        password: formData.password.trim(),
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to register.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="registration-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>
      {error && <p className="form-error">{error}</p>}

      <div className="form-row">
        <label>Email</label>
        <input
          type="email"
          required
          value={formData.email}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("email", event.target.value);
          }}
          placeholder="email"
        />
      </div>

      <div className="form-row">
        <label>Password</label>
        <input
          type="password"
          required
          value={formData.password}
          onChange={(event) => updateField("password", event.target.value)}
          placeholder="Password"
        />
      </div>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Registering..." : "Register"}
        </button>
      </div>

      <a>
        <NavLink to="/login" className="auth-link">
          Already have an account? Login here
        </NavLink>
      </a>
    </form>
  );
}
