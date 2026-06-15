import { useEffect, useState } from "react";
import { TbStar, TbStarFilled, TbStarHalfFilled } from "react-icons/tb";
import { useNavigate, useParams } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";
import { locationToString, type LocationDTO } from "../../DTOs/LocationDTO";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import { useSession } from "../../GlobalContext/SessionContext";
import ViewDiscountPolicies from "../ViewDiscountPolicies";
import ViewPurchasePolicies from "../ViewPurchasePolicies";
import "./CSS/ViewEvent.css";

export default function ViewEvent() {
  const { eventID } = useParams();
  const [error, setError] = useState<string>("");
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);
  const [location, setLocation] = useState<string>("");
  const [companyName, setCompanyName] = useState<string>("");
  const { sessionToken } = useSession();
  const navigate = useNavigate();

  useEffect(() => {
    if (!eventID) {
      return;
    }

    async function loadEvent() {
      try {
        const response = await fetch(`http://localhost:8080/events/${eventID}`);

        if (!response.ok) {
          throw new Error(await response.text());
        }
        const event: EventDTO = await response.json();

        setEventDTO(event);

        const locationResponse = await fetch(
          `http://localhost:8080/venues/${event.eventVenueID}/location`,
          {
            method: "Get",
            headers: {
              "Content-Type": "application/json",
              Authorization: sessionToken,
            },
          },
        );

        const loc: LocationDTO = await locationResponse.json();
        setLocation(locationToString(loc));

        const companyResponse = await fetch(
          `http://localhost:8080/production-companies/${event.eventProductionCompanyID}`,
        );
        const company: ProductionCompanyDTO = await companyResponse.json();
        setCompanyName(company.name);

        console.log("Loaded event from API:", event);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadEvent();
  });

  if (!eventDTO) {
    return <div>{error && <p className="form-error">{error}</p>}</div>;
  }

  return (
    <div className="event-view">
      {error && <p className="form-error">{error}</p>}

      <h1 className="event-title">{eventDTO.eventName}</h1>

      <div className="event-details">
        <p>
          <strong>Venue:</strong> {eventDTO.eventVenueID}
        </p>
        <p>
          <strong>Location:</strong> {location}
        </p>
        <p>
          <strong>Start Time:</strong> {eventDTO.eventStartTime}
        </p>
        <p>
          <strong>End Time:</strong> {eventDTO.eventEndTime}
        </p>
        <p>
          <strong>Artist:</strong> {eventDTO.eventArtist}
        </p>
        <p>
          <strong>Category:</strong> {eventDTO.eventCategory}
        </p>
        <p
          onClick={() =>
            navigate(`/companies/${eventDTO.eventProductionCompanyID}`)
          }
        >
          <strong>Production Company:</strong> {companyName}
        </p>
        <p>
          <strong>Price:</strong> {eventDTO.eventPrice}$
        </p>
        <p>
          <strong>Rating</strong>
          <span className="rating-stars">
            {renderRating(eventDTO.eventRating)}
            <span className="rating-text">
              {eventDTO.eventRating}
              {"/5"}
            </span>
          </span>
        </p>
      </div>

      <h3>Discount Policy</h3>
      <ViewDiscountPolicies discountPolicy={eventDTO.eventDiscountPolicy} />

      <h3>Purchase Policy</h3>
      <ViewPurchasePolicies purchasePolicy={eventDTO.eventPurchasePolicy} />
    </div>
  );
}

const renderRating = (rating: number) => {
  const stars = [];

  for (let i = 1; i <= 5; i++) {
    if (rating >= i) {
      stars.push(<TbStarFilled key={i} />);
    } else if (rating >= i - 0.5) {
      stars.push(<TbStarHalfFilled key={i} />);
    } else {
      stars.push(<TbStar key={i} />);
    }
  }

  return stars;
};
