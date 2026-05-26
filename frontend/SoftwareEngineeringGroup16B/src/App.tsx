import PaymentForm from "./PaymentForm"


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
  return (
    <>
        <h1>Payment</h1>
        <PaymentForm amount={250} onPaymentSubmit={handlePayment} />
    </>
  )
  
}
export default App
