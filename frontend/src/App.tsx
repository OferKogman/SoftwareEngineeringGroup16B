import { BrowserRouter, Route, Routes } from "react-router-dom";
import ViewDiscountPolicy, {
  type DiscountPolicyData,
} from "./Components/CreateDiscountPolicy";
import EventCreationForm, {
  type EventCreationData,
} from "./Components/EventCreationForm";
import EventUpdateForm, {
  type EventUpdateDetails,
} from "./Components/EventUpdateForm";
import LoginForm, { type LoginData } from "./Components/LoginFrom";
import PaymentForm from "./Components/PaymentForm";
import CreateProductionCompany, {
  type ProductionCompanyDTO,
} from "./Components/ProdctionCompanyForm";
import PurchasePolicyCreationForm, {
  type PurchasePolicyCreationData,
} from "./Components/PurchasePolicyCreationForm";
import RegistrationForm from "./Components/RegistrationForm";
import SearchEvents from "./Components/SearchEvents";
import VenueEditor from "./Components/VenueEditor";
import ViewCompanyEvents from "./Components/ViewCompanyEventList";
import ViewEvent from "./Components/ViewEvent";
import ViewOrder from "./Components/ViewOrder";

import { useEffect, useState } from "react";
import CreateOrderPage from "./Components/CreateOrder";
import ViewAdminCompanyList from "./Components/ViewAdminCompanyList";
import AdminPurchaseHistory from "./Components/ViewAdminPurcheseHistory";
import ProductionCompanyPurchaseHistory from "./Components/ViewProductionCompanyPurchaseHistory";
import ViewUserCompanyList from "./Components/ViewUserCompanyList";
import UserPurchaseHistory from "./Components/ViewUserPurchaseHistory";
import ViewUsers from "./Components/ViewUsersList";
import { type VenueData } from "./DTOs/VenueDTO";
import AssignMember from "./Components/AssignNewOwnerOrManager";
import ChangeManagerPermissions,
  {type ManagerPermissions  
}from "./Components/ChangeManagerPermissions";

