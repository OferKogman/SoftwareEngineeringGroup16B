import { useState } from "react";
import type {
  ChosenSeatingSegData,
  FieldSegData,
  SeatData,
  VenueData,
} from "../DTOs/VenueDTO";

const fieldAreaColor = "#13c3f6";
const fieldAreaBorder = "2px solid #0b6c97";
const fieldHoverColor = "#5fdcff";

const seatSegAreaColor = "#abbe55";
const seatSegAreaBorder = "2px solid #70a026";
const seatSegHoverColor = "#cde47a";

const hoverBorder = "2px solid #ff9800";
const emptyHoverColor = "#ffe0b2";

const seatBorder = "2px solid #5f6f00";
const seatColor = "#f5f0c8";
const seatHoverColor = "#fff2a8";

type VenueDisplayProps = {
  handleEmptyCellClick: (
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleFieldSegmentClick: (
    segment: FieldSegData,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleSeatSegmentClick: (
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleSeatClick: (
    seat: SeatData,
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  venue: VenueData;
  onCancel?: () => void;
};

export default function VenueEditor({
  handleEmptyCellClick,
  handleFieldSegmentClick,
  handleSeatSegmentClick,
  handleSeatClick,
  venue,
}: VenueDisplayProps) {
  const [hoveredCell, setHoveredCell] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [hoveredFieldSeg, setHoveredFieldSeg] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [hoveredSeatSeg, setHoveredSeatSeg] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [hoveredSeat, setHoveredSeat] = useState<{
    row: number;
    column: number;
  } | null>(null);

  const [selectedCell, setSelectedCell] = useState<{
    gridRow: number;
    gridColumn: number;
  } | null>(null);
  const [selectedFieldSeg, setSelectedFieldSeg] = useState<{
    segment: FieldSegData;
    gridRow: number;
    gridColumn: number;
  } | null>(null);
  const [selectedSeatSeg, setSelectedSeatSeg] = useState<{
    segment: ChosenSeatingSegData;
    gridRow: number;
    gridColumn: number;
  } | null>(null);
  const [selectedSeat, setSelectedSeat] = useState<{
    seat: SeatData;
    segment: ChosenSeatingSegData;
    gridRow: number;
    gridColumn: number;
  } | null>(null);

  function getFieldSegment(row: number, column: number) {
    return venue.fieldSeg.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }
  function getSeatSegment(row: number, column: number) {
    return venue.seatSeg.find(({ area }) => {
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

  function getSeatLabel(seat: SeatData) {
    return `${seat.row}-${seat.column}`;
  }

  function getBackgroundColor(
    fieldSegment: FieldSegData | undefined,
    seatSegment: ChosenSeatingSegData | undefined,
    seat: SeatData | undefined,
    hovered: boolean,
  ) {
    if (seat) {
      return hovered ? seatHoverColor : seatColor;
    }
    if (fieldSegment) {
      return hovered ? fieldHoverColor : fieldAreaColor;
    }

    if (seatSegment) {
      return hovered ? seatSegHoverColor : seatSegAreaColor;
    }

    return hovered ? emptyHoverColor : "transparent";
  }
  function getBorders(row: number, column: number, hovered: boolean) {
    const fieldArea = getFieldSegment(row, column)?.area;
    const seatArea = getSeatSegment(row, column)?.area;

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
      const border = hovered ? hoverBorder : seatSegAreaBorder;

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

  function handleMouseEnter(row: number, column: number) {
    setHoveredFieldSeg(null);
    setHoveredSeatSeg(null);
    setHoveredFieldSeg(null);
    setHoveredCell({ row, column });
    const seatSeg = getSeatSegment(row, column);
    if (seatSeg) {
      setHoveredSeatSeg({ row, column });

      const seat = getSeat(row, column);
      if (seat) {
        setHoveredSeat({ row, column });
      }
      return;
    }

    const fieldSeg = getFieldSegment(row, column);
    if (fieldSeg) {
      setHoveredFieldSeg({ row, column });
      return;
    }
  }

  function isHovered(row: number, column: number) {
    return (
      (hoveredSeat?.row === row && hoveredSeat.column === column) ||
      (hoveredSeatSeg?.row === row && hoveredSeatSeg.column === column) ||
      (hoveredFieldSeg?.row === row && hoveredFieldSeg.column === column) ||
      (hoveredCell?.row === row && hoveredCell.column === column)
    );
  }

  function handleGridCellClick(row: number, column: number) {
    const fieldSegment = getFieldSegment(row, column);

    if (fieldSegment) {
      setSelectedFieldSeg({
        segment: fieldSegment,
        gridRow: row,
        gridColumn: column,
      });
      handleFieldSegmentClick(fieldSegment, row, column);
      return;
    }

    const seatSegment = getSeatSegment(row, column);
    const seat = getSeat(row, column);

    if (seat && seatSegment) {
      setSelectedSeat({
        segment: seatSegment,
        seat: seat,
        gridRow: row,
        gridColumn: column,
      });
      handleSeatClick(seat, seatSegment, row, column);
      return;
    }

    if (seatSegment) {
      setSelectedSeatSeg({
        segment: seatSegment,
        gridRow: row,
        gridColumn: column,
      });
      handleSeatSegmentClick(seatSegment, row, column);
      return;
    }

    handleEmptyCellClick(row, column);
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
          gridTemplateColumns: `repeat(${venue.grid.columns}, 40px)`,
          gridTemplateRows: `repeat(${venue.grid.rows}, 40px)`,
          width: `${venue.grid.columns * 40}px`,
          height: `${venue.grid.rows * 40}px`,
          position: "relative",
        }}
      >
        {Array.from({ length: venue.grid.rows }).flatMap((_, row) =>
          Array.from({ length: venue.grid.columns }).map((_, column) => {
            const fieldSegment = getFieldSegment(row, column);
            const seatSegment = getSeatSegment(row, column);
            const seat = getSeat(row, column);
            const hovered = isHovered(row, column);

            return (
              <div
                key={`${row}-${column}`}
                onMouseEnter={() => {
                  handleMouseEnter(row, column);
                }}
                onMouseLeave={() => {
                  setHoveredCell(null);
                  setHoveredFieldSeg(null);
                  setHoveredSeatSeg(null);
                  setHoveredFieldSeg(null);
                }}
                onClick={() => {
                  setSelectedCell(null);
                  setSelectedFieldSeg(null);
                  setSelectedSeatSeg(null);
                  setSelectedSeat(null);
                  handleGridCellClick(row, column);
                }}
                style={{
                  width: "40px",
                  height: "40px",
                  ...getBorders(row, column, hovered),
                  boxSizing: "border-box",
                  backgroundColor: getBackgroundColor(
                    fieldSegment,
                    seatSegment,
                    seat,
                    hovered,
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
                    style={{
                      width: "100%",
                      height: "100%",
                      border: hovered ? seatHoverColor : seatBorder,
                      backgroundColor: hovered ? seatHoverColor : seatColor,
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
              </div>
            );
          }),
        )}
      </div>
    </div>
  );
}
