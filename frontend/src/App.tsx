import { BrowserRouter, Route, Routes } from "react-router-dom";
import AdminLoginForm, {
  type AdminLoginData,
} from "./Components/AdminLoginForm";
import EventCreationForm, {
  type EventCreationData,
} from "./Components/EventCreationForm";
import EventUpdateForm, {
  type EventUpdateDetails,
} from "./Components/EventUpdateForm";
import PaymentForm from "./Components/PaymentForm";
import PurchasePolicyCreationForm, {
  type PurchasePolicyCreationData,
} from "./Components/PurchasePolicyCreationForm";
import ViewDiscountPolicy, {
  type DiscountPolicyDTO,
} from "./Components/ViewDiscountPolicy";
import ViewEvent from "./Components/ViewEvent";
import CreateProductionCompany, {
  type ProductionCompanyDTO,
} from "./Components/ProdctionCompanyForm";
import ViewCompanyEvents from "./Components/ViewCompanyEventList";

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
  async function handleDiscountPolicySubmit(policyData: DiscountPolicyDTO) {
    console.log("Discount policy submitted:", policyData);
  }
  async function handleAdminLogin(adminLoginData: AdminLoginData) {
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

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/events/:eventID" element={<ViewEvent />} />
        <Route
          path="/checkout/"
          element={
            <PaymentForm amount={250} onPaymentSubmit={handlePayment} />
          }
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
        <Route
          path="/company/:companyID/event-update/:eventID"
          element={<EventUpdateForm onUpdateEvent={handleUpdateEvent} />}
        />
        <Route
          path="/create-discount-policy/"
          element={
            <ViewDiscountPolicy onSubmit={handleDiscountPolicySubmit} />
          }
        />
        <Route
          path="/admin/login"
          element={<AdminLoginForm onAdminLogin={handleAdminLogin} />}
        />
      </Routes>
    </BrowserRouter>
  );
}
export default App;