function App() {
  const [theme, setTheme] = useState(localStorage.getItem("theme") || "light");
  useEffect(() => {
    localStorage.setItem("theme", theme);
  }, [theme]);

  async function handlePayment(paymentData: {
    firstName: string;
    lastName: string;
    idNumber: string;
    cardNumber: string;
    expiryDate: string;
    cvv: string;
    amount: number;
  }) {
    console.log("Payment submitted:", paymentData);
  }
  async function handleCreatePurchasePolicy(
    policyData: PurchasePolicyCreationData,
  ) {
    console.log("Purchase policy created:", policyData);
  }
  async function handleCreateEvent(eventData: EventCreationData) {
    console.log("Event created:", eventData);
  }
  async function handleUpdateEvent(eventDetails: EventUpdateDetails) {
    console.log("Event updated:", eventDetails);
  }
  async function handleDiscountPolicySubmit(policyData: DiscountPolicyData) {
    console.log("Discount policy submitted:", policyData);
  }
  async function handleAdminLogin(adminLoginData: LoginData) {
    console.log("Admin Logged in:", adminLoginData);
  }
  async function handleCreateProductionCompany(
    companyData: ProductionCompanyDTO,
  ) {
    console.log("Production company created:", companyData);
  }
  async function handleEditEvent(companyID: number, eventID: number) {
    console.log("Edit event:", companyID, eventID);
  }
  async function handleDeleteEvent(eventID: number) {
    console.log("Delete event:", eventID);
  }
  async function handleManageCompany(companyID: number) {
    console.log("Manage company:", companyID);
  }
  async function handleUserLogin(userLoginData: LoginData) {
    console.log("User Logged in:", userLoginData);
  }
  async function handleUserRegistration(registrationData: {
    email: string;
    password: string;
  }) {
    console.log("User registered:", registrationData);
  }

  async function handleAdminRegistration(registrationData: {
    email: string;
    password: string;
  }) {
    console.log("Admin registered:", registrationData);
  }
  async function handleSubmitVenue(venueData: VenueData) {
    console.log("Venue saved:", venueData);
  }
  async function handleAssignMember(data: Parameters<typeof AssignMember>[0]["onSubmit"] extends (d: infer D) => any ? D : never) {
    console.log("Member assigned:", data);
  }
async function handleChangeManagerPermissions(
  targetID: string,
  companyID: number,
  newPermissions: Set<ManagerPermissions>
) {
  console.log("Manager permissions changed:", targetID, companyID, newPermissions);
}

  return (
    <BrowserRouter>
      <div className={`app ${theme}`}>
        <button
          onClick={() =>
            setTheme((current) => (current === "light" ? "dark" : "light"))
          }
        >
          switch to {theme === "light" ? "dark" : "light"} mode
        </button>
      </div>

      <Routes>
        <Route path="/events/:eventID" element={<ViewEvent />} />
        <Route path="/orders/:orderID" element={<ViewOrder />} />
        <Route
          path="/checkout/"
          element={<PaymentForm amount={250} onPaymentSubmit={handlePayment} />}
        />

        <Route
          path="/create-purchase-policy/"
          element={
            <PurchasePolicyCreationForm
              onCreatePolicy={handleCreatePurchasePolicy}
            />
          }
        />

        <Route
          path="/create-production-company/"
          element={
            <CreateProductionCompany onSubmit={handleCreateProductionCompany} />
          }
        />

        <Route
          path="/company/:companyID/events"
          element={
            <ViewCompanyEvents
              onEditEvent={handleEditEvent}
              onDeleteEvent={handleDeleteEvent}
            />
          }
        />

        <Route
          path="/create-event/"
          element={<EventCreationForm onCreateEvent={handleCreateEvent} />}
        />

        <Route path="/events/search" element={<SearchEvents />} />

        <Route
          path="/company/:companyID/event-update/:eventID"
          element={<EventUpdateForm onUpdateEvent={handleUpdateEvent} />}
        />

        <Route
          path="/create-discount-policy/"
          element={<ViewDiscountPolicy onSubmit={handleDiscountPolicySubmit} />}
        />

        <Route
          path="/admin/view-users"
          element={<ViewUsers users={undefined} />}
        />

        <Route
          path="/admin/login"
          element={<LoginForm onLogin={handleAdminLogin} title="Admin Login" />}
        />

        <Route
          path="/user/login/member"
          element={<LoginForm onLogin={handleUserLogin} title="User Login" />}
        />
        <Route
          path="/admin/purchase-history"
          element={<AdminPurchaseHistory />}
        />
        <Route
          path="/user/purchase-history"
          element={<UserPurchaseHistory />}
        />
        <Route
          path="/company/:companyID/purchase-history"
          element={<ProductionCompanyPurchaseHistory />}
        />

        <Route
          path="/user/register"
          element={
            <RegistrationForm
              onRegistration={handleUserRegistration}
              title="User Registration"
            />
          }
        />

        <Route
          path="/admin/register"
          element={
            <RegistrationForm
              onRegistration={handleAdminRegistration}
              title="Admin Registration"
            />
          }
        />

        <Route path="user/create-order" element={<CreateOrderPage />} />
        <Route
          path="/venue/:venueID?"
          element={<VenueEditor onSubmitVenue={handleSubmitVenue} />}
        />

        <Route
          path="user/:userID/companies"
          element={
            <ViewUserCompanyList onManageCompany={handleManageCompany} />
          }
        />
        <Route path="admin/companies" element={<ViewAdminCompanyList />} />

        <Route
          path="/company/:companyID/assign-member"
          element={<AssignMember onSubmit={handleAssignMember} />}
        />
        <Route
          path="/company/:companyID/change-manager-permissions"
          element={<ChangeManagerPermissions onSubmit={handleChangeManagerPermissions} />}
        />
      </Routes>
    </BrowserRouter>
  );
}
export default App;