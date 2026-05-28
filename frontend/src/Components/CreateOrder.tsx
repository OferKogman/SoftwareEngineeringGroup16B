import { useEffect, useState } from "react";
import type {
  ChosenSeatingSegData,
  FieldSegData,
  SeatData,
  VenueData,
} from "../DTOs/VenueDTO";
import VenueDisplay from "./VenueDisplay";

type SegmentAvailability = {
  segmentID: string;
  taken: number;
  total: number;
};

export default function CreateOrderPage() {
  const eventId = "event-1";

  const [venue, setVenue] = useState<VenueData | null>(null);
  const [availability, setAvailability] = useState<SegmentAvailability[]>([]);
  const [takenSeatsBySegment, setTakenSeatsBySegment] = useState<
    Record<string, SeatData[]>
  >({});

  const [selectedFieldSeg, setSelectedFieldSeg] =
    useState<FieldSegData | null>(null);

  const [selectedSeatSeg, setSelectedSeatSeg] =
    useState<ChosenSeatingSegData | null>(null);

  const [fieldTicketAmount, setFieldTicketAmount] = useState("1");
  const [selectedSeats, setSelectedSeats] = useState<SeatData[]>([]);
  const [error, setError] = useState("");

  useEffect(() => {
    const fakeVenue: VenueData = {
      grid: {
        rows: 8,
        columns: 10,
      },
      fieldSeg: [
        {
          segmentID: "F1",
          size: 100,
          area: {
            startRow: 1,
            startColumn: 1,
            rowCount: 3,
            columnCount: 3,
          },
        },
      ],
      seatSeg: [
        {
          segmentID: "S1",
          area: {
            startRow: 1,
            startColumn: 5,
            rowCount: 4,
            columnCount: 4,
          },
          seats: [
            { row: 1, column: 1 },
            { row: 1, column: 2 },
            { row: 1, column: 3 },
            { row: 2, column: 1 },
            { row: 2, column: 2 },
            { row: 2, column: 3 },
          ],
        },
      ],
      stages: [],
      entrances: [],
    };

    setVenue(fakeVenue);

    setAvailability([
      { segmentID: "F1", taken: 20, total: 100 },
      { segmentID: "S1", taken: 2, total: 6 },
    ]);

    setTakenSeatsBySegment({
      S1: [
        { row: 1, column: 1 },
        { row: 2, column: 2 },
      ],
    });
  }, []);

  function getAvailability(segmentID: string) {
    return availability.find((item) => item.segmentID === segmentID);
  }

  function getAvailableTickets(segmentID: string) {
    const data = getAvailability(segmentID);
    return data ? data.total - data.taken : 0;
  }

  function handleFieldSegmentSelected(segment: FieldSegData) {
    console.log("field segment selected", segment);
    setError("");
    setSelectedSeatSeg(null);
    setSelectedSeats([]);
    setSelectedFieldSeg(segment);
    setFieldTicketAmount("1");
  }

  function handleSeatSegmentSelected(segment: ChosenSeatingSegData) {
    console.log("seat segment selected", segment);
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
      (takenSeat) =>
        takenSeat.row === seat.row && takenSeat.column === seat.column,
    );
  }

  function isSelectedSeat(seat: SeatData) {
    return selectedSeats.some(
      (selectedSeat) =>
        selectedSeat.row === seat.row && selectedSeat.column === seat.column,
    );
  }

  function handleSeatClick(seat: SeatData) {
    if (isTakenSeat(seat)) {
      return;
    }

    setSelectedSeats((current) => {
      const alreadySelected = current.some(
        (selectedSeat) =>
          selectedSeat.row === seat.row &&
          selectedSeat.column === seat.column,
      );

      if (alreadySelected) {
        return current.filter(
          (selectedSeat) =>
            !(
              selectedSeat.row === seat.row &&
              selectedSeat.column === seat.column
            ),
        );
      }

      return [...current, seat];
    });
  }

  async function handleFakeFieldOrder(
    eventId: string,
    segmentId: string,
    numOfTickets: number,
  ) {
    alert(
      `Field order created:
event=${eventId}
segment=${segmentId}
tickets=${numOfTickets}`,
    );
  }

  async function handleFakeSeatOrder(
    eventId: string,
    segmentId: string,
    seats: SeatData[],
  ) {
    alert(
      `Seat order created:
event=${eventId}
segment=${segmentId}
seats=${JSON.stringify(seats)}`,
    );
  }

  async function handleFieldOrder() {
    if (!selectedFieldSeg) {
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

    await handleFakeFieldOrder(eventId, selectedFieldSeg.segmentID, amount);
  }

  async function handleSeatOrder() {
    if (!selectedSeatSeg) {
      return;
    }

    if (selectedSeats.length === 0) {
      setError("Please choose at least one seat.");
      return;
    }

    await handleFakeSeatOrder(eventId, selectedSeatSeg.segmentID, selectedSeats);
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

  function renderSeatOrderPanel() {
    if (!selectedSeatSeg) {
      return null;
    }

    return (
      <div className="form-card">
        <h3>Seat Section {selectedSeatSeg.segmentID}</h3>

        <div
          style={{
            display: "grid",
            gridTemplateColumns: `repeat(${selectedSeatSeg.area.columnCount}, 40px)`,
            gap: "4px",
            width: "fit-content",
          }}
        >
          {selectedSeatSeg.seats.map((seat) => {
            const taken = isTakenSeat(seat);
            const selected = isSelectedSeat(seat);

            return (
              <button
                key={`${seat.row}-${seat.column}`}
                type="button"
                disabled={taken}
                onClick={() => handleSeatClick(seat)}
                style={{
                  width: "40px",
                  height: "40px",
                  backgroundColor: taken
                    ? "#9e9e9e"
                    : selected
                      ? "#d4af37"
                      : "#2196f3",
                  color: "white",
                  border: "1px solid #333",
                  cursor: taken ? "not-allowed" : "pointer",
                }}
              >
                {seat.row}-{seat.column}
              </button>
            );
          })}
        </div>

        <p>Selected seats: {selectedSeats.length}</p>

        <button type="button" onClick={handleSeatOrder}>
          Order Selected Seats
        </button>
      </div>
    );
  }

  if (!venue) {
    return <div>Loading venue...</div>;
  }

  return (
    <div>
      <h2>Create Order</h2>

      {error && <p className="form-error">{error}</p>}

     {!selectedFieldSeg && !selectedSeatSeg && (
  <VenueDisplay
  venue={venue}
  pendingRectangle={null}
  handleEmptyCellClick={() => {}}
  handleStageClick={() => {}}
  handleEntranceClick={() => {}}
  handleSeatClick={(seat, segment, gridRow, gridColumn) => {
    handleSeatSegmentSelected(segment);
  }}
  handleFieldSegmentClick={(segment, gridRow, gridColumn) => {
    handleFieldSegmentSelected(segment);
  }}
  handleSeatSegmentClick={(segment, gridRow, gridColumn) => {
    handleSeatSegmentSelected(segment);
  }}
/>
)}

{selectedFieldSeg && (
  <>
    <button
      type="button"
      onClick={() => {
        setSelectedFieldSeg(null);
        setError("");
      }}
    >
      Back to Venue
    </button>

    {renderFieldOrderPanel()}
  </>
)}

{selectedSeatSeg && (
  <>
    <button
      type="button"
      onClick={() => {
        setSelectedSeatSeg(null);
        setSelectedSeats([]);
        setError("");
      }}
    >
      Back to Venue
    </button>

    {renderSeatOrderPanel()}
  </>
)}
    </div>
  );
}