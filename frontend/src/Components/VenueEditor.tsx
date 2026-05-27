import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

const fieldAreaColor = "#13c3f6";
const fieldAreaBorder = "2px solid #0b6c97";
const seatAreaColor = "#abbe55";
const seatAreaBorder = "2px solid #70a026";
const hoverBorder = "2px solid #ff9800";
const emptyHoverColor = "#ffe0b2";
const fieldHoverColor = "#5fdcff";
const seatHoverColor = "#cde47a";
const singleSeatColor = "#f5f0c8";
const singleSeatHoverColor = "#fff2a8";
const singleSeatBorder = "2px solid #5f6f00";
const singleSeatHoverBorder = "2px solid #ff9800";

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
  const [hoveredCell, setHoveredCell] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [hoveredSeat, setHoveredSeat] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [selectedSeat, setSelectedSeat] = useState<{
    seat: SeatData;
    segment: ChosenSeatingSegData;
    gridRow: number;
    gridColumn: number;
  } | null>(null);
  const [selectedSeatSeg, setSelectedSeatSeg] = useState<{
    segment: ChosenSeatingSegData;
    gridRow: number;
    gridColumn: number;
  } | null>(null);

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

  function getSeat(row: number, column: number) {
    const seatSegment = getSeatSegment(row, column);

    if (!seatSegment) {
      return undefined;
    }

    const localRow = row - seatSegment.area.startRow + 1;
    const localColumn = column - seatSegment.area.startColumn + 1;

    return seatSegment.seats.find((seat) => {
      return seat.row === localRow && seat.column === localColumn;
    });
  }

  function getSeatLabel(seat: SeatData) {
    return `${seat.row}-${seat.column}`;
  }

  function handleEmptyCellClick(row: number, column: number) {
    console.log("empty cell clicked", { row, column });
  }

  function handleFieldSegmentClick(segment: FieldSegData) {
    console.log("field segment clicked", segment);
  }

  function handleSeatSegmentClick(
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    setSelectedSeatSeg({ segment, gridRow, gridColumn });
  }

  function handleSeatClick(
    seat: SeatData,
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    setSelectedSeat({ seat, segment, gridRow, gridColumn });
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

  function handleGridCellClick(row: number, column: number) {
    const fieldSegment = getFieldSegment(row, column);

    if (fieldSegment) {
      handleFieldSegmentClick(fieldSegment);
      return;
    }

    const seatSegment = getSeatSegment(row, column);
    const seat = getSeat(row, column);

    if (seat && seatSegment) {
      handleSeatClick(seat, seatSegment, row, column);
      return;
    }

    if (seatSegment) {
      handleSeatSegmentClick(seatSegment, row, column);
      return;
    }

    handleEmptyCellClick(row, column);
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

  function isInsideArea(row: number, column: number, area: GridRectangleData) {
    return (
      row >= area.startRow &&
      row < area.startRow + area.rowCount &&
      column >= area.startColumn &&
      column < area.startColumn + area.columnCount
    );
  }

  function isHovered(row: number, column: number) {
    if (!hoveredCell) {
      return false;
    }

    const hoveredFieldSegment = getFieldSegment(
      hoveredCell.row,
      hoveredCell.column,
    );

    if (hoveredFieldSegment) {
      return isInsideArea(row, column, hoveredFieldSegment.area);
    }

    const hoveredSeatSegment = getSeatSegment(
      hoveredCell.row,
      hoveredCell.column,
    );

    if (hoveredSeatSegment) {
      return isInsideArea(row, column, hoveredSeatSegment.area);
    }

    return row === hoveredCell.row && column === hoveredCell.column;
  }

  function isSeatHovered(row: number, column: number) {
    return hoveredSeat?.row === row && hoveredSeat.column === column;
  }

  function getBackgroundColor(
    fieldSegment: FieldSegData | undefined,
    seatSegment: ChosenSeatingSegData | undefined,
    seat: SeatData | undefined,
    hovered: boolean,
    seatHovered: boolean,
  ) {
    if (seat) {
      return seatHovered ? singleSeatHoverColor : singleSeatColor;
    }
    if (fieldSegment) {
      return hovered ? fieldHoverColor : fieldAreaColor;
    }

    if (seatSegment) {
      return hovered ? seatHoverColor : seatAreaColor;
    }

    return hovered ? emptyHoverColor : "transparent";
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

  function getBorders(row: number, column: number, hovered: boolean) {
    const fieldArea = getFieldSegmentArea(row, column);
    const seatArea = getSeatSegmentArea(row, column);

    if (fieldArea) {
      const border = hovered ? hoverBorder : fieldAreaBorder;

      return {
        borderTop: row === fieldArea.startRow ? border : "none",
        borderRight:
          column === fieldArea.startColumn + fieldArea.columnCount - 1
            ? border
            : "none",
        borderBottom:
          row === fieldArea.startRow + fieldArea.rowCount - 1 ? border : "none",
        borderLeft: column === fieldArea.startColumn ? border : "none",
      };
    }

    if (seatArea) {
      const border = hovered ? hoverBorder : seatAreaBorder;

      return {
        borderTop: row === seatArea.startRow ? border : "none",
        borderRight:
          column === seatArea.startColumn + seatArea.columnCount - 1
            ? border
            : "none",
        borderBottom:
          row === seatArea.startRow + seatArea.rowCount - 1 ? border : "none",
        borderLeft: column === seatArea.startColumn ? border : "none",
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
          position: "relative",
        }}
      >
        {Array.from({ length: formData.grid.rows }).flatMap((_, row) =>
          Array.from({ length: formData.grid.columns }).map((_, column) => {
            const fieldSegment = getFieldSegment(row, column);
            const seatSegment = getSeatSegment(row, column);
            const seat = getSeat(row, column);
            const hovered = isHovered(row, column);
            const seatHovered = isSeatHovered(row, column);

            return (
              <div
                key={`${row}-${column}`}
                onMouseEnter={() => setHoveredCell({ row, column })}
                onMouseLeave={() => setHoveredCell(null)}
                onClick={() => {
                  setSelectedSeat(null);
                  setSelectedSeatSeg(null);
                  handleGridCellClick(row, column);
                }}
                style={{
                  width: "40px",
                  height: "40px",
                  ...getBorders(row, column, hovered),
                  boxSizing: "border-box",
                  backgroundColor: seat
                    ? hovered
                      ? seatHoverColor
                      : seatAreaColor
                    : getBackgroundColor(
                        fieldSegment,
                        seatSegment,
                        seat,
                        hovered,
                        seatHovered,
                      ),
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  justifyContent: "center",
                  padding: seat ? "2px" : "0px",
                  fontSize: "12px",
                  fontWeight: "bold",
                  color: "#0b2f3f",
                  textAlign: "center",
                  overflow: "visible",
                  position: "relative",
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
                {seat && (
                  <div
                    onMouseEnter={() => setHoveredSeat({ row, column })}
                    onMouseLeave={() => setHoveredSeat(null)}
                    onClick={(event) => {
                      event.stopPropagation();
                      setSelectedSeatSeg(null);
                      if (seatSegment) {
                        handleSeatClick(seat, seatSegment, row, column);
                      }
                    }}
                    style={{
                      width: "100%",
                      height: "100%",
                      border: seatHovered
                        ? singleSeatHoverBorder
                        : singleSeatBorder,
                      backgroundColor: seatHovered
                        ? singleSeatHoverColor
                        : singleSeatColor,
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                      boxSizing: "border-box",
                      color: "#314000",
                      fontSize: "11px",
                      lineHeight: "11px",
                    }}
                  >
                    {getSeatLabel(seat)}
                  </div>
                )}
                {selectedSeat &&
                  selectedSeat.gridRow === row &&
                  selectedSeat.gridColumn === column && (
                    <div
                      style={{
                        position: "absolute",
                        top: "34px",
                        left: "20px",
                        zIndex: 10,
                        backgroundColor: "white",
                        border: "1px solid #555",
                        borderRadius: "4px",
                        padding: "6px",
                        boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
                        whiteSpace: "nowrap",
                      }}
                      onClick={(event) => event.stopPropagation()}
                    >
                      <button type="button" onClick={handleDeleteSeat}>
                        Delete seat
                      </button>
                    </div>
                  )}
                {selectedSeatSeg &&
                  !selectedSeat &&
                  selectedSeatSeg.gridRow === row &&
                  selectedSeatSeg.gridColumn === column && (
                    <div
                      style={{
                        position: "absolute",
                        top: "34px",
                        left: "20px",
                        zIndex: 10,
                        backgroundColor: "white",
                        border: "1px solid #555",
                        borderRadius: "4px",
                        padding: "6px",
                        boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
                        whiteSpace: "nowrap",
                      }}
                      onClick={(event) => event.stopPropagation()}
                    >
                      <button type="button" onClick={handleAddSeat}>
                        Add Seat
                      </button>
                    </div>
                  )}
              </div>
            );
          }),
        )}
      </div>
    </div>
  );
}
