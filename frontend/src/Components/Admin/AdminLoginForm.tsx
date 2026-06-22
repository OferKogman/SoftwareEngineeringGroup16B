import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import { useAdminLoggedIn } from "../../GlobalContext/AdminLoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import "../User/CSS/UserLoginForm.css";

export type AdminLoginData = {
  username: string;
  email: string;
  password: string;
};

type AdminLoginFormProps = {
  title: string;
};

const initialFormData: AdminLoginData = {
  username: "",
  email: "",
  password: "",
};

export default function AdminLoginForm({ title }: AdminLoginFormProps) {
  const [formData, setFormData] = useState<AdminLoginData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { sessionToken, setSessionToken } = useSession();
  const { adminLoggedIn, setAdminLoggedIn } = useAdminLoggedIn();
  const apiFetch = useApiFetch();

  const navigate = useNavigate();

  async function onAdminLogin({ username, email, password }: AdminLoginData) {
    try {
      console.log("token:", sessionToken);
      const response = await apiFetch(`http://localhost:8080/api/admin/login`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({ username, password, email }),
      });

      if (!response.ok) {
        throw new Error(await response.text());
      }
      const token: string = await response.text();

      setSessionToken(token);
      setAdminLoggedIn(true);
      navigate("/admins");
      console.log("Admin successfully logged in");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to login.");
    }
  }

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
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to login.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return !adminLoggedIn ? (
    <form className="login-form" onSubmit={handleSubmit}>
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
          {isSubmitting ? "Logging in..." : "Login"}
        </button>
      </div>
    </form>
  ) : (
    <div>Already Logged in as admin</div>
  );
}
