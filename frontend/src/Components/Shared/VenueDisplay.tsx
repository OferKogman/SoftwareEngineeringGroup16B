import { useEffect, useState } from "react";
import type {
  ChosenSeatingSegDTO,
  EntranceDTO,
  FieldSegDTO,
  SeatDTO,
  StageDTO,
  VenueDTO,
} from "../../DTOs/VenueDTO";

const fieldAreaColor = "#13c3f6";
const fieldAreaBorder = "2px solid #0b6c97";
const fieldHoverColor = "#5fdcff";

const seatSegAreaColor = "#abbe55";
const seatSegAreaBorder = "2px solid #70a026";
const seatSegHoverColor = "#cde47a";

const hoverBorder = "2px solid #ff9800";
const selectedSegmentBorder = "4px solid #ffcc00";
const emptyHoverColor = "#ffe0b2";

const seatBorder = "2px solid #5f6f00";
const seatColor = "#f5f0c8";
const seatHoverColor = "#fff2a8";

const stageAreaColor = "#7c4dff";
const stageAreaBorder = "2px solid #512da8";

const entranceAreaColor = "#43a047";
const entranceAreaBorder = "2px solid #1b5e20";

type VenueDisplayProps = {
  handleEmptyCellClick: (
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleFieldSegmentClick: (
    segment: FieldSegDTO,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleSeatSegmentClick: (
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleSeatClick: (
    seat: SeatDTO,
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleStageClick: (
    stage: StageDTO,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleEntranceClick: (
    entrance: EntranceDTO,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  venue: VenueDTO;
  onCancel?: () => void;
  pendingRectangle?: {
    startRow: number;
    startColumn: number;
    endRow: number;
    endColumn: number;
  } | null;
  selectionStartCell?: {
    gridRow: number;
    gridColumn: number;
  } | null;

  selectedFieldSegmentID?: string;
  selectedSeatSegmentID?: string;
  selectedSeats?: SeatDTO[];
  segmentPrices?: Record<string, number>;
};

export default function VenueDisplay({
  handleEmptyCellClick,
  handleFieldSegmentClick,
  handleSeatSegmentClick,
  handleSeatClick,
  handleStageClick,
  handleEntranceClick,
  venue,
  pendingRectangle,
  selectionStartCell,
  selectedFieldSegmentID,
  selectedSeatSegmentID,
  selectedSeats = [],
  segmentPrices = {},
}: VenueDisplayProps) {
  const [hoveredCell, setHoveredCell] = useState<{
    row: number;
    column: number;
  } | null>(null);
  const [hoveredFieldSeg, setHoveredFieldSeg] = useState<{
    segment: FieldSegDTO;
    row: number;
    column: number;
  } | null>(null);
  const [hoveredSeatSeg, setHoveredSeatSeg] = useState<{
    segment: ChosenSeatingSegDTO;
    row: number;
    column: number;
  } | null>(null);

  const [hoveredStage, setHoveredStage] = useState<{
    stage: StageDTO;
    row: number;
    column: number;
  } | null>(null);

  const [hoveredEntrance, setHoveredEntrance] = useState<{
    entrance: EntranceDTO;
    row: number;
    column: number;
  } | null>(null);

  const [hoveredSeat, setHoveredSeat] = useState<{
    seat: SeatDTO;
    segment: ChosenSeatingSegDTO;
    row: number;
    column: number;
  } | null>(null);

  const [fieldSeg, setFieldSeg] = useState<FieldSegDTO[]>([]);
  const [seatSeg, setSeatSeg] = useState<ChosenSeatingSegDTO[]>([]);
  const [stages, setStages] = useState<StageDTO[]>([]);
  const [entrances, setEntrances] = useState<EntranceDTO[]>([]);

  useEffect(() => {
    async function loadSegs() {
      const segmentValues = Object.values(venue.segments);

      setFieldSeg(
        segmentValues.filter((segment): segment is FieldSegDTO => {
          return "size" in segment;
        }),
      );

      setSeatSeg(
        segmentValues.filter((segment): segment is ChosenSeatingSegDTO => {
          return "seats" in segment;
        }),
      );

      setStages(Object.values(venue.stages));
      setEntrances(Object.values(venue.entrances));
    }
    void loadSegs();
  }, [venue]);

  function getFieldSegment(row: number, column: number) {
    return fieldSeg.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }
  function getSeatSegment(row: number, column: number) {
    return seatSeg.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }

  function getStage(row: number, column: number) {
    return stages.find(({ area }) => {
      return (
        row >= area.startRow &&
        row < area.startRow + area.rowCount &&
        column >= area.startColumn &&
        column < area.startColumn + area.columnCount
      );
    });
  }

  function getEntrance(row: number, column: number) {
    return entrances.find(({ area }) => {
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

    return seatSegment.seats[`${localRow}-${localColumn}`];
  }

  function isFieldSegmentCenterCell(
    segment: FieldSegDTO,
    row: number,
    column: number,
  ) {
    const centerRow =
      segment.area.startRow + Math.floor(segment.area.rowCount / 2);
    const centerColumn =
      segment.area.startColumn + Math.floor(segment.area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function isStageCenterCell(stage: StageDTO, row: number, column: number) {
    const centerRow = stage.area.startRow + Math.floor(stage.area.rowCount / 2);
    const centerColumn =
      stage.area.startColumn + Math.floor(stage.area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function isEntranceCenterCell(
    entrance: EntranceDTO,
    row: number,
    column: number,
  ) {
    const centerRow =
      entrance.area.startRow + Math.floor(entrance.area.rowCount / 2);
    const centerColumn =
      entrance.area.startColumn + Math.floor(entrance.area.columnCount / 2);

    return row === centerRow && column === centerColumn;
  }

  function getSeatLabel(seat: SeatDTO) {
    return seat.seatId;
  }

  function getBackgroundColor(
    fieldSegment: FieldSegDTO | undefined,
    seatSegment: ChosenSeatingSegDTO | undefined,
    seat: SeatDTO | undefined,
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

    const stageArea = getStage(row, column)?.area;

    if (stageArea) {
      const border = hovered ? hoverBorder : stageAreaBorder;

      return {
        borderTop: row === stageArea.startRow ? border : "none",
        borderRight:
          column === stageArea.startColumn + stageArea.columnCount - 1
            ? border
            : "none",
        borderBottom:
          row === stageArea.startRow + stageArea.rowCount - 1 ? border : "none",
        borderLeft: column === stageArea.startColumn ? border : "none",
      };
    }

    const entranceArea = getEntrance(row, column)?.area;

    if (entranceArea) {
      const border = hovered ? hoverBorder : entranceAreaBorder;

      return {
        borderTop: row === entranceArea.startRow ? border : "none",
        borderRight:
          column === entranceArea.startColumn + entranceArea.columnCount - 1
            ? border
            : "none",
        borderBottom:
          row === entranceArea.startRow + entranceArea.rowCount - 1
            ? border
            : "none",
        borderLeft: column === entranceArea.startColumn ? border : "none",
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
    setHoveredSeat(null);
    setHoveredStage(null);
    setHoveredEntrance(null);
    setHoveredCell({ row, column });
    const seatSeg = getSeatSegment(row, column);
    if (seatSeg) {
      setHoveredSeatSeg({ segment: seatSeg, row: row, column: column });

      return;
    }

    const fieldSeg = getFieldSegment(row, column);
    if (fieldSeg) {
      setHoveredFieldSeg({ segment: fieldSeg, row: row, column: column });
      return;
    }

    const stage = getStage(row, column);
    if (stage) {
      setHoveredStage({ stage, row, column });
      return;
    }

    const entrance = getEntrance(row, column);
    if (entrance) {
      setHoveredEntrance({ entrance, row, column });
      return;
    }
  }

  function isInsideArea(
    area: {
      startRow: number;
      startColumn: number;
      rowCount: number;
      columnCount: number;
    },
    row: number,
    column: number,
  ) {
    return (
      row >= area.startRow &&
      row < area.startRow + area.rowCount &&
      column >= area.startColumn &&
      column < area.startColumn + area.columnCount
    );
  }

  function isSegmentHovered(row: number, column: number) {
    return (
      (hoveredSeatSeg &&
        isInsideArea(hoveredSeatSeg.segment.area, row, column)) ||
      (hoveredFieldSeg &&
        isInsideArea(hoveredFieldSeg.segment.area, row, column)) ||
      (hoveredStage && isInsideArea(hoveredStage.stage.area, row, column)) ||
      (hoveredEntrance &&
        isInsideArea(hoveredEntrance.entrance.area, row, column)) ||
      (hoveredCell?.row === row && hoveredCell.column === column)
    );
  }

  function isSeatHovered(row: number, column: number) {
    return hoveredSeat?.row === row && hoveredSeat.column === column;
  }
  function isSeatSelected(
    seat: SeatDTO | undefined,
    seatSegment: ChosenSeatingSegDTO | undefined,
  ) {
    if (!seat || !seatSegment) {
      return false;
    }

    if (seatSegment.segmentID !== selectedSeatSegmentID) {
      return false;
    }

    return selectedSeats.some(
      (selectedSeat) =>
        selectedSeat.row === seat.row && selectedSeat.number === seat.number,
    );
  }

  function handleGridCellClick(row: number, column: number) {
    const fieldSegment = getFieldSegment(row, column);

    if (fieldSegment) {
      handleFieldSegmentClick(fieldSegment, row, column);
      return;
    }

    const stage = getStage(row, column);

    if (stage) {
      handleStageClick(stage, row, column);
      return;
    }

    const entrance = getEntrance(row, column);

    if (entrance) {
      handleEntranceClick(entrance, row, column);
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

  return (
    <div
      style={{
        width: "900px",
        height: "700px",
        minWidth: "900px",
        minHeight: "700px",
        maxWidth: "900px",
        maxHeight: "700px",
        overflow: "auto",
        border: "1px solid gray",
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
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
            const stage = getStage(row, column);
            const entrance = getEntrance(row, column);
            const segmentHovered = isSegmentHovered(row, column);
            const seatHovered = isSeatHovered(row, column);
            const selectedSegmentArea =
              fieldSegment && fieldSegment.segmentID === selectedFieldSegmentID
                ? fieldSegment.area
                : seatSegment && seatSegment.segmentID === selectedSeatSegmentID
                  ? seatSegment.area
                  : undefined;

            const seatSelected = isSeatSelected(seat, seatSegment);
            const activeRectangle = pendingRectangle
              ? pendingRectangle
              : selectionStartCell && hoveredCell
                ? {
                    startRow: Math.min(
                      selectionStartCell.gridRow,
                      hoveredCell.row,
                    ),
                    startColumn: Math.min(
                      selectionStartCell.gridColumn,
                      hoveredCell.column,
                    ),
                    endRow: Math.max(
                      selectionStartCell.gridRow,
                      hoveredCell.row,
                    ),
                    endColumn: Math.max(
                      selectionStartCell.gridColumn,
                      hoveredCell.column,
                    ),
                  }
                : null;

            const rectangleHovered =
              activeRectangle &&
              row >= activeRectangle.startRow &&
              row <= activeRectangle.endRow &&
              column >= activeRectangle.startColumn &&
              column <= activeRectangle.endColumn;

            return (
              <div
                key={`${row}-${column}`}
                onMouseEnter={() => {
                  handleMouseEnter(row, column);
                }}
                onMouseLeave={() => {
                  setHoveredFieldSeg(null);
                  setHoveredSeatSeg(null);
                  setHoveredSeat(null);
                  setHoveredStage(null);
                  setHoveredEntrance(null);
                }}
                onClick={() => {
                  handleGridCellClick(row, column);
                }}
                style={{
                  width: "40px",
                  height: "40px",
                  ...getBorders(row, column, segmentHovered),
                  ...(selectedSegmentArea
                    ? {
                        borderTop:
                          row === selectedSegmentArea.startRow
                            ? selectedSegmentBorder
                            : undefined,
                        borderRight:
                          column ===
                          selectedSegmentArea.startColumn +
                            selectedSegmentArea.columnCount -
                            1
                            ? selectedSegmentBorder
                            : undefined,
                        borderBottom:
                          row ===
                          selectedSegmentArea.startRow +
                            selectedSegmentArea.rowCount -
                            1
                            ? selectedSegmentBorder
                            : undefined,
                        borderLeft:
                          column === selectedSegmentArea.startColumn
                            ? selectedSegmentBorder
                            : undefined,
                      }
                    : {}),
                  boxSizing: "border-box",
                  backgroundColor: rectangleHovered
                    ? "#88bbff"
                    : stage
                      ? stageAreaColor
                      : entrance
                        ? entranceAreaColor
                        : getBackgroundColor(
                            fieldSegment,
                            seatSegment,
                            undefined,
                            segmentHovered,
                          ),
                  boxShadow: undefined,
                  filter: undefined,
                  zIndex: selectedSegmentArea ? 2 : 1,
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
                      <div
                        style={{
                          color: "#0b6c97",
                          fontSize: "12px",
                          lineHeight: "12px",
                          margin: 0,
                        }}
                      >
                        ${segmentPrices[fieldSegment.segmentID] ?? 0}
                      </div>
                    </>
                  )}
                {seatSegment &&
                  !seat &&
                  row ===
                    seatSegment.area.startRow +
                      Math.floor(seatSegment.area.rowCount / 2) &&
                  column ===
                    seatSegment.area.startColumn +
                      Math.floor(seatSegment.area.columnCount / 2) && (
                    <>
                      <div
                        style={{
                          color: "#314000",
                          fontSize: "16px",
                          lineHeight: "16px",
                          margin: 0,
                        }}
                      >
                        {seatSegment.segmentID}
                      </div>
                      <div
                        style={{
                          color: "#314000",
                          fontSize: "12px",
                          lineHeight: "12px",
                          margin: 0,
                        }}
                      >
                        ${segmentPrices[seatSegment.segmentID] ?? 0}
                      </div>
                    </>
                  )}
                {stage && isStageCenterCell(stage, row, column) && (
                  <div
                    style={{
                      color: "#ffffff",
                      fontSize: "16px",
                      lineHeight: "16px",
                      margin: 0,
                    }}
                  >
                    {stage.stageID}
                  </div>
                )}

                {entrance && isEntranceCenterCell(entrance, row, column) && (
                  <div
                    style={{
                      color: "#ffffff",
                      fontSize: "16px",
                      lineHeight: "16px",
                      margin: 0,
                    }}
                  >
                    {entrance.entranceID}
                  </div>
                )}
                {seat && (
                  <div
                    onMouseEnter={() => {
                      if (seatSegment) {
                        setHoveredSeat({
                          seat: seat,
                          segment: seatSegment,
                          row: row,
                          column: column,
                        });
                      }
                    }}
                    onMouseLeave={() => {
                      setHoveredSeat(null);
                    }}
                    style={{
                      width: "100%",
                      height: "100%",
                      border: seatHovered ? hoverBorder : seatBorder,
                      backgroundColor: seatSelected
                        ? "#d4af37"
                        : seatHovered
                          ? seatHoverColor
                          : seatColor,
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
