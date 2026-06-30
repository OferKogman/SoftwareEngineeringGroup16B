import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { UserDTO } from "../../DTOs/UserDTO";
import "./CSS/UserRegistrationForm.css";

export type UserRegistrationData = {
  email: string;
  password: string;
};

export type UserRegistrationFormProps = {
  title: string;
};

const initialFormData: UserRegistrationData = {
  email: "",
  password: "",
};

export default function UserRegistrationForm({
  title,
}: UserRegistrationFormProps) {
  const navigate = useNavigate();
  const [formData, setFormData] =
    useState<UserRegistrationData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [message, setMessage] = useState("");

  const apiFetch = useApiFetch();

  function closePopup() {
    setMessage("");
    setError("");
  }

  function closeSuccessPopup() {
    setMessage("");
    setFormData(initialFormData);
    navigate("/login");
  }

  async function onUserRegistration({ email, password }: UserRegistrationData) {
    try {
      const response = await apiFetch(
        `http://localhost:8080/api/user/registerUser`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ email, password }),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }
      const user: UserDTO = await response.json();

      console.log("User successfully registered:", user);
      setMessage(
        "Registered successfully. Your account was created. You can now log in.",
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    }
  }

  function updateField<K extends keyof UserRegistrationData>(
    field: K,
    value: UserRegistrationData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setMessage("");
    setError("");

    try {
      await onUserRegistration({
        ...formData,
        email: formData.email.trim(),
        password: formData.password.trim(),
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="registration-form" onSubmit={handleSubmit}>
      {message && (
        <div className="settings-alert">
          <p>{message}</p>
          <button type="button" onClick={closeSuccessPopup}>
            {" "}
            OK{" "}
          </button>
        </div>
      )}
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button type="button" onClick={closePopup}>
            {" "}
            OK{" "}
          </button>
        </div>
      )}
      {!message && (
        <>
          <h2>{title}</h2>
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
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? "Registering..." : "Register"}
            </button>
          </div>

          <NavLink to="/login" className="auth-link">
            Already have an account? Login here
          </NavLink>
        </>
      )}
    </form>
  );
}
