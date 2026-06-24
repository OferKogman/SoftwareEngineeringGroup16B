import { useEffect, useRef, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import type {
  ChosenSeatingSegDTO,
  FieldSegDTO,
  SeatDTO,
  VenueDTO,
} from "../../DTOs/VenueDTO";
import VenueDisplay from "../Shared/VenueDisplay";
import "./CSS/CreateOrder.css";
import { useSession } from "../../GlobalContext/SessionContext";
import type { ActiveOrderDTO } from "../../DTOs/ActiveOrderDTO";


const API_BASE = "http://localhost:8080";
const ORDER_LIFETIME_MS = 10 * 60 * 1000;

export default function EditOrderPage() {
    const navigate = useNavigate();
    const { sessionToken } = useSession();
    const [activeOrder, setActiveOrder] = useState<ActiveOrderDTO | null>(null);
    const [timeLeft, setTimeLeft] = useState("");
    const [orderId, setOrderId] = useState("");
    const didAutoCancelRef = useRef(false);


    const [venueID, setVenueID] = useState("");
    const [venue, setVenue] = useState<VenueDTO | null>(null);
    const [eventID, setEventID] = useState<number | null>(null);

    const [selectedFieldSeg, setSelectedFieldSeg] = useState<FieldSegDTO | null>(
        null,
    );
    const [selectedSeatSeg, setSelectedSeatSeg] =
        useState<ChosenSeatingSegDTO | null>(null);

    const [fieldTicketAmount, setFieldTicketAmount] = useState("1");
    const [selectedSeats, setSelectedSeats] = useState<SeatDTO[]>([]);
    const [error, setError] = useState("");

    const apiFetch = useApiFetch();

    useEffect(() => {
        let cancelled = false;

        async function loadActiveOrder() {
            try{
                if (!sessionToken) {throw new Error("No session token found");}
                
                const orderResponse = await apiFetch(`${API_BASE}/api/user/me/active-order`, {
                    method: "GET",
                    headers: {
                    "Authorization": sessionToken,
                    "Content-Type": "application/json"
                }
                });

                if (!orderResponse.ok) {throw new Error(await orderResponse.text());}

                const orderData : ActiveOrderDTO = await orderResponse.json();
                if (cancelled) return;

                setActiveOrder(orderData);
                setOrderId(orderData.orderId);

                const loadedEventID = orderData.eventId;

                setEventID(loadedEventID);      
                setError("");
                if (!loadedEventID) {
                    setError("Order does not have an associated event.");
                    return;}
    
                const eventResponse = await apiFetch(`${API_BASE}/events/${loadedEventID}`, {
                method: "GET",
                });

                if (!eventResponse.ok) {throw new Error(await eventResponse.text());}

                const event: EventDTO = await eventResponse.json();
                const loadedVenueID = event.eventVenueID;
                if (cancelled) return;

                const venueResponse = await apiFetch(
                `${API_BASE}/venues/${loadedVenueID}`,
                {
                    method: "GET",
                },
                );

                if (!venueResponse.ok) {throw new Error(await venueResponse.text());}
                const venue: VenueDTO = await venueResponse.json();
                if (cancelled) {return;}
                setVenueID(loadedVenueID);
                setVenue(venue);

                const activeSegment = venue.segments[orderData.segmentId];

                if (!activeSegment) {throw new Error("Order segment was not found in venue.");}

                if ("size" in activeSegment) {
                    setSelectedFieldSeg(activeSegment);
                    setSelectedSeatSeg(null);
                    setSelectedSeats([]);
                    setFieldTicketAmount(String(orderData.numOfTickets));
                }

                if ("seats" in activeSegment) {
                    setSelectedSeatSeg(activeSegment);
                    setSelectedFieldSeg(null);

                    
                    const orderSeatIds = new Set(orderData.seats ?? []);
                    const orderSeats = Object.values(activeSegment.seats).filter((seat) =>
                      orderSeatIds.has(seat.seatId),

                    );
                    console.log("orderData.seats:", orderData.seats);
                    console.log("activeSegment seat IDs:", Object.values(activeSegment.seats).map((seat) => seat.seatId));
                    console.log("matched orderSeats:", orderSeats);

                    setSelectedSeats(orderSeats);
                }
                

            } catch (err) {
                if (!cancelled) {
                setError(
                    err instanceof Error ? err.message : "Failed to load venue.",
                );
                }
            }
            }

        void loadActiveOrder();
        
          return () => {
            cancelled = true;
          };
    }, [sessionToken, apiFetch]);


    useEffect(() => {
      if (!activeOrder) {
        return;
      }

      function updateTimer() {
        if (!activeOrder) {return;}

        const expiresAt = activeOrder.orderStartTime + ORDER_LIFETIME_MS;
        // TODO: delete
        console.log("=== TIMER DEBUG ===");
        console.log("orderStartTime:", activeOrder.orderStartTime);
        console.log("Date.now():", Date.now());
        console.log("orderStartTime as date:", new Date(activeOrder.orderStartTime));
        console.log(
          "orderStartTime * 1000 as date:",
          new Date(activeOrder.orderStartTime * 1000),
        );
        console.log(
          "expiresAt (milliseconds assumption):",
          new Date(activeOrder.orderStartTime + ORDER_LIFETIME_MS),
        );
        console.log(
          "expiresAt (seconds assumption):",
          new Date(activeOrder.orderStartTime * 1000 + ORDER_LIFETIME_MS),
        );
        // end delete
        const remaining = Math.max(0, expiresAt - Date.now());

        const minutes = Math.floor(remaining / 60000);
        const seconds = Math.floor((remaining % 60000) / 1000);

        setTimeLeft(`${minutes}:${seconds.toString().padStart(2, "0")}`);

        if (remaining === 0 && !didAutoCancelRef.current) {
          didAutoCancelRef.current = true;
          void cancelOrder();
        }
      }

      updateTimer();

      const intervalId = window.setInterval(updateTimer, 1000);

      return () => {window.clearInterval(intervalId);};
    }, [activeOrder]);

  function handleFieldSegmentSelected(segment: FieldSegDTO) {
    setError("");
    setSelectedSeatSeg(null);
    setSelectedSeats([]);
    setSelectedFieldSeg(segment);
    setFieldTicketAmount("1");
  }

  function handleSeatSegmentSelected(segment: ChosenSeatingSegDTO) {
    setError("");
    setSelectedFieldSeg(null);
    setSelectedSeats([]);
    setSelectedSeatSeg(segment);
  }

  function isTakenSeat(seat: SeatDTO) {
    if (!eventID) {
      setError("Missing Event ID");
      return;
    }
    if (!selectedSeatSeg) {
      return;
    }

    return !(
      selectedSeatSeg &&
      Object.keys(selectedSeatSeg.seats).includes(seat.seatId) &&
      !seat.stock[Number(eventID)]
    );
  }

  function handleSeatClick(seat: SeatDTO) {
    const alreadySelected = selectedSeats.some((selectedSeat) => selectedSeat.seatId === seat.seatId,);
    if (alreadySelected) {
      setSelectedSeats((current) => current.filter((selectedSeat) => selectedSeat.seatId !== seat.seatId),);
      return;
    }
    if (isTakenSeat(seat)) {
      return;
    }

    setSelectedSeats((current) => [...current, seat]);
  }

  async function updateFieldOrder(
    orderID: string,
    amount: number,
  ) {
    const response = await apiFetch(
      `${API_BASE}/api/order/changeNumOfSeatsInFieldOrder/${orderID}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(amount)
      },
    );

    const text = await response.text();

    if (!response.ok) {
      throw new Error(text || "Failed to reserve field.");
    }

  }

  async function updateSeatOrder(
    orderID: string,
    seats: SeatDTO[],
  ) {
    const seatIDs = seats.map((seat) => `${seat.row}-${seat.number}`);
    const response = await apiFetch(
      `${API_BASE}/api/order/changeSeatsToOrder/${orderID}`,
      {
        method: "PUT",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(seatIDs)
      },
    );

    const text = await response.text();

    if (!response.ok) {
      throw new Error(text || "Failed to reserve seats.");
    }

  }

  async function handleFieldOrder() {
    if (!eventID) {
      setError("Missing event ID.");
      return;
    }

    if (!selectedFieldSeg) {
      return;
    }

    if (!venueID) {
      setError("Missing venue ID.");
      return;
    }

    const amount = Number(fieldTicketAmount);
    const availableTickets = selectedFieldSeg.stocks[Number(eventID)];

    if (!Number.isInteger(amount) || amount <= 0) {
      setError("Please enter a valid ticket amount.");
      return;
    }

    if (amount > availableTickets) {
      setError(`Only ${availableTickets} tickets are available.`);
      return;
    }

    try {
      if (!activeOrder){return ;}
      await updateFieldOrder(orderId, amount);
      navigate("/payment", {
        state: {
          orderId,
          amount: selectedFieldSeg.eventPrices[Number(eventID)] * amount,
          secondsLeft: Math.min(10 * 60,Math.max(0,Math.floor((activeOrder?.orderStartTime + 10 * 60 * 1000 - Date.now()) / 1000,))),
          },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reserve field.");
    }
  }
  async function cancelOrder() {
    if (!orderId || !sessionToken) {
      navigate("/events/search");
      return;
    }

    try {
      await apiFetch(`${API_BASE}/api/order/cancelOrder/${orderId}`, {
        method: "PUT",
        headers: {
          Accept: "application/json",
        },
      });
    } finally {
      navigate("/events/search");
    }
  }

  async function handleSeatOrder() {
    if (!eventID) {
      setError("Missing event ID.");
      return;
    }

    if (!selectedSeatSeg) {
      return;
    }

    if (!venueID) {
      setError("Missing venue ID.");
      return;
    }

    if (selectedSeats.length === 0) {
      setError("Please choose at least one seat.");
      return;
    }

    try {
      await updateSeatOrder(orderId, selectedSeats);
      if (!activeOrder) {return ;}
      navigate("/payment", {
        state: {
          orderId,
          amount: selectedSeatSeg.eventPrices[Number(eventID)] * selectedSeats.length,
          secondsLeft: Math.min(10 * 60,Math.max(0,Math.floor((activeOrder?.orderStartTime + 10 * 60 * 1000 - Date.now()) / 1000,))),

        },
      });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reserve seats.");
    }
  }

  function renderFieldOrderPanel() {
    if (!selectedFieldSeg) {
      return null;
    }

    const total = selectedFieldSeg.size;
    const available = selectedFieldSeg.stocks[Number(eventID)];
    const taken = total - available;

    return (
      <div className="form-card">
        <h3>Field Section {selectedFieldSeg.segmentID}</h3>
        <p>
          Taken: {taken} / {total}
        </p>
        <p>Available: {available}</p>

        <input
          type="number"
          min="1"
          max={available}
          value={fieldTicketAmount}
          onChange={(event) => setFieldTicketAmount(event.currentTarget.value)}
        />
        <h3>
          Subtotal:{" "}
          {selectedFieldSeg.eventPrices[Number(eventID)] *
            Number(fieldTicketAmount)}
          $
        </h3>

        <button type="button" onClick={handleFieldOrder}>
          Order Field Tickets
        </button>
      </div>
    );
  }

  function handleEmptyAreaClick() {
    setSelectedFieldSeg(null);
    setSelectedSeatSeg(null);
    setSelectedSeats([]);
    setError("");
  }

  function handleVenueSeatClick(seat: SeatDTO, segment: ChosenSeatingSegDTO) {
    setError("");

    if (selectedSeatSeg?.segmentID !== segment.segmentID) {
      setSelectedFieldSeg(null);
      setSelectedSeatSeg(segment);
      setSelectedSeats([seat]);
      return;
    }

    handleSeatClick(seat);
  }

  function renderSeatInfoPanel() {
    if (!selectedSeatSeg) {
      return null;
    }

    if (!eventID) {
      return null;
    }

    return (
      <div className="form-card">
        <h3>Seat Section {selectedSeatSeg.segmentID}</h3>
        <p>Click seats inside the selected section to add/remove them.</p>
        <p>Selected seats: {selectedSeats.length}</p>
        <h3>
          Subtotal:{" "}
          {selectedSeatSeg.eventPrices[Number(eventID)] * selectedSeats.length}$
        </h3>

        <button type="button" onClick={handleSeatOrder}>
          Order Selected Seats
        </button>
      </div>
    );
  }

  function renderOrderSummary() {
    if (!selectedFieldSeg && selectedSeats.length === 0) {
      return null;
    }

    return (
      <div className="order-summary">
        <h3>Order Selected Tickets</h3>

        {selectedFieldSeg && (
          <p>
            Field section {selectedFieldSeg.segmentID}: {fieldTicketAmount}{" "}
            tickets
          </p>
        )}

        {selectedSeatSeg &&
          selectedSeats.map((seat) => (
            <p key={`${seat.row}-${seat.number}`}>
              Seat section {selectedSeatSeg.segmentID}: {seat.row}-{seat.number}
            </p>
          ))}
      </div>
    );
  }

  if (!venue) {
    return (
      <div>
        <p>Loading venue...</p>
        {error && <p className="form-error">{error}</p>}
      </div>
    );
  }
  if (!eventID) {
    return (
      <div>
        <p>Loading event...</p>
        {error && <p className="form-error">{error}</p>}
      </div>
    );
  }
  return (
    <div>
      <h2>Edit Order</h2>

      {error && <p className="form-error">{error}</p>}

      {timeLeft && (
        <div className="form-card">
          <h5>Time left:</h5>
          <p>{timeLeft}</p>
          <button type="button" onClick={cancelOrder}>Cancel Order</button>
        </div>
      )}

      <div className="create-order-layout">
        <VenueDisplay
          venue={venue}
          pendingRectangle={null}
          selectedFieldSegmentID={selectedFieldSeg?.segmentID}
          selectedSeatSegmentID={selectedSeatSeg?.segmentID}
          selectedSeats={selectedSeats}
          handleEmptyCellClick={handleEmptyAreaClick}
          handleStageClick={() => {}}
          handleEntranceClick={() => {}}
          handleSeatClick={handleVenueSeatClick}
          handleFieldSegmentClick={handleFieldSegmentSelected}
          handleSeatSegmentClick={handleSeatSegmentSelected}
          eventID={eventID}
        />

        <div className="create-order-side-panel">
          {selectedFieldSeg && renderFieldOrderPanel()}
          {selectedSeatSeg && renderSeatInfoPanel()}
        </div>
      </div>

      {renderOrderSummary()}
    </div>
  );
}
