import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useSession } from "../../App";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/CreateOrder.css";
import PaymentForm from "./PaymentForm";
import type {
  ChosenSeatingSegData,
  FieldSegData,
  SeatData,
  VenueData,
} from "../../DTOs/VenueDTO";
import VenueDisplay from "../Shared/VenueDisplay";

const API_BASE = "http://localhost:8080";

type SegmentAvailability = {
  segmentID: string;
  taken: number;
  total: number;
};

type CreatedOrderResponse = {
  orderId?: string;
  totalOrderPrice?: number;
  tocalOrderPrice?: number;
  amount?: number;
};

type BackendVenueDTO = {
  name: string;
  location: unknown;
  grid: {
    rows: number;
    columns: number;
  };
  segments?: Record<string, any>;
  entrances?: Record<string, any>;
};

export default function CreateOrderPage() {
  const { eventID } = useParams();
  const navigate = useNavigate();
  const { sessionToken } = useSession();

  const [venueID, setVenueID] = useState("");
  const [venue, setVenue] = useState<VenueData | null>(null);
  const [availability, setAvailability] = useState<SegmentAvailability[]>([]);
  const [takenSeatsBySegment, setTakenSeatsBySegment] = useState<Record<string, SeatData[]>>({});

  const [selectedFieldSeg, setSelectedFieldSeg] = useState<FieldSegData | null>(null);
  const [selectedSeatSeg, setSelectedSeatSeg] = useState<ChosenSeatingSegData | null>(null);

  const [fieldTicketAmount, setFieldTicketAmount] = useState("1");
  const [selectedSeats, setSelectedSeats] = useState<SeatData[]>([]);
  const [error, setError] = useState("");

  function convertSeatsToArray(seats: unknown): SeatData[] {
    if (Array.isArray(seats)) {
      return seats as SeatData[];
    }

    if (seats && typeof seats === "object") {
      return Object.entries(seats as Record<string, any>).map(([seatID, seat]) => {
        if (typeof seat?.row === "number" && typeof seat?.column === "number") {
          return {
            row: seat.row,
            column: seat.column,
          };
        }

        const [row, column] = seatID.split("-").map(Number);

        return {
          row,
          column,
        };
      });
    }

    return [];
  }

  function getFallbackAreaFromSeats(seats: SeatData[]) {
    if (seats.length === 0) {
      return null;
    }

    const rows = seats.map((seat) => seat.row);
    const columns = seats.map((seat) => seat.column);

    const minRow = Math.min(...rows);
    const maxRow = Math.max(...rows);
    const minColumn = Math.min(...columns);
    const maxColumn = Math.max(...columns);

    return {
      startRow: minRow,
      startColumn: minColumn,
      rowCount: maxRow - minRow + 1,
      columnCount: maxColumn - minColumn + 1,
    };
  }

  function convertBackendVenueToVenueData(backendVenue: BackendVenueDTO): VenueData {
    const fieldSeg: FieldSegData[] = [];
    const seatSeg: ChosenSeatingSegData[] = [];

    Object.entries(backendVenue.segments ?? {}).forEach(([segmentID, segment]) => {
      const segmentType = segment.segmentType ?? segment.type;

      if (segmentType === "F") {
        const area =
          segment.area ??
          segment.gridRectangle ??
          segment.location ??
          segment.rectangle ??
          null;

        console.log("Segment:", segmentID, segment);
        console.log("Area candidates:", {
          area: segment.area,
          gridRectangle: segment.gridRectangle,
          location: segment.location,
          rectangle: segment.rectangle,
        });

        if (!area) {
          return;
        }

        fieldSeg.push({
          segmentID,
          area,
          size: segment.size ?? 0,
        });
      }

      if (segmentType === "S") {
        const seats = convertSeatsToArray(segment.seats);
        const area =
          segment.area ??
          segment.gridRectangle ??
          segment.location ??
          segment.rectangle ??
          getFallbackAreaFromSeats(seats);
        

        console.log("Segment:", segmentID, segment);
        console.log("Area candidates:", {
          area: segment.area,
          gridRectangle: segment.gridRectangle,
          location: segment.location,
          rectangle: segment.rectangle,
        });
        if (!area) {
          return;
        }

        seatSeg.push({
          segmentID,
          area,
          seats,
        });
      }
    });

    const entrances = Object.entries(backendVenue.entrances ?? {})
      .map(([entranceID, entrance]) => ({
        entranceID,
        area:
          entrance.area ??
          entrance.gridRectangle ??
          entrance.location ??
          entrance.rectangle,
      }))
      .filter((entrance) => Boolean(entrance.area));

    return {
      name: backendVenue.name,
      location:
        typeof backendVenue.location === "string"
          ? backendVenue.location
          : JSON.stringify(backendVenue.location),
      grid: backendVenue.grid,
      fieldSeg,
      seatSeg,
      stages: [],
      entrances,
    };
  }
  useEffect(() => {
    if (!eventID) {
      setError("Missing event ID.");
      return;
    }

    if (!sessionToken) {
      setError("Missing session token.");
      return;
    }

    let cancelled = false;

    async function loadVenue() {
      try {
        setError("");

        const eventResponse = await fetch(`${API_BASE}/events/${eventID}`, {
          method: "GET",
          headers: {
            Authorization: sessionToken,
            Accept: "application/json",
          },
        });

        const eventData = await eventResponse.json();

        if (!eventResponse.ok) {
          throw new Error(eventData?.message || eventData?.error || "Failed to load event.");
        }

        const event = eventData as EventDTO;
        const loadedVenueID = String((event as any).eventVenueID ?? (event as any).venueID ?? "");

        if (!loadedVenueID || loadedVenueID === "undefined") {
          throw new Error("Event loaded, but venue ID is missing.");
        }

        const venueResponse = await fetch(`${API_BASE}/venues/${loadedVenueID}/location`, {
          method: "GET",
          headers: {
            Authorization: sessionToken,
            Accept: "application/json",
          },
        });

        const backendVenue = await venueResponse.json();
        console.log("Loaded venue data:", backendVenue);
        if (!venueResponse.ok) {
          throw new Error(
            backendVenue?.message || backendVenue?.error || "Failed to load venue.",
          );
        }

        const realVenue = convertBackendVenueToVenueData(backendVenue);

        if (!cancelled) {
          setVenueID(loadedVenueID);
          setVenue(realVenue);

          setAvailability([
            ...realVenue.fieldSeg.map((segment) => ({
              segmentID: segment.segmentID,
              taken: 0,
              total: segment.size,
            })),
            ...realVenue.seatSeg.map((segment) => ({
              segmentID: segment.segmentID,
              taken: 0,
              total: segment.seats.length,
            })),
          ]);

          setTakenSeatsBySegment({});
        }
      } catch (err) {
        if (!cancelled) {
          setError(err instanceof Error ? err.message : "Failed to load venue.");
        }
      }
    }

    void loadVenue();

    return () => {
      cancelled = true;
    };
  }, [eventID, sessionToken]);

  function getAvailability(segmentID: string) {
    return availability.find((item) => item.segmentID === segmentID);
  }

  function getAvailableTickets(segmentID: string) {
    const data = getAvailability(segmentID);
    return data ? data.total - data.taken : 0;
  }

  function handleFieldSegmentSelected(segment: FieldSegData) {
    setError("");
    setSelectedSeatSeg(null);
    setSelectedSeats([]);
    setSelectedFieldSeg(segment);
    setFieldTicketAmount("1");
  }

  function handleSeatSegmentSelected(segment: ChosenSeatingSegData) {
    setError("");
    setSelectedFieldSeg(null);
    setSelectedSeats([]);
    setSelectedSeatSeg(segment);
  }

  function isTakenSeat(seat: SeatData) {
    if (!selectedSeatSeg) {
      return false;
    }

    const takenSeats = takenSeatsBySegment[selectedSeatSeg.segmentID] ?? [];

    return takenSeats.some(
      (takenSeat) => takenSeat.row === seat.row && takenSeat.column === seat.column,
    );
  }

  function isSelectedSeat(seat: SeatData) {
    return selectedSeats.some(
      (selectedSeat) => selectedSeat.row === seat.row && selectedSeat.column === seat.column,
    );
  }

  function handleSeatClick(seat: SeatData) {
    if (isTakenSeat(seat)) {
      return;
    }

    setSelectedSeats((current) => {
      const alreadySelected = current.some(
        (selectedSeat) => selectedSeat.row === seat.row && selectedSeat.column === seat.column,
      );

      if (alreadySelected) {
        return current.filter(
          (selectedSeat) =>
            !(selectedSeat.row === seat.row && selectedSeat.column === seat.column),
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
    const response = await fetch(`${API_BASE}/events/${eventID}/reservations/field`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: sessionToken,
      },
      body: JSON.stringify({
        venueId: venueID,
        segmentId: segmentID,
        amount,
      }),
    });

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
    seats: SeatData[],
  ) {
    const seatIDs = seats.map((seat) => `${seat.row}-${seat.column}`);

    const response = await fetch(`${API_BASE}/events/${eventID}/reservations/seats`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: sessionToken,
      },
      body: JSON.stringify({
        venueId: venueID,
        segmentId: segmentID,
        seatIds: seatIDs,
      }),
    });

    const text = await response.text();

        if (!response.ok) {
          throw new Error(text || "Failed to reserve seats.");
        }

        const orderId = text.replace("new OrderId:", "").trim();

        return {orderId,};
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
    const availableTickets = getAvailableTickets(selectedFieldSeg.segmentID);

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

    const availabilityData = getAvailability(selectedFieldSeg.segmentID);
    const taken = availabilityData?.taken ?? 0;
    const total = availabilityData?.total ?? selectedFieldSeg.size;
    const available = total - taken;

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

  function handleVenueSeatClick(
    seat: SeatData,
    segment: ChosenSeatingSegData,
  ) {
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
            Field section {selectedFieldSeg.segmentID}: {fieldTicketAmount} tickets
          </p>
        )}

        {selectedSeatSeg &&
          selectedSeats.map((seat) => (
            <p key={`${seat.row}-${seat.column}`}>
              Seat section {selectedSeatSeg.segmentID}: {seat.row}-{seat.column}
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