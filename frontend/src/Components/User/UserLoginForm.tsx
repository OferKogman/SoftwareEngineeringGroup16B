import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import { useLoggedIn } from "../../GlobalContext/LoggedInContext";
import { useSession } from "../../GlobalContext/SessionContext";
import "./CSS/UserLoginForm.css";

export type UserLoginData = {
  email: string;
  password: string;
};

type UserLoginFormProps = {
  title: string;
};

const initialFormData: UserLoginData = {
  email: "",
  password: "",
};

export default function UserLoginForm({ title }: UserLoginFormProps) {
  const [formData, setFormData] = useState<UserLoginData>(initialFormData);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  const { sessionToken, setSessionToken } = useSession();
  const { setLoggedIn } = useLoggedIn();

  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  function closePopup() {
    setError("");
  }

  async function onUserLogin({ email, password }: UserLoginData) {
    try {
      if (!sessionToken) {
        setError("Missing session token.");
        return;
      }
      console.log("token:", sessionToken);
      const response = await apiFetch(
        `http://localhost:8080/api/user/login/member`,
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
      const token: string = await response.text();

      setSessionToken(token);
      setLoggedIn(true);
      navigate("/");
      console.log("User successfully logged in");
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    }
  }

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
    } catch (err) {
      setError(err instanceof Error ? err.message : "");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <h2>{title}</h2>
      {error && (
        <div className="settings-alert">
          <p>{error}</p>
          <button onClick={closePopup}> OK </button>
        </div>
      )}

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

      <p>
        <NavLink to="/register" className="auth-link">
          Don&apos;t have an account? Register here
        </NavLink>
      </p>
    </form>
  );
}
