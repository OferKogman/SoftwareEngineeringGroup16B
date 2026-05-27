import type {
  ChosenSeatingSegData,
  FieldSegData,
  VenueData,
} from "../DTOs/VenueDTO";

type SegmentAvailability = {
  segmentID: string;
  taken: number;
  total: number;
};

type CreateOrderVenueDisplayProps = {
  venue: VenueData;
  availability: SegmentAvailability[];
  onFieldSegmentSelected: (segment: FieldSegData) => void;
  onSeatSegmentSelected: (segment: ChosenSeatingSegData) => void;
};

const cellSize = 40;

const availableColor = "#2196f3";
const soldOutColor = "#9e9e9e";
const hoverColor = "#64b5f6";
const borderColor = "2px solid #0d47a1";

export default function CreateOrderVenueDisplay({
  venue,
  availability,
  onFieldSegmentSelected,
  onSeatSegmentSelected,
}: CreateOrderVenueDisplayProps) {
  function getAvailability(segmentID: string) {
    return availability.find((item) => item.segmentID === segmentID);
  }

  function isSoldOut(segmentID: string) {
    const data = getAvailability(segmentID);

    if (!data) {
      return false;
    }

    return data.taken >= data.total;
  }

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

  function isCenterCell(
    area: {
      startRow: number;
      startColumn: number;
      rowCount: number;
      columnCount: number;
    },
    row: number,
    column: number,
  ) {
    const centerRow = area.startRow + Math.floor(area.rowCount / 2);
    const centerColumn = area.startColumn + Math.floor(area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function handleCellClick(row: number, column: number) {
    console.log("cell clicked", row, column);
    const fieldSegment = getFieldSegment(row, column);

    if (fieldSegment) {
        console.log("field found", fieldSegment);
      if (!isSoldOut(fieldSegment.segmentID)) {
        onFieldSegmentSelected(fieldSegment);
      }
      return;
    }

    const seatSegment = getSeatSegment(row, column);

    if (seatSegment) {
        console.log("seat segment found", seatSegment);
      if (!isSoldOut(seatSegment.segmentID)) {
        onSeatSegmentSelected(seatSegment);
      }
    }
  }

  function getCellColor(
    fieldSegment: FieldSegData | undefined,
    seatSegment: ChosenSeatingSegData | undefined,
  ) {
    const segmentID = fieldSegment?.segmentID ?? seatSegment?.segmentID;

    if (!segmentID) {
      return "transparent";
    }

    return isSoldOut(segmentID) ? soldOutColor : availableColor;
  }

  function getLabel(
    fieldSegment: FieldSegData | undefined,
    seatSegment: ChosenSeatingSegData | undefined,
    row: number,
    column: number,
  ) {
    if (fieldSegment && isCenterCell(fieldSegment.area, row, column)) {
      return fieldSegment.segmentID;
    }

    if (seatSegment && isCenterCell(seatSegment.area, row, column)) {
      return seatSegment.segmentID;
    }

    return "";
  }

  return (
    <div
      style={{
        width: "900px",
        height: "700px",
        overflow: "auto",
        border: "1px solid gray",
      }}
    >
      <div
        style={{
          display: "grid",
          gridTemplateColumns: `repeat(${venue.grid.columns}, ${cellSize}px)`,
          gridTemplateRows: `repeat(${venue.grid.rows}, ${cellSize}px)`,
          width: `${venue.grid.columns * cellSize}px`,
          height: `${venue.grid.rows * cellSize}px`,
        }}
      >
        {Array.from({ length: venue.grid.rows }).flatMap((_, row) =>
          Array.from({ length: venue.grid.columns }).map((_, column) => {
            const fieldSegment = getFieldSegment(row, column);
            const seatSegment = getSeatSegment(row, column);
            const clickable = fieldSegment || seatSegment;

            return (
              <div
                key={`${row}-${column}`}
                onClick={() => handleCellClick(row, column)}
                style={{
                  width: `${cellSize}px`,
                  height: `${cellSize}px`,
                  boxSizing: "border-box",
                  border: clickable ? borderColor : "1px solid #cccccc",
                  backgroundColor: getCellColor(fieldSegment, seatSegment),
                  cursor: clickable ? "pointer" : "default",
                  display: "flex",
                  alignItems: "center",
                  justifyContent: "center",
                  fontWeight: "bold",
                  color: "white",
                  fontSize: "12px",
                }}
                onMouseEnter={(event) => {
                  if (clickable && !isSoldOut(clickable.segmentID)) {
                    event.currentTarget.style.backgroundColor = hoverColor;
                  }
                }}
                onMouseLeave={(event) => {
                  event.currentTarget.style.backgroundColor = getCellColor(
                    fieldSegment,
                    seatSegment,
                  );
                }}
              >
                {getLabel(fieldSegment, seatSegment, row, column)}
              </div>
            );
          }),
        )}
      </div>
    </div>
  );
}