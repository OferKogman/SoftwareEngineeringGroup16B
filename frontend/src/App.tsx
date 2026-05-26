import EventCreationForm, { type EventCreationData } from "./EventCreationForm";
import PaymentForm from "./PaymentForm";
import PurchasePolicyCreationForm, {
  type PurchasePolicyCreationData,
} from "./PurchasePolicyCreationForm";

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
  return (
    <>
      <h1>Payment</h1>
      <PaymentForm amount={250} onPaymentSubmit={handlePayment} />

      <h1>Create Purchase Policy</h1>
      <PurchasePolicyCreationForm onCreatePolicy={handleCreatePurchasePolicy} />

      <h1>Create Event</h1>
      <EventCreationForm onCreateEvent={handleCreateEvent} />
    </>
  );
}
export default App;
