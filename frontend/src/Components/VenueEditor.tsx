import { useEffect, useState, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import type {
  ChosenSeatingSegData,
  EntranceData,
  FieldSegData,
  GridRectangleData,
  SeatData,
  StageData,
  VenueData,
  VenueGridData,
} from "../DTOs/VenueDTO";
import VenueDisplay from "./VenueDisplay";

type VenueEditorProps = {
  onSubmitVenue: (venue: VenueData) => void | Promise<void>;
  onCancel?: () => void;
};

const initialGrid: VenueGridData = {
  rows: 10,
  columns: 10,
};

const initialVenue: VenueData = {
  name: "",
  location: "",
  grid: initialGrid,
  fieldSeg: [],
  seatSeg: [],
  stages: [],
  entrances: [],
};

type SelectedCellData = {
  gridRow: number;
  gridColumn: number;
};

type PendingRectangleData = {
  startRow: number;
  startColumn: number;
  endRow: number;
  endColumn: number;
};

type SelectedFieldSegData = {
  segment: FieldSegData;
  gridRow: number;
  gridColumn: number;
};

type SelectedSeatSegData = {
  segment: ChosenSeatingSegData;
  gridRow: number;
  gridColumn: number;
};

type SelectedSeatData = {
  seat: SeatData;
  segment: ChosenSeatingSegData;
  gridRow: number;
  gridColumn: number;
};

type SelectedStageData = {
  stage: StageData;
  gridRow: number;
  gridColumn: number;
};

type SelectedEntranceData = {
  entrance: EntranceData;
  gridRow: number;
  gridColumn: number;
};

export default function VenueEditor({
  onSubmitVenue,
  onCancel,
}: VenueEditorProps) {
  const { venueID } = useParams();
  const [formData, setFormData] = useState<VenueData>(initialVenue);
  const [error, setError] = useState<string>("");

  const [selectedCell, setSelectedCell] = useState<SelectedCellData | null>(
    null,
  );
  const [selectedFieldSeg, setSelectedFieldSeg] =
    useState<SelectedFieldSegData | null>(null);
  const [selectedSeatSeg, setSelectedSeatSeg] =
    useState<SelectedSeatSegData | null>(null);
  const [selectedSeat, setSelectedSeat] = useState<SelectedSeatData | null>(
    null,
  );
  const [selectedStage, setSelectedStage] = useState<SelectedStageData | null>(
    null,
  );

  const [selectedEntrance, setSelectedEntrance] =
    useState<SelectedEntranceData | null>(null);
  const [fieldSizeInput, setFieldSizeInput] = useState<string>("");
  const [pendingRectangle, setPendingRectangle] =
    useState<PendingRectangleData | null>(null);

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
          stages: [],
          entrances: [],
        };

        setFormData(venue);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadVenue();
  }, [venueID]);

  function clearSelections() {
    setSelectedCell(null);
    setSelectedFieldSeg(null);
    setSelectedSeatSeg(null);
    setSelectedSeat(null);
    setPendingRectangle(null);
    setSelectedStage(null);
    setSelectedEntrance(null);
  }

  function handleEmptyCellClick(gridRow: number, gridColumn: number) {
    setError("");
    if (!selectedCell) {
      clearSelections();
      setSelectedCell({ gridRow, gridColumn });
      return;
    }

    const startRow = Math.min(selectedCell.gridRow, gridRow);
    const startColumn = Math.min(selectedCell.gridColumn, gridColumn);
    const endRow = Math.max(selectedCell.gridRow, gridRow);
    const endColumn = Math.max(selectedCell.gridColumn, gridColumn);

    setPendingRectangle({
      startRow,
      startColumn,
      endRow,
      endColumn,
    });

    setSelectedCell(null);
  }

  function rectanglesOverlap(
    first: {
      startRow: number;
      startColumn: number;
      rowCount: number;
      columnCount: number;
    },
    second: {
      startRow: number;
      startColumn: number;
      rowCount: number;
      columnCount: number;
    },
  ) {
    return !(
      first.startRow + first.rowCount - 1 < second.startRow ||
      second.startRow + second.rowCount - 1 < first.startRow ||
      first.startColumn + first.columnCount - 1 < second.startColumn ||
      second.startColumn + second.columnCount - 1 < first.startColumn
    );
  }

  function getNextID(existingIDs: string[], prefix: string) {
    let nextNumber = 1;

    while (existingIDs.includes(`${prefix}${nextNumber}`)) {
      nextNumber++;
    }

    return `${prefix}${nextNumber}`;
  }
  function handleAddFieldSegment() {
    if (!pendingRectangle) {
      return;
    }

    const newSegment: FieldSegData = {
      segmentID: getNextID(
        formData.fieldSeg.map((segment) => segment.segmentID),
        "F",
      ),
      size: 0,
      area: {
        startRow: pendingRectangle.startRow,
        startColumn: pendingRectangle.startColumn,
        rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
        columnCount:
          pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
      },
    };

    const overlapsExisting = [
      ...formData.fieldSeg.map((segment) => segment.area),
      ...formData.seatSeg.map((segment) => segment.area),
      ...formData.stages.map((stage) => stage.area),
      ...formData.entrances.map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newSegment.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      fieldSeg: [...current.fieldSeg, newSegment],
    }));

    setPendingRectangle(null);
  }

  function handleAddSeatSegment() {
    if (!pendingRectangle) {
      return;
    }

    const newSegment: ChosenSeatingSegData = {
      segmentID: getNextID(
        formData.seatSeg.map((segment) => segment.segmentID),
        "S",
      ),
      seats: [],
      area: {
        startRow: pendingRectangle.startRow,
        startColumn: pendingRectangle.startColumn,
        rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
        columnCount:
          pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
      },
    };

    const overlapsExisting = [
      ...formData.fieldSeg.map((segment) => segment.area),
      ...formData.seatSeg.map((segment) => segment.area),
      ...formData.stages.map((stage) => stage.area),
      ...formData.entrances.map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newSegment.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      seatSeg: [...current.seatSeg, newSegment],
    }));

    setPendingRectangle(null);
  }

  function handleAddStage() {
    if (!pendingRectangle) {
      return;
    }

    const newStage = {
      stageID: getNextID(
        formData.stages.map((stage) => stage.stageID),
        "ST",
      ),
      area: {
        startRow: pendingRectangle.startRow,
        startColumn: pendingRectangle.startColumn,
        rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
        columnCount:
          pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
      },
    };

    const overlapsExisting = [
      ...formData.fieldSeg.map((segment) => segment.area),
      ...formData.seatSeg.map((segment) => segment.area),
      ...formData.stages.map((stage) => stage.area),
      ...formData.entrances.map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newStage.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      stages: [...current.stages, newStage],
    }));

    setPendingRectangle(null);
  }

  function handleAddEntrance() {
    if (!pendingRectangle) {
      return;
    }

    const newEntrance = {
      entranceID: getNextID(
        formData.entrances.map((entrance) => entrance.entranceID),
        "EN",
      ),
      area: {
        startRow: pendingRectangle.startRow,
        startColumn: pendingRectangle.startColumn,
        rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
        columnCount:
          pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
      },
    };

    const overlapsExisting = [
      ...formData.fieldSeg.map((segment) => segment.area),
      ...formData.seatSeg.map((segment) => segment.area),
      ...formData.stages.map((stage) => stage.area),
      ...formData.entrances.map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newEntrance.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      entrances: [...current.entrances, newEntrance],
    }));

    setPendingRectangle(null);
  }
  function handleFieldSegmentClick(
    segment: FieldSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setFieldSizeInput(segment.size.toString());
    setSelectedFieldSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatSegmentClick(
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedSeatSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatClick(
    seat: SeatData,
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedSeat({ seat, segment, gridRow, gridColumn });
  }

  function handleStageClick(
    stage: StageData,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedStage({ stage, gridRow, gridColumn });
  }

  function handleEntranceClick(
    entrance: EntranceData,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedEntrance({ entrance, gridRow, gridColumn });
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

  function handleDeleteSeatSegment() {
    if (!selectedSeatSeg) {
      return;
    }

    setFormData((current) => ({
      ...current,
      seatSeg: current.seatSeg.filter((segment) => {
        return !(segment.segmentID === selectedSeatSeg.segment.segmentID);
      }),
    }));

    setSelectedSeatSeg(null);
  }

  function handleDeleteFieldSegment() {
    if (!selectedFieldSeg) {
      return;
    }

    setFormData((current) => ({
      ...current,
      fieldSeg: current.fieldSeg.filter((segment) => {
        return !(segment.segmentID === selectedFieldSeg.segment.segmentID);
      }),
    }));

    setSelectedFieldSeg(null);
  }

  function handleDeleteStage() {
    if (!selectedStage) {
      return;
    }

    setFormData((current) => ({
      ...current,
      stages: current.stages.filter((stage) => {
        return !(stage.stageID === selectedStage.stage.stageID);
      }),
    }));

    setSelectedStage(null);
  }

  function handleDeleteEntrance() {
    if (!selectedEntrance) {
      return;
    }

    setFormData((current) => ({
      ...current,
      entrances: current.entrances.filter((entrance) => {
        return !(entrance.entranceID === selectedEntrance.entrance.entranceID);
      }),
    }));

    setSelectedEntrance(null);
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

  function handleChangeSize() {
    if (!selectedFieldSeg) {
      return;
    }

    const trimmedValue = fieldSizeInput.trim();
    const newSize = Number(trimmedValue);

    if (trimmedValue === "" || !Number.isInteger(newSize) || newSize < 0) {
      setError("Invalid segment size.");
      return;
    }

    const segmentID = selectedFieldSeg.segment.segmentID;

    setFormData((current) => ({
      ...current,
      fieldSeg: current.fieldSeg.map((segment) => {
        if (segment.segmentID !== segmentID) {
          return segment;
        }

        return {
          ...segment,
          size: newSize,
        };
      }),
    }));

    setSelectedFieldSeg((current) => {
      if (!current || current.segment.segmentID !== segmentID) {
        return current;
      }

      return {
        ...current,
        segment: {
          ...current.segment,
          size: newSize,
        },
      };
    });
  }

  function handleAddRow() {
    setFormData((current) => ({
      ...current,
      grid: {
        ...current.grid,
        rows: current.grid.rows + 1,
      },
    }));
  }

  function handleAddColumn() {
    setFormData((current) => ({
      ...current,
      grid: {
        ...current.grid,
        columns: current.grid.columns + 1,
      },
    }));
  }

  function renderPopup(): ReactNode {
    if (pendingRectangle) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${pendingRectangle.startRow * 40 + 8}px`,
            left: `${pendingRectangle.startColumn * 40 + 8}px`,
            zIndex: 10,
            backgroundColor: "white",
            border: "1px solid #555",
            borderRadius: "4px",
            padding: "6px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
            whiteSpace: "nowrap",
            display: "flex",
            flexDirection: "column",
            gap: "4px",
          }}
          onClick={(event) => event.stopPropagation()}
        >
          <button type="button" onClick={handleAddFieldSegment}>
            Add Field Segment
          </button>

          <button type="button" onClick={handleAddSeatSegment}>
            Add Seating Segment
          </button>

          <button type="button" onClick={handleAddStage}>
            Add Stage
          </button>

          <button type="button" onClick={handleAddEntrance}>
            Add Entrance
          </button>
        </div>
      );
    }
    if (selectedStage) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${selectedStage.gridRow * 40 + 8}px`,
            left: `${selectedStage.gridColumn * 40 + 8}px`,
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
          <button type="button" onClick={handleDeleteStage}>
            Delete Stage
          </button>
        </div>
      );
    }

    if (selectedEntrance) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${selectedEntrance.gridRow * 40 + 8}px`,
            left: `${selectedEntrance.gridColumn * 40 + 8}px`,
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
          <button type="button" onClick={handleDeleteEntrance}>
            Delete Entrance
          </button>
        </div>
      );
    }
    if (selectedSeat) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${selectedSeat.gridRow * 40 + 8}px`,
            left: `${selectedSeat.gridColumn * 40 + 8}px`,
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
      );
    }

    if (selectedSeatSeg) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${selectedSeatSeg.gridRow * 40 + 8}px`,
            left: `${selectedSeatSeg.gridColumn * 40 + 8}px`,
            zIndex: 10,
            backgroundColor: "white",
            border: "1px solid #555",
            borderRadius: "4px",
            padding: "6px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
            whiteSpace: "nowrap",
            display: "flex",
            flexDirection: "column",
            gap: "4px",
          }}
          onClick={(event) => event.stopPropagation()}
        >
          <button type="button" onClick={handleAddSeat}>
            Add Seat
          </button>

          <button type="button" onClick={handleDeleteSeatSegment}>
            Delete Segment
          </button>
        </div>
      );
    }

    if (selectedFieldSeg) {
      return (
        <div
          style={{
            position: "absolute",
            top: `${selectedFieldSeg.gridRow * 40 + 8}px`,
            left: `${selectedFieldSeg.gridColumn * 40 + 8}px`,
            zIndex: 10,
            backgroundColor: "white",
            border: "1px solid #555",
            borderRadius: "4px",
            padding: "6px",
            boxShadow: "0 2px 8px rgba(0, 0, 0, 0.25)",
            whiteSpace: "nowrap",
            display: "flex",
            flexDirection: "column",
            gap: "4px",
          }}
          onClick={(event) => event.stopPropagation()}
        >
          <input
            type="number"
            min="0"
            value={fieldSizeInput}
            onChange={(event) => {
              setFieldSizeInput(event.currentTarget.value);
            }}
            placeholder="Segment size"
            style={{ width: "120px" }}
          />

          <button type="button" onClick={handleChangeSize}>
            Change Size
          </button>

          <button type="button" onClick={handleDeleteFieldSegment}>
            Delete Segment
          </button>
        </div>
      );
    }

    return null;
  }

  return (
    <div
      style={{
        position: "relative",
        width: "100%",
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
      }}
    >
      <h2>Venue Editor</h2>
      {error && <p className="form-error">{error}</p>}
      <div
        style={{
          position: "relative",
          width: "fit-content",
        }}
      >
        <VenueDisplay
          handleEmptyCellClick={handleEmptyCellClick}
          handleFieldSegmentClick={handleFieldSegmentClick}
          handleSeatSegmentClick={handleSeatSegmentClick}
          handleSeatClick={handleSeatClick}
          handleStageClick={handleStageClick}
          handleEntranceClick={handleEntranceClick}
          venue={formData}
          pendingRectangle={pendingRectangle}
        ></VenueDisplay>

        {renderPopup()}
      </div>

      <div
        style={{
          display: "flex",
          gap: "8px",
          marginTop: "12px",
        }}
      >
        <button type="button" onClick={handleAddRow}>
          Add Row
        </button>

        <button type="button" onClick={handleAddColumn}>
          Add Column
        </button>
      </div>

      {
        <div className="form-actions">
          {onCancel && (
            <button type="button" onClick={onCancel}>
              Cancel
            </button>
          )}
          <button
            type="button"
            onClick={() => {
              void onSubmitVenue(formData);
            }}
          >
            {"Save Changes"}
          </button>
        </div>
      }
    </div>
  );
}
