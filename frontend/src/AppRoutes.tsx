import { Navigate, Route, Routes } from "react-router-dom";
import AdminLoginForm from "./Components/Admin/AdminLoginForm";
import AdminManagement from "./Components/Admin/AdminManagement";
import EditDiscountPolicy from "./Components/EditDiscountPolicy";
import EditPurchasePolicy from "./Components/EditPurchasePolicy";
import EventCreationForm from "./Components/Event/EventCreationForm";
import EventManagement from "./Components/Event/EventManagement";
import EventUpdateForm from "./Components/Event/EventUpdateForm";
import SearchEvents from "./Components/Event/SearchEvents";
import ViewEvent from "./Components/Event/ViewEvent";
import CompanyEvents from "./Components/ProdactionCompany/CompanyEvents";
import CompanySettings from "./Components/ProdactionCompany/CompanySettings";
import HierarchyTree from "./Components/ProdactionCompany/HierarchyTree";
import MembersPermissions from "./Components/ProdactionCompany/MembersPermissions";
import ProductionCompanyMenegment from "./Components/ProdactionCompany/ProductionCompanyMenegment";
import TotalRevenue from "./Components/ProdactionCompany/TotalRevenue";
import VenueEditor from "./Components/ProdactionCompany/VenueEditor";
import ProductionCompanyPurchaseHistory from "./Components/ProdactionCompany/ViewProductionCompanyPurchaseHistory";
import ChangePasswordForm from "./Components/User/ChangePasswordForm";
import LoginForm from "./Components/User/UserLoginForm";
import UserManagement from "./Components/User/UserManagement";
import RegistrationForm from "./Components/User/UserRegistrationForm";
import ViewUserCompanyList from "./Components/User/ViewUserCompanyList";
import ViewUserPurchaseHistory from "./Components/User/ViewUserPurchaseHistory";

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginForm title="Login" />} />
      <Route path="/register" element={<RegistrationForm title="Register" />} />
      <Route path="/users/management" element={<UserManagement />}>
        <Route
          path="change-password"
          element={<ChangePasswordForm title={""} />}
        />
        <Route path="purchase-history" element={<ViewUserPurchaseHistory />} />
        <Route path="companies" element={<ViewUserCompanyList />} />
      </Route>
      <Route path="/admins" element={<AdminManagement />} />
      <Route
        path="/admins/login"
        element={<AdminLoginForm title="Admin Login" />}
      />
      <Route path="/admins/management" element={<AdminManagement />} />
      <Route path="/events/search" element={<SearchEvents />} />
      <Route path="/events/:eventID/management" element={<EventManagement />}>
        <Route index element={<Navigate to="show" />} />
        <Route path="show" element={<ViewEvent />} />
        <Route path="update-info" element={<EventUpdateForm />} />
        <Route path="inventory" element={<VenueEditor />} />
        <Route path="discount-policy" element={<EditDiscountPolicy />} />
        <Route path="purchase-policy" element={<EditPurchasePolicy />} />
      </Route>
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
        <Route path="events" element={<CompanyEvents />} />
        <Route path="venue-config" element={<VenueEditor />} />
        <Route path="members" element={<MembersPermissions />} />
        <Route path="hierarchy" element={<HierarchyTree />} />
        <Route path="settings" element={<CompanySettings />} />
        <Route path="events/create" element={<EventCreationForm />} />
      </Route>
      <Route path="/events/:eventID" element={<ViewEvent />} />
    </Routes>
  );
}
