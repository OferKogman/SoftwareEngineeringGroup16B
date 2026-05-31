/* eslint-disable @typescript-eslint/no-unused-vars */
import { useState } from "react";
import type { EventDTO } from "../DTOs/EventDTO";
import ViewEventsList from "./ViewEventsList";

type SearchEventsFilters = {
  names?: string[];
  artists?: string[];
  categories?: string[];
  keywords?: string[];

  minPrice?: [number];
  maxPrice?: [number];

  startDate?: [string];
  endDate?: [string];

  minRating?: [number];

  productionCompanyID?: [number];

  locations?: string[];

  productionCompaniesRating?: [number];
};

export default function SearchEvents() {
  const [filters, setFilters] = useState<SearchEventsFilters>({
    names: [],
    artists: [],
    categories: [],
    keywords: [],
    locations: [],
  });

  const [events, setEvents] = useState<EventDTO[] | null>(null);
  const [error, setError] = useState("");

  function updateField<K extends keyof SearchEventsFilters>(
    field: K,
    value: SearchEventsFilters[K],
  ) {
    setFilters((prev) => ({
      ...prev,
      [field]: value,
    }));
  }

  async function searchEvents() {
    try {
      setError("");

      const filteredData = Object.fromEntries(
        Object.entries(filters).filter(([_, value]) => {
          if (value === undefined || value === null) return false;
          if (Array.isArray(value) && value.length === 0) return false;
          return true;
        }),
      );

      console.log(filteredData);

      /*
      const response = await fetch("/api/events/search", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(filteredData),
      });

      if (!response.ok) {
        throw new Error("Failed to search events.");
      }

      const data: EventDTO[] = await response.json();
      setEvents(data);
      */

      const fakeEvents: EventDTO[] = [
        {
          eventID: 1,
          active: true,
          venueID: "Live Park",
          name: "Queen Concert",
          startTime: "2027-06-22T14:30",
          endTime: "2027-06-22T18:30",
          artist: "Queen",
          category: "Rock",
          productionCompanyID: 0,
          discountPolicy: null,
          purchasePolicy: null,
          price: 500,
          rating: 5,
        },
        {
          eventID: 2,
          active: true,
          venueID: "Madison Square Garden",
          name: "ABBA Reunion",
          startTime: "2028-08-10T19:00",
          endTime: "2028-08-10T22:00",
          artist: "ABBA",
          category: "Pop",
          productionCompanyID: 1,
          discountPolicy: null,
          purchasePolicy: null,
          price: 300,
          rating: 4.5,
        },
      ];

      setEvents(fakeEvents);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to search events.");
    }
  }

  return (
    <div>
      <h1>Search Events</h1>

      {/* NAMES */}
      <input
        type="text"
        placeholder="Names (comma separated)"
        value={filters.names?.join(", ") ?? ""}
        onChange={(e) =>
          updateField(
            "names",
            e.target.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean),
          )
        }
      />

      {/* ARTISTS */}
      <input
        type="text"
        placeholder="Artists (comma separated)"
        value={filters.artists?.join(", ") ?? ""}
        onChange={(e) =>
          updateField(
            "artists",
            e.target.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean),
          )
        }
      />

      {/* CATEGORIES */}
      <input
        type="text"
        placeholder="Categories (comma separated)"
        value={filters.categories?.join(", ") ?? ""}
        onChange={(e) =>
          updateField(
            "categories",
            e.target.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean),
          )
        }
      />

      {/* KEYWORDS */}
      <input
        type="text"
        placeholder="Keywords (comma separated)"
        value={filters.keywords?.join(", ") ?? ""}
        onChange={(e) =>
          updateField(
            "keywords",
            e.target.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean),
          )
        }
      />

      {/* MIN PRICE */}
      <input
        type="number"
        placeholder="Minimum price"
        value={filters.minPrice?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "minPrice",
            e.target.value === "" ? undefined : [Number(e.target.value)],
          )
        }
      />

      {/* MAX PRICE */}
      <input
        type="number"
        placeholder="Maximum price"
        value={filters.maxPrice?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "maxPrice",
            e.target.value === "" ? undefined : [Number(e.target.value)],
          )
        }
      />

      {/* START DATE */}
      <input
        type="datetime-local"
        value={filters.startDate?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "startDate",
            e.target.value === "" ? undefined : [e.target.value],
          )
        }
      />

      {/* END DATE */}
      <input
        type="datetime-local"
        value={filters.endDate?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "endDate",
            e.target.value === "" ? undefined : [e.target.value],
          )
        }
      />

      {/* MIN RATING */}
      <input
        type="number"
        placeholder="Minimum rating"
        value={filters.minRating?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "minRating",
            e.target.value === "" ? undefined : [Number(e.target.value)],
          )
        }
      />

      {/* COMPANY ID */}
      <input
        type="number"
        placeholder="Production Company ID"
        value={filters.productionCompanyID?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "productionCompanyID",
            e.target.value === "" ? undefined : [Number(e.target.value)],
          )
        }
      />

      {/* LOCATIONS */}
      <input
        type="text"
        placeholder="Locations (comma separated)"
        value={filters.locations?.join(", ") ?? ""}
        onChange={(e) =>
          updateField(
            "locations",
            e.target.value
              .split(",")
              .map((s) => s.trim())
              .filter(Boolean),
          )
        }
      />

      {/* COMPANY RATING */}
      <input
        type="number"
        placeholder="Production company rating"
        value={filters.productionCompaniesRating?.[0] ?? ""}
        onChange={(e) =>
          updateField(
            "productionCompaniesRating",
            e.target.value === "" ? undefined : [Number(e.target.value)],
          )
        }
      />

      <button onClick={searchEvents}>Search</button>

      {error && <p className="form-error">{error}</p>}

      {/* null -> "not searched yet" */}
      {events === null ? (
        <ViewEventsList events={null} />
      ) : (
        <ViewEventsList events={events} />
      )}
    </div>
  );
}
