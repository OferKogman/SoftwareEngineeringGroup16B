import { BrowserRouter, Routes, Route } from "react-router-dom";
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
import ViewEvent from "./Components/ViewEvent";
import ViewDiscountPolicy, {
  type DiscountPolicyDTO,
} from "./Components/ViewDiscountPolicy";

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
  async function handleUpdateEvent(eventData: EventUpdateDetails) {
    console.log("Event updated:", eventData);
  }
  async function handleDiscountPolicySubmit(policyData: DiscountPolicyDTO) {
    console.log("Discount policy submitted:", policyData);
  }

  return (
    <>
      <h1>Payment</h1>
      <PaymentForm amount={250} onPaymentSubmit={handlePayment} />

      <h1>Create Purchase Policy</h1>
      <PurchasePolicyCreationForm onCreatePolicy={handleCreatePurchasePolicy} />

      <h1>Create Event</h1>
      <EventCreationForm onCreateEvent={handleCreateEvent} />
      <h1>Discount Policy</h1>
      <ViewDiscountPolicy onSubmit={handleDiscountPolicySubmit} />
      <BrowserRouter>
        <Routes>
          <Route path="/events/:id" element={<ViewEvent />} />
          <Route
            path="/eventupdate/:id"
            element={<EventUpdateForm onUpdateEvent={handleUpdateEvent} />}
          />
          <Route
            path="/discount-policies/:id"
            element={<ViewDiscountPolicy onSubmit={handleDiscountPolicySubmit} />}
          />
        </Routes>
      </BrowserRouter>
    </>
  );
}
export default App;