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
import ViewCompanyEvents from "./Components/ViewCompanyEventList";
import ViewDiscountPolicy, {
  type DiscountPolicyDTO,
} from "./Components/ViewDiscountPolicy";
import ViewEvent from "./Components/ViewEvent";
import { type VenueData } from "./DTOs/VenueDTO";
import VenueEditor from "./Components/VenueEditor";

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
  async function handleDeleteEvent(id: number) {
    console.log("Event Deleted:", id);
  }
  async function handleEditEvent(companyID: number, eventID: number) {
    window.location.href = `/company/${companyID}/event-update/${eventID}`;
  }
  async function handleSubmitVenue(venueData: VenueData) {
    console.log("Venue saved:", venueData);
  }

  return (
    <>
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
          <Route
            path="/venue/:venueID?"
            element={<VenueEditor onSubmitVenue={handleSubmitVenue} />}
          />
        </Routes>
      </BrowserRouter>
    </>
  );
}
export default App;
