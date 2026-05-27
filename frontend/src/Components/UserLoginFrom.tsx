import { useState } from "react";

export type UserLoginData = {
  email: string;
  password: string;
};

type UserLoginFormProps = {
  onUserLogin: (event: UserLoginData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: UserLoginData = {
  email: "",
  password: "",
};

export default function UserLoginForm({
  onUserLogin,
  onCancel,
}: UserLoginFormProps) {
  const [formData, setFormData] = useState<UserLoginData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof UserLoginData>(
    field: K,
    value: UserLoginData[K],
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
      await onUserLogin({
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
    <form className="user-login-form" onSubmit={handleSubmit}>
      <h2>User Login</h2>

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
          {isSubmitting ? "Logging in..." : "Login"}
        </button>
      </div>
    </form>
  );
}
