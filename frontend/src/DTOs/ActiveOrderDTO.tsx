export type ActiveOrderDTO = {
    orderId: string;
    segmentId: string;
    numOfTickets: number;
    orderType: "SEAT" | "FIELD" | string;
    tocalOrderPrice: number;
    eventId: number;
    subjectId: string;
    orderStartTime: number;
    seats: string[];
};