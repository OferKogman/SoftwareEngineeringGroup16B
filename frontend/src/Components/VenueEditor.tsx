import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type {
  ChosenSeatingSegData,
  FieldSegData,
  GridRectangleData,
  SeatData,
  VenueData,
  VenueGridData,
} from "../DTOs/VenueDTO";
import VenueDisplay from "./VenueDisplay";

type VenueEditorProps = {
  onSubmitVenue: (venue: VenueData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialGrid: VenueGridData = {
  rows: 100,
  columns: 100,
};

const initialVenue: VenueData = {
  name: "",
  location: "",
  grid: initialGrid,
  fieldSeg: [],
  seatSeg: [],
};

export default function VenueEditor({
  onSubmitVenue,
  onCancel,
}: VenueEditorProps) {
  const { venueID } = useParams();
  const [formData, setFormData] = useState<VenueData>(initialVenue);
  const [error, setError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  useEffect(() => {
    if (!venueID) {
      return;
    }
    async function loadVenue() {
      try {
        //const response = await fetch(`/api/venues/${venueID}`);

        //if (!response.ok) {
        //throw new Error("Failed to load event.");
        //}

        //const venue: VenueData = await response.json();

        const fieldRec: GridRectangleData = {
          startRow: 1,
          startColumn: 1,
          rowCount: 5,
          columnCount: 5,
        };

        const field: FieldSegData = {
          segmentID: "F1",
          size: 50,
          area: fieldRec,
        };

        const seat1: SeatData = {
          row: 1,
          column: 1,
        };

        const seat2: SeatData = {
          row: 1,
          column: 2,
        };

        const seat3: SeatData = {
          row: 2,
          column: 3,
        };

        const seatRec: GridRectangleData = {
          startRow: 1,
          startColumn: 6,
          rowCount: 5,
          columnCount: 5,
        };

        const seatSeg: ChosenSeatingSegData = {
          segmentID: "S1",
          seats: [seat1, seat2, seat3],
          area: seatRec,
        };

        const grid: VenueGridData = {
          rows: 20,
          columns: 20,
        };

        const venue: VenueData = {
          name: "",
          location: "",
          grid: grid,
          fieldSeg: [field],
          seatSeg: [seatSeg],
        };

        setFormData(venue);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadVenue();
  }, [venueID]);

  function updateField<K extends keyof VenueData>(
    field: K,
    value: VenueData[K],
  ) {
    setFormData((current) => ({
      ...current,
      [field]: value,
    }));
  }

  async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setIsSubmitting(true);
    setError("");

    try {
      await onSubmitVenue({
        ...formData,
      });
      setFormData(initialVenue);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to edit venue.");
    } finally {
      setIsSubmitting(false);
    }
  }

  function handleEmptyCellClick(gridRow: number, gridColumn: number) {
    return;
  }
  function handleFieldSegmentClick(
    segment: FieldSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    return;
  }
  function handleSeatSegmentClick(
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    return;
    //  <div
    //    style={{
    //      position: "absolute",
    //      top: "34px",
    //      left: "20px",
    //      zIndex: 10,
    //      backgroundColor: "white",
    //      border: "1px solid #555",
    //      borderRadius: "4px",
    //      padding: "6px",
    //      boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
    //      whiteSpace: "nowrap",
    //    }}
    //    onClick={(event) => event.stopPropagation()}
    //  >
    //    <button type="button" onClick={handleAddSeat}>
    //      Add Seat
    //    </button>
    //  </div>
    //);
  }
  function handleSeatClick(
    seat: SeatData,
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    return;
    //  <div
    //    style={{
    //      position: "absolute",
    //      top: "34px",
    //      left: "20px",
    //      zIndex: 10,
    //      backgroundColor: "white",
    //      border: "1px solid #555",
    //      borderRadius: "4px",
    //      padding: "6px",
    //      boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
    //      whiteSpace: "nowrap",
    //    }}
    //    onClick={(event) => event.stopPropagation()}
    //  >
    //    <button type="button" onClick={handleDeleteSeat}>
    //      Delete seat
    //    </button>
    //  </div>
    //);
  }

  function handleDeleteSeat() {
    if (!selectedSeat) {
      return;
    }

    setFormData((current) => ({
      ...current,
      seatSeg: current.seatSeg.map((segment) => {
        if (segment.segmentID !== selectedSeat.segment.segmentID) {
          return segment;
        }

        return {
          ...segment,
          seats: segment.seats.filter((seat) => {
            return !(
              seat.row === selectedSeat.seat.row &&
              seat.column === selectedSeat.seat.column
            );
          }),
        };
      }),
    }));

    setSelectedSeat(null);
  }
  function handleAddSeat() {
    if (!selectedSeatSeg) {
      return;
    }

    const newSeat: SeatData = {
      row: selectedSeatSeg.gridRow - selectedSeatSeg.segment.area.startRow + 1,
      column:
        selectedSeatSeg.gridColumn -
        selectedSeatSeg.segment.area.startColumn +
        1,
    };

    setFormData((current) => ({
      ...current,
      seatSeg: current.seatSeg.map((segment) => {
        if (segment.segmentID !== selectedSeatSeg.segment.segmentID) {
          return segment;
        }

        const seatExists = segment.seats.some((seat) => {
          return seat.row === newSeat.row && seat.column === newSeat.column;
        });

        if (seatExists) {
          return segment;
        }

        return {
          ...segment,
          seats: [...segment.seats, newSeat],
        };
      }),
    }));

    setSelectedSeatSeg(null);
  }

  return (
    <VenueDisplay
      handleEmptyCellClick={handleEmptyCellClick}
      handleFieldSegmentClick={handleFieldSegmentClick}
      handleSeatSegmentClick={handleSeatSegmentClick}
      handleSeatClick={handleSeatClick}
      venue={formData}
    ></VenueDisplay>
  );
}
