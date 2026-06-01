/* eslint-disable @typescript-eslint/no-unused-vars */
import { useState } from "react";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/SearchEvents.css";
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

  productionCompany?: [string];

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

      const response = await fetch("http://localhost:8080/events/search", {
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
    } catch (err) {
      setError(err instanceof Error ? err.message : "Failed to search events.");
    }
  }

  return (
    <div>
      <h1>Search Events</h1>

      <div className="filters-container">
        <label className="field">
          <div>Names</div>
          <input
            type="text"
            value={filters.names?.join(", ") ?? ""}
            placeholder="Names (comma separated)"
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
        </label>

        <label className="field">
          <div>Artists</div>
          <input
            type="text"
            value={filters.artists?.join(", ") ?? ""}
            placeholder="Artists (comma separated)"
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
        </label>

        <label className="field">
          <div>Categories</div>
          <input
            type="text"
            value={filters.categories?.join(", ") ?? ""}
            placeholder="Categories (comma separated)"
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
        </label>

        <label className="field">
          <div>Keywords</div>
          <input
            type="text"
            value={filters.keywords?.join(", ") ?? ""}
            placeholder="Keywords (comma separated)"
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
        </label>

        <label className="field">
          <div>Min Price</div>
          <input
            type="number"
            value={filters.minPrice?.[0] ?? ""}
            placeholder="Minimum price"
            onChange={(e) =>
              updateField(
                "minPrice",
                e.target.value === "" ? undefined : [Number(e.target.value)],
              )
            }
          />
        </label>

        <label className="field">
          <div>Max Price</div>
          <input
            type="number"
            value={filters.maxPrice?.[0] ?? ""}
            placeholder="Maximum price"
            onChange={(e) =>
              updateField(
                "maxPrice",
                e.target.value === "" ? undefined : [Number(e.target.value)],
              )
            }
          />
        </label>

        <label className="field">
          <div>Start Date</div>
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
        </label>

        <label className="field">
          <div>End Date</div>
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
        </label>

        <label className="field">
          <div>Minimum Rating</div>
          <input
            type="number"
            value={filters.minRating?.[0] ?? ""}
            placeholder="Minimum rating"
            onChange={(e) =>
              updateField(
                "minRating",
                e.target.value === "" ? undefined : [Number(e.target.value)],
              )
            }
          />
        </label>

        <label className="field">
          <div>Production Company</div>
          <input
            type="text"
            value={filters.productionCompany?.[0] ?? ""}
            placeholder="Production Company"
            onChange={(e) =>
              updateField(
                "productionCompany",
                e.target.value === "" ? undefined : [e.target.value.trim()],
              )
            }
          />
        </label>

        <label className="field">
          <div>Locations</div>
          <input
            type="text"
            value={filters.locations?.join(", ") ?? ""}
            placeholder="Locations (comma separated)"
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
        </label>

        <label className="field">
          <div>Production Company Rating</div>
          <input
            type="number"
            value={filters.productionCompaniesRating?.[0] ?? ""}
            placeholder="Production Company Rating"
            onChange={(e) =>
              updateField(
                "productionCompaniesRating",
                e.target.value === "" ? undefined : [Number(e.target.value)],
              )
            }
          />
        </label>
      </div>

      <br />

      <button onClick={searchEvents}>Search</button>

      {error && <p className="form-error">{error}</p>}

      {events !== null && <ViewEventsList events={events} />}
    </div>
  );
}
