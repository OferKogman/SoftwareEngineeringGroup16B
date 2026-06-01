import { Route, Routes } from "react-router-dom";
import SearchEvents from "./Components/Event/SearchEvents";
import ViewEvent from "./Components/Event/ViewEvent";
import LoginForm, { type LoginData } from "./Components/User/LoginForm";
import type { RegistrationData } from "./Components/User/RegistrationForm";
import RegistrationForm from "./Components/User/RegistrationForm";

export default function AppRoutes() {
  async function handleLogin(data: LoginData) {
    console.log("Logging in with data:", data);
    return Promise.resolve();
  }

  async function handleRegister(data: RegistrationData) {
    console.log("Registering with data:", data);
    return Promise.resolve();
  }

  return (
    <Routes>
      <Route
        path="/login"
        element={<LoginForm title="Login" onLogin={handleLogin} />}
      />
      <Route
        path="/register"
        element={
          <RegistrationForm title="Register" onRegistration={handleRegister} />
        }
      />

      <Route path="/events/search" element={<SearchEvents />} />

      <Route path="/events/:eventID" element={<ViewEvent />} />
    </Routes>
  );
}
