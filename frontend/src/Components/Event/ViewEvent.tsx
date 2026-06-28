import { useEffect, useState } from "react";
import { TbStar, TbStarFilled, TbStarHalfFilled } from "react-icons/tb";
import { useNavigate, useParams } from "react-router-dom";
import { useApiFetch } from "../../apiFetch";
import type { DiscountPolicyDTO } from "../../DTOs/DiscountPolicyDTO";
import type { EventDTO } from "../../DTOs/EventDTO";
import { locationToString } from "../../DTOs/LocationDTO";
import type { ProductionCompanyDTO } from "../../DTOs/ProductionCompanyDTO";
import type { PurchasePolicyDTO } from "../../DTOs/PurchasePolicyDTO";
import type { VenueDTO } from "../../DTOs/VenueDTO";
import ViewDiscountPolicies from "../Shared/ViewDiscountPolicies";
import ViewPurchasePolicies from "../Shared/ViewPurchasePolicies";
import "./CSS/ViewEvent.css";
import LotteryInformation from "./LotteryInformation";

export default function ViewEvent() {
  const { eventID } = useParams();
  const [error, setError] = useState<string>("");
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);
  const [location, setLocation] = useState<string>("");
  const [companyName, setCompanyName] = useState<string>("");

  const [companyPurchasePolicy, setCompanyPurchasePolicy] =
    useState<PurchasePolicyDTO | null>(null);

  const [companyDiscountPolicy, setCompanyDiscountPolicy] =
    useState<DiscountPolicyDTO | null>(null);

  const navigate = useNavigate();
  const apiFetch = useApiFetch();

  useEffect(() => {
    if (!eventID) {
      return;
    }

    async function loadEvent() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/events/${eventID}`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          throw new Error(await response.text());
        }
        const event: EventDTO = await response.json();

        setEventDTO(event);

        const locationResponse = await apiFetch(
          `http://localhost:8080/venues/${event.eventVenueID}`,
          {
            method: "Get",
          },
        );

        if (!locationResponse.ok) {
          throw new Error(await response.text());
        }

        const ven: VenueDTO = await locationResponse.json();
        setLocation(locationToString(ven.location));

        const companyResponse = await apiFetch(
          `http://localhost:8080/production-companies/${event.eventProductionCompanyID}`,
          {
            method: "GET",
          },
        );

        if (!companyResponse.ok) {
          throw new Error(await response.text());
        }

        const company: ProductionCompanyDTO = await companyResponse.json();
        setCompanyName(company.name);
        setCompanyDiscountPolicy(company.discountPolicy);
        setCompanyPurchasePolicy(company.purchasePolicy);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadEvent();
  }, [apiFetch, eventID]);

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

      <LotteryInformation lottery={eventDTO.lotteryDTO} />

      <h3>Discount Policies</h3>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: "20px",
        }}
      >
        <div>
          <h4>Company Policy</h4>
          <ViewDiscountPolicies discountPolicy={companyDiscountPolicy} />
        </div>

        <div>
          <h4>Event Policy</h4>
          <ViewDiscountPolicies discountPolicy={eventDTO.eventDiscountPolicy} />
        </div>
      </div>

      <h3>Purchase Policies</h3>
      <div
        style={{
          display: "grid",
          gridTemplateColumns: "1fr 1fr",
          gap: "20px",
        }}
      >
        <div>
          <h4>Company Policy</h4>
          <ViewPurchasePolicies purchasePolicy={companyPurchasePolicy} />
        </div>

        <div>
          <h4>Event Policy</h4>
          <ViewPurchasePolicies purchasePolicy={eventDTO.eventPurchasePolicy} />
        </div>
      </div>
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
