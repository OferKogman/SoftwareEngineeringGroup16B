import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useSession } from "../../App";
import "../User/CSS/UserLoginForm.css";

export type AdminRegisterData = {
  username: string;
  email: string;
  password: string;
};

type AdminRegisterFormProps = {
  title: string;
};

const initialFormData: AdminRegisterData = {
  username: "",
  email: "",
  password: "",
};

export default function AdminRegisterForm({ title }: AdminRegisterFormProps) {
  const [formData, setFormData] = useState<AdminRegisterData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { sessionToken, setSessionToken } = useSession();

  const navigate = useNavigate();

  async function onAdminRegister({
    username,
    email,
    password,
  }: AdminRegisterData) {
    try {
      console.log("token:", sessionToken);
      const response = await fetch(
        `http://localhost:8080/api/admin/registerNewAdmin`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: sessionToken,
          },
          body: JSON.stringify({ username, password, email }),
        },
      );

      if (!response.ok) {
        throw new Error(await response.text());
      }
      const token: string = await response.text();

      setSessionToken(token);
      navigate("/admins/management");
      console.log("Admin successfully registered");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to register.");
    }
  }

  function updateField<K extends keyof AdminRegisterData>(
    field: K,
    value: AdminRegisterData[K],
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
      await onAdminRegister({
        ...formData,
        email: formData.email.trim(),
        password: formData.password.trim(),
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to register.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="register-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>
      {error && <p className="form-error">{error}</p>}

      <div className="form-row">
        <label>Username</label>
        <input
          type="text"
          required
          value={formData.username}
          onChange={(event) => {
            event.currentTarget.setCustomValidity("");
            updateField("username", event.target.value);
          }}
          placeholder="Username"
        />
      </div>

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
    </form>
  );
}
