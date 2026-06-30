import { useState } from "react";
import { useSession } from "../../GlobalContext/SessionContext";
import { useApiFetch } from "../../apiFetch";

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
  const [message, setMessage] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const { sessionToken } = useSession();

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

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

    setMessage("");
    setError("");

    if (formData.newPassword !== confirmPassword) {
      setError("New passwords do not match.");
      return;
    }

    setIsSubmitting(true);

    try {
      if (!sessionToken) {
        setError("Missing session token.");
        return;
      }
      const response = await apiFetch(
        "http://localhost:8080/api/user/updateUserPassword",
        {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            oldPassword: formData.oldPassword,
            newPassword: formData.newPassword,
          }),
        },
      );

      const responseText = await response.text();

      if (!response.ok) {
        throw new Error(responseText);
      }

      setFormData(initialFormData);
      setConfirmPassword("");
      setMessage("Password changed successfully.");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="change-password-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>

      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

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
