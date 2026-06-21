import { useEffect, useState, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type {
  ChosenSeatingSegData,
  EntranceData,
  FieldSegData,
  SeatData,
  StageData,
  VenueData,
  VenueGridData,
} from "../../DTOs/VenueDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import VenueDisplay from "../Shared/VenueDisplay";
import type { EventDTO } from "../../DTOs/EventDTO";

const API_BASE = "http://localhost:8080";


const initialGrid: VenueGridData = {
  rows: 10,
  columns: 10,
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
type SegmentAvailability = {
  segmentID: string;
  taken: number;
  total: number;
};

export default function ManageEventInventory() {
    const { eventID } = useParams();
    const { companyId } = useParams();
    const [venueID, setVenueID] = useState("");
    const [venue, setVenue] = useState<VenueData | null>(null);
    const [availability, setAvailability] = useState<SegmentAvailability[]>([]);
    const [takenSeatsBySegment, setTakenSeatsBySegment] = useState<Record<string, SeatData[]>>({});


    const { sessionToken } = useSession();
    const [formData, setFormData] = useState<VenueData>(initialVenue);
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

    function convertBackendVenueToVenueData(
          backendVenue: BackendVenueDTO,
        ): VenueData {
          const fieldSeg: FieldSegData[] = [];
          const seatSeg: ChosenSeatingSegData[] = [];
      
          Object.entries(backendVenue.segments ?? {}).forEach(
            ([segmentID, segment]) => {
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
            },
          );
      
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
      const response = await apiFetch(
        "http://localhost:8080/venues/configureNewLayoutAndInventory",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({
            companyID: Number(companyId),
            newVenueLayout: {
              ...formData,
              name: trimmedName,
              location: trimmedLocation,
            },
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
    
            const eventResponse = await apiFetch(`${API_BASE}/events/${eventID}`, {
              method: "GET",
            });
    
            const eventData = await eventResponse.json();
    
            if (!eventResponse.ok) {
              throw new Error(
                eventData?.message || eventData?.error || "Failed to load event.",
              );
            }
    
            const event = eventData as EventDTO;
            const loadedVenueID = String(
              (event as any).venueID,
            );
    
            if (!loadedVenueID || loadedVenueID === "undefined") {
              throw new Error("Event loaded, but venue ID is missing.");
            }
    
            const venueResponse = await apiFetch(
              `${API_BASE}/venues/${loadedVenueID}/location`,
              {
                method: "GET",
              },
            );
    
            const backendVenue = await venueResponse.json();
            console.log("Loaded venue data:", backendVenue);
            if (!venueResponse.ok) {
              throw new Error(
                backendVenue?.message ||
                  backendVenue?.error ||
                  "Failed to load venue.",
              );
            }
    
            const realVenue = convertBackendVenueToVenueData(backendVenue);
    
            if (!cancelled) {
                setVenueID(loadedVenueID);
                setVenue(realVenue);
                
                setFormData(realVenue);
                setVenueName(realVenue.name);
                setVenueLocation(realVenue.location);

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
              setError(
                err instanceof Error ? err.message : "Failed to load venue.",
              );
            }
          }
        }
    
        void loadVenue();
    
        return () => {
          cancelled = true;
        };
      }, [eventID, sessionToken]);
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
      <h2>Manage Venue</h2>
      {error && <p className="form-error">{error}</p>}
      {success && <p className="form-success">{success}</p>}

    <div
        style={{
        display: "flex",
        flexDirection: "column",
        gap: "8px",
        marginBottom: "12px",
        width: "260px",
        }}>
    <p>
        <strong>Venue Name:</strong> {formData.name}
    </p>
    <p>
        <strong>Venue Location:</strong> {formData.location}
    </p>
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
