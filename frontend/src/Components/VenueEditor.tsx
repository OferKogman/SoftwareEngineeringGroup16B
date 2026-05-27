import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const fieldAreaColor;
const fieldAreaBorder = "2px solid #0b6c97";
const seatAreaColor;
const seatAreaBorder = "#70a026";

export type VenueData = {
  name: string;
  location: string;
  grid: VenueGridData;
  fieldSeg: FieldSegData[];
  seatSeg: ChosenSeatingSegData[];
};

export type VenueGridData = {
  rows: number;
  columns: number;
};

export type FieldSegData = {
  segmentID: string;
  area: GridRectangleData;
  size: number;
};

export type ChosenSeatingSegData = {
  segmentID: string;
  area: GridRectangleData;
  seats: SeatData[];
};

export type GridRectangleData = {
  startRow: number;
  startColumn: number;
  rowCount: number;
  columnCount: number;
};

export type SeatData = {
  row: number;
  column: number;
};

type VenueEditorProps = {
  onSubmitVenue: (venue: VenueData) => void | Promise<void>;
  onCancel?: () => void;
  venue?: VenueData;
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
          rowCount: 6,
          columnCount: 6,
        };

        const field: FieldSegData = {
          segmentID: "F1",
          size: 50,
          area: fieldRec,
        };

        const seat1: SeatData = {
          row: 6,
          column: 6,
        };

        const seat2: SeatData = {
          row: 6,
          column: 7,
        };

        const seat3: SeatData = {
          row: 7,
          column: 8,
        };

        const seatRec: GridRectangleData = {
          startRow: 6,
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
      setError(err instanceof Error ? err.message : "Failed to create event.");
    } finally {
      setIsSubmitting(false);
    }
  }

  function getFieldSegment(row: number, column: number) {
    return formData.fieldSeg.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }

  function getSeatSegment(row: number, column: number) {
    return formData.seatSeg.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }

  function getFieldSegmentArea(row: number, column: number) {
    return getFieldSegment(row, column)?.area;
  }

  function getSeatSegmentArea(row: number, column: number) {
    return getSeatSegment(row, column)?.area;
  }

  function isFieldSegmentCenterCell(
    segment: FieldSegData,
    row: number,
    column: number,
  ) {
    const centerRow =
      segment.area.startRow + Math.floor(segment.area.rowCount / 2);
    const centerColumn =
      segment.area.startColumn + Math.floor(segment.area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function isSeatSegmentCenterCell(
    segment: ChosenSeatingSegData,
    row: number,
    column: number,
  ) {
    const centerRow =
      segment.area.startRow + Math.floor(segment.area.rowCount / 2);
    const centerColumn =
      segment.area.startColumn + Math.floor(segment.area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function getBorders(row: number, column: number) {
    const fieldArea = getFieldSegmentArea(row, column);
    const seatArea = getFieldSegmentArea(row, column);

    if (fieldArea) {
      return {
        borderTop: row === fieldArea.startRow ? fieldAreaBorder : "none",
        borderRight:
          column === fieldArea.startColumn + fieldArea.columnCount - 1
            ? "2px solid #0b6c97"
            : "none",
        borderBottom:
          row === fieldArea.startRow + fieldArea.rowCount - 1
            ? "2px solid #0b6c97"
            : "none",
        borderLeft:
          column === fieldArea.startColumn ? "2px solid #0b6c97" : "none",
      };
    }

    if (seatArea) {
      return {
        borderTop: row === seatArea.startRow ? "2px solid #0b6c97" : "none",
        borderRight:
          column === seatArea.startColumn + seatArea.columnCount - 1
            ? "2px solid #0b6c97"
            : "none",
        borderBottom:
          row === seatArea.startRow + seatArea.rowCount - 1
            ? "2px solid #0b6c97"
            : "none",
        borderLeft:
          column === seatArea.startColumn ? "2px solid #0b6c97" : "none",
      };
    }

    return {
      borderTop: "1px solid #bbbbbb",
      borderRight: "1px solid #bbbbbb",
      borderBottom: "1px solid #bbbbbb",
      borderLeft: "1px solid #bbbbbb",
    };
  }

  return (
    <div
      style={{
        width: "100%",
        height: "80vh",
        overflow: "auto",
        border: "1px solid gray",
        display: "flex",
        justifyContent: "center",
      }}
    >
      <div
        style={{
          display: "grid",
          gridTemplateColumns: `repeat(${formData.grid.columns}, 40px)`,
          gridTemplateRows: `repeat(${formData.grid.rows}, 40px)`,
          width: `${formData.grid.columns * 40}px`,
          height: `${formData.grid.rows * 40}px`,
        }}
      >
        {Array.from({ length: formData.grid.rows }).flatMap((_, row) =>
          Array.from({ length: formData.grid.columns }).map((_, column) => {
            const fieldSegment = getFieldSegment(row, column);

            return (
              <div
                key={`${row}-${column}`}
                style={{
                  width: "40px",
                  height: "40px",
                  ...getFieldSegmentBorders(row, column),
                  boxSizing: "border-box",
                  backgroundColor: fieldSegment ? "#13c3f6" : "transparent",
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                  fontSize: "12px",
                  fontWeight: "bold",
                  color: "#0b2f3f",
                  textAlign: "center",
                  overflow: "hidden",
                }}
              >
                {fieldSegment &&
                  isFieldSegmentCenterCell(fieldSegment, row, column) && (
                    <>
                      <div
                        style={{
                          color: "#0b6c97",
                          fontSize: "20px",
                          lineHeight: "18px",
                          margin: 0,
                        }}
                      >
                        {fieldSegment.segmentID}
                      </div>
                      <div
                        style={{
                          color: "#0b6c97",
                          fontSize: "12px",
                          lineHeight: "10px",
                          margin: 0,
                        }}
                      >
                        {fieldSegment.size}
                      </div>
                    </>
                  )}
              </div>
            );
          }),
        )}
      </div>
    </div>
  );
}
