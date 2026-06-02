import { useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import { useLoggedIn, useSession } from "../../App";
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
  const { loggedIn, setLoggedIn } = useLoggedIn();

  const navigate = useNavigate();

  async function onUserLogin({ email, password }: UserLoginData) {
    try {
      console.log("token:", sessionToken);
      const response = await fetch(
        `http://localhost:8080/api/user/login/member`,
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
            Authorization: sessionToken,
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
      setError(err instanceof Error ? err.message : "Failed to login.");
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
      setError(err instanceof Error ? err.message : "Failed to login.");
    } finally {
      setIsSubmitting(false);
    }
  }

  return (
    <form className="login-form" onSubmit={handleSubmit}>
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
