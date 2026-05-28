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
import PaymentForm from "./Components/PaymentForm";
import PurchasePolicyCreationForm, {type PurchasePolicyCreationData,} from "./Components/PurchasePolicyCreationForm";
import RegistrationForm from "./Components/RegistrationForm";
import SearchEvents from "./Components/SearchEvents";
import VenueEditor from "./Components/VenueEditor";
import ViewCompanyEvents from "./Components/ViewCompanyEventList";
import ViewEvent from "./Components/ViewEvent";
import ViewOrder from "./Components/ViewOrder";
import CreateProductionCompany, {type ProductionCompanyDTO,} from "./Components/ProdctionCompanyForm";
import LoginForm, { type LoginData } from "./Components/LoginFrom";
import AdminViewPurchaseHistory, {type PurchaseHistorySearchData,} from "./Components/ViewAdminPurcheseHistory";

import { type VenueData } from "./DTOs/VenueDTO";
import CreateOrderPage from "./Components/CreateOrder";
import UserPurchaseHistory from "./Components/ViewUserPurchaseHistory";
import ProductionCompanyPurchaseHistory from "./Components/ViewProductionCompanyPurchaseHistory";
import AdminPurchaseHistory from "./Components/ViewAdminPurcheseHistory";

function App() {
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
  async function handleUserLogin(userLoginData: LoginData) {
    console.log("User Logged in:", userLoginData);
  }
  async function handleAdminPurchaseHistory(searchData: PurchaseHistorySearchData) {
    console.log("Admin Search purchase history:", searchData);
  }
  async function handleUserPurchaseHistory(searchData: PurchaseHistorySearchData) {
    console.log("User Search purchase history:", searchData);
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

  return (
    <BrowserRouter>
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
          path="/admin/login"
          element={<LoginForm onLogin={handleAdminLogin} title="Admin Login" />}
        />

        <Route
          path="/user/login"
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
      </Routes>
      
    </BrowserRouter>
  );
}
export default App;
