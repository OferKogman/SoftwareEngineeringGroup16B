import { useState } from "react";
import { useSession } from "../../App";

export type ChangePasswordData = {
  oldPassword: string;
  newPassword: string;
};

type ChangePasswordFormProps = {
  title: string;
  onCancel?: () => void;
};

const initialFormData: ChangePasswordData = {
  oldPassword: "",
  newPassword: "",
};

export default function ChangePasswordForm({
  title,
  onCancel,
}: ChangePasswordFormProps) {
  const [formData, setFormData] = useState<ChangePasswordData>(initialFormData);
  const [confirmPassword, setConfirmPassword] = useState("");

  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { sessionToken } = useSession();

  function updateField<K extends keyof ChangePasswordData>(
    field: K,
    value: ChangePasswordData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();

    setError("");

    if (formData.newPassword !== confirmPassword) {
      setError("New passwords do not match.");
      return;
    }

    setIsSubmitting(true);

    try {
      const response = await fetch(
        "http://localhost:8080/api/user/updateUserPassword",
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
            Authorization: sessionToken,
          },
          body: JSON.stringify({
            oldPassword: formData.oldPassword,
            newPassword: formData.newPassword,
          }),
        },
      );

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Failed to change password.");
      }

      setFormData(initialFormData);
      setConfirmPassword("");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to change password.",
      );
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="change-password-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>

      {error && <p className="form-error">{error}</p>}

      <div className="form-row">
        <label>Old Password</label>

        <input
          type="password"
          required
          value={formData.oldPassword}
          onChange={(event) => updateField("oldPassword", event.target.value)}
          placeholder="Old Password"
        />
      </div>

      <div className="form-row">
        <label>New Password</label>

        <input
          type="password"
          required
          value={formData.newPassword}
          onChange={(event) => updateField("newPassword", event.target.value)}
          placeholder="New Password"
        />
      </div>

      <div className="form-row">
        <label>Confirm New Password</label>

        <input
          type="password"
          required
          value={confirmPassword}
          onChange={(event) => setConfirmPassword(event.target.value)}
          placeholder="Confirm New Password"
        />
      </div>

      <div className="form-actions">
        {onCancel && (
          <button type="button" onClick={onCancel} disabled={isSubmitting}>
            Cancel
          </button>
        )}

        <button
          type="submit"
          disabled={
            isSubmitting ||
            !formData.oldPassword ||
            !formData.newPassword ||
            !confirmPassword
          }
        >
          {isSubmitting ? "Changing password..." : "Submit Change"}
        </button>
      </div>
    </form>
  );
}
