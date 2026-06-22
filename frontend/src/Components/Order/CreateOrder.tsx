import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
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

const API_BASE = "http://localhost:8080";
/*
type SegmentAvailability = {
  segmentID: string;
  taken: number;
  total: number;
};
*/

type CreatedOrderResponse = {
  orderId?: string;
  totalOrderPrice?: number;
  tocalOrderPrice?: number;
  amount?: number;
};

export default function CreateOrderPage() {
  const { eventID } = useParams();
  const navigate = useNavigate();

  const [venueID, setVenueID] = useState("");
  const [venue, setVenue] = useState<VenueDTO | null>(null);

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

    async function loadVenue() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/events/${eventID}/reservations/status`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }

        const status = await response.json();
        if (status != -1) {
          navigate(`/events/${eventID}/queue`, {
            state: {
              initialStatus: status,
            },
          });
        }

        setError("");
        if (!eventID) {
          setError("Missing event ID.");
          return;
        }

        const eventResponse = await apiFetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
        });

        if (!eventResponse.ok) {
          throw new Error(await eventResponse.text());
        }

        const event: EventDTO = await eventResponse.json();
        const loadedVenueID = event.eventVenueID;

        const venueResponse = await apiFetch(
          `${API_BASE}/venues/${loadedVenueID}`,
          {
            method: "GET",
          },
        );

        if (!venueResponse.ok) {
          throw new Error(await venueResponse.text());
        }
        const venue: VenueDTO = await venueResponse.json();

        if (!cancelled) {
          console.log(venue);
          setVenueID(loadedVenueID);
          setVenue(venue);
        }
      } catch (err) {
        if (!cancelled) {
          setError(
            err instanceof Error ? err.message : "Failed to load venue.",
          );
        }
      }
    }

    void loadVenue();

    return () => {
      cancelled = true;
    };
  }, [eventID, navigate, apiFetch]);

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
    if (isTakenSeat(seat)) {
      return;
    }

    setSelectedSeats((current) => {
      const alreadySelected = current.some(
        (selectedSeat) =>
          selectedSeat.row === seat.row && selectedSeat.number === seat.number,
      );

      if (alreadySelected) {
        return current.filter(
          (selectedSeat) =>
            !(
              selectedSeat.row === seat.row &&
              selectedSeat.number === seat.number
            ),
        );
      }

      return [...current, seat];
    });
  }

  async function reserveFieldSeats(
    eventID: string,
    venueID: string,
    segmentID: string,
    amount: number,
  ) {
    const response = await apiFetch(
      `${API_BASE}/events/${eventID}/reservations/field`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          venueId: venueID,
          segmentId: segmentID,
          amount,
        }),
      },
    );

    const text = await response.text();

    if (!response.ok) {
      throw new Error(text || "Failed to reserve field.");
    }

    const orderId = text.replace("new OrderId:", "").trim();

    return {
      orderId,
    };
  }

  async function reserveSeats(
    eventID: string,
    venueID: string,
    segmentID: string,
    seats: SeatDTO[],
  ) {
    const seatIDs = seats.map((seat) => `${seat.row}-${seat.number}`);

    const response = await apiFetch(
      `${API_BASE}/events/${eventID}/reservations/seats`,
      {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          venueId: venueID,
          segmentId: segmentID,
          seatIds: seatIDs,
        }),
      },
    );

    const text = await response.text();

    if (!response.ok) {
      throw new Error(text || "Failed to reserve seats.");
    }

    const orderId = text.replace("new OrderId:", "").trim();

    return { orderId };
  }

  function moveToPayment(createdOrder: CreatedOrderResponse) {
    const orderId = createdOrder.orderId;
    const amount =
      createdOrder.totalOrderPrice ??
      createdOrder.tocalOrderPrice ??
      createdOrder.amount;

    if (!orderId) {
      setError("Order was created, but no order ID was returned.");
      return;
    }

    navigate("/payment", {
      state: {
        orderId: orderId,
        amount: amount,
      },
    });
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
      const createdOrder = await reserveFieldSeats(
        eventID,
        venueID,
        selectedFieldSeg.segmentID,
        amount,
      );

      moveToPayment(createdOrder);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to reserve field.");
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
      const createdOrder = await reserveSeats(
        eventID,
        venueID,
        selectedSeatSeg.segmentID,
        selectedSeats,
      );

      moveToPayment(createdOrder);
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

    return (
      <div className="form-card">
        <h3>Seat Section {selectedSeatSeg.segmentID}</h3>
        <p>Click seats inside the selected section to add/remove them.</p>
        <p>Selected seats: {selectedSeats.length}</p>

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

  return (
    <div>
      <h2>Create Order</h2>

      {error && <p className="form-error">{error}</p>}

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
