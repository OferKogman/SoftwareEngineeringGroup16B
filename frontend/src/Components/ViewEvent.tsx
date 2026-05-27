import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";
import ViewPurchasePolicies from "./ViewPurchasePolicies";

export default function ViewEvent() {
  const { id } = useParams();
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

  useEffect(() => {
    const fakeEventDTO: EventDTO = {
      eventID: 1,
      active: false,
      venueID: "pizdez",
      name: "DAMN ROCK",
      startTime: "2026-03-23T20:00",
      endTime: "2026-03-23T22:00",
      artist: "Satan",
      category: "NOT Rock",
      productionCompanyID: 0,
      discountPolicy: null,
      purchasePolicy: null,
      price: 1000.0,
      rating: -5.0,
    };
    setEventDTO(fakeEventDTO);

    // fetch(`/api/events/${id}`)
    //     .then(res => res.json())
    //     .then(data => setEventDTO(data));
  }, [id]);

  if (!eventDTO) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      <h1>{eventDTO.name}</h1>

      <p>Venue ID: {eventDTO.venueID}</p>

      <p>Start Time: {eventDTO.startTime}</p>

      <p>End Time: {eventDTO.endTime}</p>

      <p>Artist: {eventDTO.artist}</p>

      <p>Category: {eventDTO.category}</p>

      <p>Production Company ID: {eventDTO.productionCompanyID}</p>

      <p>Price: {eventDTO.price}</p>

      <p>Rating: {eventDTO.rating}</p>

      <ViewPurchasePolicies purchasePolicy={eventDTO.purchasePolicy} />
    </div>
  );
}
