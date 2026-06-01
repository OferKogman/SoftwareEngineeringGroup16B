import { Route, Routes } from "react-router-dom";
import SearchEvents from "./Components/Event/SearchEvents";
import LoginForm, { type LoginData } from "./Components/User/LoginForm";
import type { RegistrationData } from "./Components/User/RegistrationForm";
import RegistrationForm from "./Components/User/RegistrationForm";
import ProductionCompanyMenegment from "./Components/ProdactionCompany/ProductionCompanyMenegment";
import ProductionCompanyPurchaseHistory from "./Components/ProdactionCompany/ViewProductionCompanyPurchaseHistory";
import TotalRevenue from "./Components/ProdactionCompany/TotalRevenue";
import VenueConfiguration from "./Components/ProdactionCompany/CompanyEvents";
import MembersPermissions from "./Components/ProdactionCompany/MembersPermissions";
import HierarchyTree from "./Components/ProdactionCompany/HierarchyTree";
import CompanySettings from "./Components/ProdactionCompany/CompanySettings";
import CompanyEvents from "./Components/ProdactionCompany/CompanyEvents";
import CreateEvent from "./Components/EventCreationForm";
import ManageEvent from "./Components/ProdactionCompany/ManageEvent";

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

      <Route
        path="/production-company-menegment/:companyId"
        element={<ProductionCompanyMenegment />}
      >
        <Route index element={<TotalRevenue />} />
        <Route
          path="sales-history"
          element={<ProductionCompanyPurchaseHistory />}
        />
        <Route path="total-revenue" element={<TotalRevenue />} />
        <Route path="venue-config" element={<CompanyEvents />} />
        <Route path="members" element={<MembersPermissions />} />
        <Route path="hierarchy" element={<HierarchyTree />} />
        <Route path="settings" element={<CompanySettings />} />
        <Route path="events/create"element={<CreateEvent />}/>
        <Route path="events/:eventId/manage" element={<ManageEvent />}/>
      </Route>
    </Routes>
  );
}
