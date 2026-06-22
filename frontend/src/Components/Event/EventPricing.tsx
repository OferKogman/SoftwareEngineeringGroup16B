import { useEffect, useState, type ReactNode } from "react";
import { useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { EventDTO } from "../../DTOs/EventDTO";
import { locationToString } from "../../DTOs/LocationDTO";
import type {
  ChosenSeatingSegDTO,
  FieldSegDTO,
  SeatDTO,
  VenueDTO,
} from "../../DTOs/VenueDTO";
import VenueDisplay from "../Shared/VenueDisplay";

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

export default function EventPricing() {
  const [selectedFieldSeg, setSelectedFieldSeg] =
    useState<SelectedFieldSegData | null>(null);
  const [selectedSeatSeg, setSelectedSeatSeg] =
    useState<SelectedSeatSegData | null>(null);
  const [prices, setPrices] = useState<Record<string, number>>({});
  const [venueID, setVenueID] = useState<string | null>(null);
  const [venue, setVenue] = useState<VenueDTO | null>(null);
  const [error, setError] = useState<string>("");
  const [success, setSuccess] = useState<string>("");
  const { eventID } = useParams();

  const apiFetch = useApiFetch();

  async function onSubmitPricing() {
    try {
      setSuccess("");
      const response = await apiFetch(
        `http://localhost:8080/events/${eventID}/prices`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify(prices),
        },
      );

      if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText || "Failed to update venue segments.");
      }

      setSuccess("Venue updated successfully.");
      setSelectedFieldSeg(null);
      setSelectedSeatSeg(null);
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Failed to update venue segments.",
      );
    }
  }

  function handleFieldSegmentClick(
    segment: FieldSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    setSelectedSeatSeg(null);
    setSelectedFieldSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatSegmentClick(
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    setSelectedFieldSeg(null);
    setSelectedSeatSeg({ segment, gridRow, gridColumn });
  }
  function handleSeatClick(
    seat: SeatDTO,
    segment: ChosenSeatingSegDTO,
    gridRow: number,
    gridColumn: number,
  ) {
    setError("");
    setSelectedFieldSeg(null);
    setSelectedSeatSeg({ segment, gridRow, gridColumn });
  }

  useEffect(() => {
    let cancelled = false;

    async function loadVenue() {
      try {
        setError("");
        if (!eventID) {
          setError("Missing event ID.");
          return;
        }

        const eventResponse = await apiFetch(
          `http://localhost:8080/events/${eventID}`,
          {
            method: "GET",
          },
        );

        if (!eventResponse.ok) {
          throw new Error(await eventResponse.text());
        }

        const event: EventDTO = await eventResponse.json();
        const venueID = event.eventVenueID;
        setVenueID(venueID);
        if (!venueID) {
          setError("Missing venue ID.");
          return;
        }

        const venueResponse = await apiFetch(
          `http://localhost:8080/venues/${venueID}`,
          {
            method: "GET",
          },
        );

        if (!venueResponse.ok) {
          throw new Error(await venueResponse.text());
        }
        const loadedVenue: VenueDTO = await venueResponse.json();
        const loadedPrices: Record<string, number> = {};

        Object.values(loadedVenue.segments).forEach((segment) => {
          const eventPrices = segment.eventPrices;

          if (
            eventPrices &&
            eventID &&
            eventPrices[Number(eventID)] !== undefined
          ) {
            loadedPrices[segment.segmentID] = eventPrices[Number(eventID)];
          } else {
            loadedPrices[segment.segmentID] = 0;
          }
        });

        if (!cancelled) {
          setVenue(loadedVenue);
          setPrices(loadedPrices);
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
  }, [venueID, apiFetch, eventID]);

  function renderPopup(): ReactNode {
    if (selectedFieldSeg || selectedSeatSeg) {
      const selectedSegmentID =
        selectedFieldSeg?.segment.segmentID ??
        selectedSeatSeg?.segment.segmentID;

      return (
        <div
          style={{
            position: "absolute",
            top: `${(selectedFieldSeg?.gridRow ?? selectedSeatSeg?.gridRow ?? 0) * 40 + 8}px`,
            left: `${(selectedFieldSeg?.gridColumn ?? selectedSeatSeg?.gridColumn ?? 0) * 40 + 8}px`,
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
            value={selectedSegmentID ? (prices[selectedSegmentID] ?? "") : ""}
            onChange={(event) => {
              if (!selectedSegmentID) {
                return;
              }

              const value = event.currentTarget.value;

              setPrices((previousPrices) => ({
                ...previousPrices,
                [selectedSegmentID]: value === "" ? 0 : Number(value),
              }));
            }}
            placeholder="Segment price"
            style={{ width: "120px" }}
          />
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
      <h2>Pricing Manager</h2>
      {error && <p className="form-error">{error}</p>}
      {success && <p className="form-success">{success}</p>}

      {venue && (
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
            <p>{venue.name}</p>
          </label>

          <label>
            Venue Location
            <p>{locationToString(venue.location)}</p>
          </label>
        </div>
      )}
      <div
        style={{
          position: "relative",
          width: "fit-content",
        }}
      >
        {venue && (
          <VenueDisplay
            handleEmptyCellClick={() => {}}
            handleFieldSegmentClick={handleFieldSegmentClick}
            handleSeatSegmentClick={handleSeatSegmentClick}
            handleSeatClick={handleSeatClick}
            handleStageClick={() => {}}
            handleEntranceClick={() => {}}
            venue={venue}
            segmentPrices={prices}
          ></VenueDisplay>
        )}
        {renderPopup()}
      </div>

      {
        <div className="form-actions">
          <button
            type="button"
            onClick={() => {
              void onSubmitPricing();
            }}
          >
            {"Save Changes"}
          </button>
        </div>
      }
    </div>
  );
}
