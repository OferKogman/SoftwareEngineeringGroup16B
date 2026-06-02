import { Route, Routes } from "react-router-dom";
import SearchEvents from "./Components/Event/SearchEvents";
import ViewEvent from "./Components/Event/ViewEvent";
import CreateEvent from "./Components/EventCreationForm";
import CompanyEvents from "./Components/ProdactionCompany/CompanyEvents";
import CompanySettings from "./Components/ProdactionCompany/CompanySettings";
import HierarchyTree from "./Components/ProdactionCompany/HierarchyTree";
import ManageEvent from "./Components/ProdactionCompany/ManageEvent";
import MembersPermissions from "./Components/ProdactionCompany/MembersPermissions";
import ProductionCompanyMenegment from "./Components/ProdactionCompany/ProductionCompanyMenegment";
import TotalRevenue from "./Components/ProdactionCompany/TotalRevenue";
import ProductionCompanyPurchaseHistory from "./Components/ProdactionCompany/ViewProductionCompanyPurchaseHistory";
import CreateProductionCompany from "./Components/ProdctionCompanyForm";
import LoginForm from "./Components/User/LoginForm";
import RegistrationForm from "./Components/User/RegistrationForm";
import UserManagement from "./Components/User/UserManagement";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginForm title="Login" />} />
      <Route path="/register" element={<RegistrationForm title="Register" />} />
      <Route path="/users" element={<UserManagement />} />

      <Route path="/events/search" element={<SearchEvents />} />

      <Route path="/companies/create" element={<CreateProductionCompany />} />
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
        <Route path="events/create" element={<CreateEvent />} />
        <Route path="events/:eventId/manage" element={<ManageEvent />} />
      </Route>
      <Route path="/events/:eventID" element={<ViewEvent />} />
    </Routes>
  );
}
