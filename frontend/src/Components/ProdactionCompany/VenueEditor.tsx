import { useState, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type {
  ChosenSeatingSegDTO,
  EntranceDTO,
  EventScheduleDTO,
  FieldSegDTO,
  SeatDTO,
  SegmentDTO,
  StageDTO,
  VenueGridDTO,
} from "../../DTOs/VenueDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import VenueDisplay from "../Shared/VenueDisplay";

const initialGrid: VenueGridDTO = {
  rows: 10,
  columns: 10,
};

type VenueCreateDTO = {
  name: string;
  location: string;
  segments: Record<string, SegmentDTO>;
  events: Record<number, EventScheduleDTO>;
  stages: Record<string, StageDTO>;
  entrances: Record<string, EntranceDTO>;
  grid: VenueGridDTO;
};

const initialVenue: VenueCreateDTO = {
  name: "",
  location: "",
  grid: initialGrid,
  segments: {},
  stages: {},
  events: {},
  entrances: {},
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
  segment: FieldSegDTO;
  gridRow: number;
  gridColumn: number;
};

type SelectedSeatSegData = {
  segment: ChosenSeatingSegDTO;
  gridRow: number;
  gridColumn: number;
};

type SelectedSeatData = {
  seat: SeatDTO;
  segment: ChosenSeatingSegDTO;
  gridRow: number;
  gridColumn: number;
};

type SelectedStageData = {
  stage: StageDTO;
  gridRow: number;
  gridColumn: number;
};

type SelectedEntranceData = {
  entrance: EntranceDTO;
  gridRow: number;
  gridColumn: number;
};

export default function VenueEditor() {
  const { companyId } = useParams();
  const { sessionToken } = useSession();
  const [formData, setFormData] = useState<VenueCreateDTO>(initialVenue);
  const [venueName, setVenueName] = useState<string>(initialVenue.name);
  const [venueLocation, setVenueLocation] = useState<string>(
    initialVenue.location,
  );
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");

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

  const apiFetch = useApiFetch();

  async function onSubmitVenue() {
    setError("");
    setSuccess("");

    if (!companyId) {
      setError("Missing company ID or event ID.");
      return;
    }

    const trimmedName = venueName.trim();
    const trimmedLocation = venueLocation.trim();

    if (!sessionToken) {
      setError("Missing session token.");
      return;
    }

    if (trimmedName === "") {
      setError("Venue name cannot be empty.");
      return;
    }

    if (trimmedLocation === "") {
      setError("Venue location cannot be empty.");
      return;
    }

    try {
      const segmentValues = Object.values(formData.segments);

      const newVenueLayout = {
        name: trimmedName,
        location: trimmedLocation,
        fieldSeg: segmentValues.filter((segment): segment is FieldSegDTO => {
          return "size" in segment;
        }),
        seatSeg: segmentValues
          .filter((segment): segment is ChosenSeatingSegDTO => {
            return "seats" in segment;
          })
          .map((segment) => ({
            segmentID: segment.segmentID,
            seats: Object.values(segment.seats),
            area: segment.area,
          })),
        stages: Object.values(formData.stages),
        entrances: Object.values(formData.entrances),
        grid: formData.grid,
        events: Object.values(formData.events),
      };

      const response = await apiFetch(
        "http://localhost:8080/venues/configureNewLayoutAndInventory",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            companyID: Number(companyId),
            newVenueLayout,
          }),
        },
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to configure venue layout.");
      }

      setSuccess("Venue created successfully.");
      setVenueName("");
      setVenueLocation("");
      setFormData(initialVenue);
      clearSelections();
    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Failed to configure venue layout.",
      );
    }
  }

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

    const newSegment: FieldSegDTO = {
      segmentID: getNextID(Object.keys(formData.segments), "F"),
      size: 0,
      area: {
        startRow: pendingRectangle.startRow,
        startColumn: pendingRectangle.startColumn,
        rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
        columnCount:
          pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
      },
      stocks: {},
    };

    const overlapsExisting = [
      ...Object.values(formData.segments).map((segment) => segment.area),
      ...Object.values(formData.stages).map((stage) => stage.area),
      ...Object.values(formData.entrances).map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newSegment.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      segments: {
        ...current.segments,
        [newSegment.segmentID]: newSegment,
      },
    }));

    setPendingRectangle(null);
  }

  function handleAddSeatSegment() {
    function createSeatsRecord(area: {
      startRow: number;
      startColumn: number;
      rowCount: number;
      columnCount: number;
    }) {
      const seats: Record<string, SeatDTO> = {};

      for (let row = 1; row <= area.rowCount; row++) {
        for (let column = 1; column <= area.columnCount; column++) {
          const seatId = `${row}-${column}`;

          seats[seatId] = {
            seatId: seatId,
            row: row,
            number: column,
            stock: {},
          };
        }
      }

      return seats;
    }

    if (!pendingRectangle) {
      return;
    }

    const newSegmentArea = {
      startRow: pendingRectangle.startRow,
      startColumn: pendingRectangle.startColumn,
      rowCount: pendingRectangle.endRow - pendingRectangle.startRow + 1,
      columnCount:
        pendingRectangle.endColumn - pendingRectangle.startColumn + 1,
    };

    const newSegment: ChosenSeatingSegDTO = {
      segmentID: getNextID(Object.keys(formData.segments), "S"),
      seats: createSeatsRecord(newSegmentArea),
      area: newSegmentArea,
    };

    const overlapsExisting = [
      ...Object.values(formData.segments).map((segment) => segment.area),
      ...Object.values(formData.stages).map((stage) => stage.area),
      ...Object.values(formData.entrances).map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newSegment.area));

    if (overlapsExisting) {
      setError("Segments cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      segments: {
        ...current.segments,
        [newSegment.segmentID]: newSegment,
      },
    }));

    setPendingRectangle(null);
  }

  function handleAddStage() {
    if (!pendingRectangle) {
      return;
    }

    const newStage = {
      stageID: getNextID(
        Object.values(formData.stages).map((stage) => stage.stageID),
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
      ...Object.values(formData.segments).map((segment) => segment.area),
      ...Object.values(formData.stages).map((stage) => stage.area),
      ...Object.values(formData.entrances).map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newStage.area));

    if (overlapsExisting) {
      setError("Stages cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      stages: {
        ...current.stages,
        [newStage.stageID]: newStage,
      },
    }));

    setPendingRectangle(null);
  }

  function handleAddEntrance() {
    if (!pendingRectangle) {
      return;
    }

    const newEntrance = {
      entranceID: getNextID(
        Object.values(formData.entrances).map(
          (entrance) => entrance.entranceID,
        ),
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
      ...Object.values(formData.segments).map((segment) => segment.area),
      ...Object.values(formData.stages).map((stage) => stage.area),
      ...Object.values(formData.entrances).map((entrance) => entrance.area),
    ].some((area) => rectanglesOverlap(area, newEntrance.area));

    if (overlapsExisting) {
      setError("Entrances cannot overlap.");
      return;
    }

    setFormData((current) => ({
      ...current,
      entrances: {
        ...current.entrances,
        [newEntrance.entranceID]: newEntrance,
      },
    }));

    setPendingRectangle(null);
  }
  function handleFieldSegmentClick(
    segment: FieldSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setFieldSizeInput(segment.size.toString());
    setSelectedFieldSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatSegmentClick(
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedSeatSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatClick(
    seat: SeatDTO,
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedSeat({ seat, segment, gridRow, gridColumn });
  }

  function handleStageClick(
    stage: StageDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    clearSelections();
    setSelectedStage({ stage, gridRow, gridColumn });
  }

  function handleEntranceClick(
    entrance: EntranceDTO,
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

    setFormData((current) => {
      const segmentID = selectedSeat.segment.segmentID;
      const currentSegment = current.segments[segmentID];

      if (!currentSegment || !("seats" in currentSegment)) {
        return current;
      }

      const updatedSeats = { ...currentSegment.seats };
      delete updatedSeats[selectedSeat.seat.seatId];

      return {
        ...current,
        segments: {
          ...current.segments,
          [segmentID]: {
            ...currentSegment,
            seats: updatedSeats,
          },
        },
      };
    });

    setSelectedSeat(null);
  }

  function handleDeleteSeatSegment() {
    if (!selectedSeatSeg) {
      return;
    }

    setFormData((current) => {
      const updatedSegments = { ...current.segments };
      delete updatedSegments[selectedSeatSeg.segment.segmentID];

      return {
        ...current,
        segments: updatedSegments,
      };
    });

    setSelectedSeatSeg(null);
  }

  function handleDeleteFieldSegment() {
    if (!selectedFieldSeg) {
      return;
    }

    setFormData((current) => {
      const updatedSegments = { ...current.segments };
      delete updatedSegments[selectedFieldSeg.segment.segmentID];

      return {
        ...current,
        segments: updatedSegments,
      };
    });

    setSelectedFieldSeg(null);
  }

  function handleDeleteStage() {
    if (!selectedStage) {
      return;
    }

    setFormData((current) => {
      const updatedStages = { ...current.stages };
      delete updatedStages[selectedStage.stage.stageID];

      return {
        ...current,
        stages: updatedStages,
      };
    });

    setSelectedStage(null);
  }

  function handleDeleteEntrance() {
    if (!selectedEntrance) {
      return;
    }

    setFormData((current) => {
      const updatedEntrances = { ...current.entrances };
      delete updatedEntrances[selectedEntrance.entrance.entranceID];

      return {
        ...current,
        entrances: updatedEntrances,
      };
    });

    setSelectedEntrance(null);
  }

  function handleAddSeat() {
    if (!selectedSeatSeg) {
      return;
    }

    const row =
      selectedSeatSeg.gridRow - selectedSeatSeg.segment.area.startRow + 1;
    const column =
      selectedSeatSeg.gridColumn - selectedSeatSeg.segment.area.startColumn + 1;

    const newSeat: SeatDTO = {
      seatId: `${row}-${column}`,
      row: row,
      number: column,
      stock: {},
    };

    setFormData((current) => {
      const segmentID = selectedSeatSeg.segment.segmentID;
      const currentSegment = current.segments[segmentID];

      if (!currentSegment || !("seats" in currentSegment)) {
        return current;
      }

      if (newSeat.seatId in currentSegment.seats) {
        return current;
      }

      return {
        ...current,
        segments: {
          ...current.segments,
          [segmentID]: {
            ...currentSegment,
            seats: {
              ...currentSegment.seats,
              [newSeat.seatId]: newSeat,
            },
          },
        },
      };
    });

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

    setFormData((current) => {
      const currentSegment = current.segments[segmentID];

      if (!currentSegment || !("size" in currentSegment)) {
        return current;
      }

      return {
        ...current,
        segments: {
          ...current.segments,
          [segmentID]: {
            ...currentSegment,
            size: newSize,
          },
        },
      };
    });

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
      {success && <p className="form-success">{success}</p>}

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          gap: "8px",
          marginBottom: "12px",
          width: "260px",
        }}
      >
        <label>
          Venue Name
          <input
            type="text"
            value={venueName}
            onChange={(event) => {
              setVenueName(event.currentTarget.value);
            }}
            style={{ width: "100%" }}
          />
        </label>

        <label>
          Venue Location
          <input
            type="text"
            value={venueLocation}
            onChange={(event) => {
              setVenueLocation(event.currentTarget.value);
            }}
            style={{ width: "100%" }}
          />
        </label>
      </div>

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
          venue={{
            ...formData,
            location: {
              name: "",
              houseNumber: "",
              street: "",
              city: "",
              state: "",
              country: "",
              latitude: null,
              longitude: null,
            },
          }}
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
          <button
            type="button"
            onClick={() => {
              void onSubmitVenue();
            }}
          >
            {"Save Changes"}
          </button>
        </div>
      }
    </div>
  );
}
