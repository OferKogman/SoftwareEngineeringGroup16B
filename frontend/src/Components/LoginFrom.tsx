import { useState } from "react";

export type LoginData = {
  email: string;
  password: string;
};

type LoginFormProps = {
  title: string;
  onLogin: (event: LoginData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialFormData: LoginData = {
  email: "",
  password: "",
};

export default function LoginForm({
  title,
  onLogin,
  onCancel,
}: LoginFormProps) {
  const [formData, setFormData] = useState<LoginData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  function updateField<K extends keyof LoginData>(
    field: K,
    value: LoginData[K],
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
      await onLogin({
        ...formData,
        email: formData.email.trim(),
        password: formData.password.trim(),
      });
      setFormData(initialFormData);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to login.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>

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
          type="password"
          required
          value={formData.password}
          onChange={(event) => updateField("password", event.target.value)}
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
