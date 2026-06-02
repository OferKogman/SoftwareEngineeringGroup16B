import { Route, Routes } from "react-router-dom";
import AdminLoginForm from "./Components/Admin/AdminLoginForm";
import AdminManagement from "./Components/Admin/AdminManagement";
import EventCreationForm from "./Components/Event/EventCreationForm";
import EventUpdateForm from "./Components/Event/EventUpdateForm";
import SearchEvents from "./Components/Event/SearchEvents";
import ViewEvent from "./Components/Event/ViewEvent";
import CompanyEvents from "./Components/ProdactionCompany/CompanyEvents";
import CompanySettings from "./Components/ProdactionCompany/CompanySettings";
import CreateProdactionCompany from "./Components/ProdactionCompany/CreateProductionCompany";
import HierarchyTree from "./Components/ProdactionCompany/HierarchyTree";
import MembersPermissions from "./Components/ProdactionCompany/MembersPermissions";
import ProductionCompanyMenegment from "./Components/ProdactionCompany/ProductionCompanyMenegment";
import TotalRevenue from "./Components/ProdactionCompany/TotalRevenue";
import ProductionCompanyPurchaseHistory from "./Components/ProdactionCompany/ViewProductionCompanyPurchaseHistory";
import LoginForm from "./Components/User/UserLoginForm";
import UserManagement from "./Components/User/UserManagement";
import RegistrationForm from "./Components/User/UserRegistrationForm";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginForm title="Login" />} />
      <Route path="/register" element={<RegistrationForm title="Register" />} />
      <Route path="/users" element={<UserManagement />} />

      <Route path="/admins" element={<AdminManagement />} />
      <Route
        path="/admins/login"
        element={<AdminLoginForm title="Admin Login" />}
      />
      <Route path="/admins/management" element={<AdminManagement />} />

      <Route path="/events/search" element={<SearchEvents />} />

      <Route path="/companies/create" element={<CreateProdactionCompany />} />

      <Route
        path="/companies/:companyId"
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
        <Route path="events/create" element={<EventCreationForm />} />
        <Route path="events/:eventID/manage" element={<EventUpdateForm />} />
      </Route>
      <Route path="/events/:eventID" element={<ViewEvent />} />
    </Routes>
  );
}
