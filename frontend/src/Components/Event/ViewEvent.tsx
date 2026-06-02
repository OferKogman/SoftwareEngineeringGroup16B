import { useEffect, useState } from "react";
import { TbStar, TbStarFilled, TbStarHalfFilled } from "react-icons/tb";
import { useNavigate, useParams } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";
import type { VenueData } from "../../DTOs/VenueDTO";
import type { ProductionCompanyDTO } from "../ProdactionCompany/ProdctionCompanyForm";
import ViewDiscountPolicies from "../ViewDiscountPolicies";
import ViewPurchasePolicies from "../ViewPurchasePolicies";
import "./CSS/ViewEvent.css";

export default function ViewEvent() {
  const { eventID } = useParams();
  const [error, setError] = useState<string>("");
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);
  const [location, setLocation] = useState<string>("");
  const [companyName, setCompanyName] = useState<string>("");
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
          `http://localhost:8080/venues/${event.venueID}`,
        );

        const venue: VenueData = await locationResponse.json();
        setLocation(venue.location);

        const companyResponse = await fetch(
          `http://localhost:8080/production-companies/${event.productionCompanyID}`,
        );
        const company: ProductionCompanyDTO = await companyResponse.json();
        setCompanyName(company.name);

        console.log("Loaded event from API:", event);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadEvent();
  }, [eventID]);

  if (!eventDTO) {
    return <div>{error && <p className="form-error">{error}</p>}</div>;
  }

  return (
    <div className="event-view">
      {error && <p className="form-error">{error}</p>}

      <h1 className="event-title">{eventDTO.name}</h1>

      <div className="event-details">
        <p>
          <strong>Venue:</strong> {eventDTO.venueID}
        </p>
        <p>
          <strong>Location:</strong> {location}
        </p>
        <p>
          <strong>Start Time:</strong> {eventDTO.startTime}
        </p>
        <p>
          <strong>End Time:</strong> {eventDTO.endTime}
        </p>
        <p>
          <strong>Artist:</strong> {eventDTO.artist}
        </p>
        <p>
          <strong>Category:</strong> {eventDTO.category}
        </p>
        <p
          onClick={() => navigate(`/companies/${eventDTO.productionCompanyID}`)}
        >
          <strong>Production Company:</strong> {companyName}
        </p>
        <p>
          <strong>Price:</strong> {eventDTO.price}$
        </p>
        <p>
          <strong>Rating</strong>
          <span className="rating-stars">
            {renderRating(eventDTO.rating)}
            <span className="rating-text">
              {eventDTO.rating}
              {"/5"}
            </span>
          </span>
        </p>
      </div>

      <h3>Discount Policy</h3>
      <ViewDiscountPolicies discountPolicy={eventDTO.discountPolicy} />

      <h3>Purchase Policy</h3>
      <ViewPurchasePolicies purchasePolicy={eventDTO.purchasePolicy} />
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
