import ViewSaleHistory from "../Components/ViewSaleHistory";
import type { OrderDTO } from "../DTOs/OrderDTO";

export default function TestSaleHistory() {
  const mockOrders: OrderDTO[] = [
    {
      orderId: "order1",
      segmentId: "A",
      numOfTickets: 2,
      orderType: "Seat",
      tocalOrderPrice: 250,
      eventId: 1,
      subjectId: "42",
    },
    {
      orderId: "order2",
      segmentId: "B",
      numOfTickets: 4,
      orderType: "Field",
      tocalOrderPrice: 400,
      eventId: 2,
      subjectId: "42",
    },
    {
      orderId: "order3",
      segmentId: "VIP",
      numOfTickets: 1,
      orderType: "Seat",
      tocalOrderPrice: 1000,
      eventId: 1,
      subjectId: "99",
    },
  ];

  return <ViewSaleHistory orders={mockOrders} />;
}
