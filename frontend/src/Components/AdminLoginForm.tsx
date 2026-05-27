import { useState } from "react";

export type AdminLoginData = {
  email: string;
  password: string;
};

type AdminLoginFormProps = {
  onAdminLogin: (event: AdminLoginData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: AdminLoginData = {
  email: "",
  password: "",
};

export default function AdminLoginForm({
  onAdminLogin,
  onCancel,
}: AdminLoginFormProps) {
  const [formData, setFormData] = useState<AdminLoginData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof AdminLoginData>(
    field: K,
    value: AdminLoginData[K],
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
      await onAdminLogin({
        ...formData,
        email: formData.email.trim(),
        password: formData.password.trim(),
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to create event.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="admin-login-form" onSubmit={handleSubmit}>
      <h2>Admin Login</h2>

      {error && <p className="form-error">{error}</p>}

      <label>
        Email
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
      </label>

      <label>
        Password
        <input
          type="text"
          required
          pattern=".*\S.*"
          onInvalid={(event) =>
            event.currentTarget.setCustomValidity(
              "Password cannot be empty or whitespace.",
            )
          }
          value={formData.password}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("password", event.target.value);
          }}
          placeholder="Password"
        />
      </label>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? "Creating..." : "Create event"}
        </button>
      </div>
    </form>
  );
}
