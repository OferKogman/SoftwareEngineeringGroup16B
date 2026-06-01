import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import type { EventDTO } from "../DTOs/EventDTO";
import ViewDiscountPolicies from "./ViewDiscountPolicies";
import ViewPurchasePolicies from "./ViewPurchasePolicies";

export default function ViewEvent() {
  const { eventID } = useParams();
  const [error, setError] = useState<string>("");
  const [eventDTO, setEventDTO] = useState<EventDTO | null>(null);

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
        /*const fakeEventDTO: EventDTO = {
          eventID: 1,
          active: false,
          venueID: "pizdez",
          name: "DAMN ROCK",
          startTime: "2026-03-23T20:00",
          endTime: "2026-03-23T22:00",
          artist: "Satan",
          category: "NOT Rock",
          productionCompanyID: 0,
          discountPolicy: {
            type: "Composite",
            operator: "OR",

            leftPolicy: {
                type: "Composite",
                operator: "AND",

                leftPolicy: {
                    type: "Early Bird",
                    percentage: 20,
                    earlyBirdEndDate: "2026-06-01",
                },

                rightPolicy: {
                    type: "Minimum Purchase",
                    percentage: 5,
                    minimumAmount: 100,
                },
            },

            rightPolicy: {
                type: "Composite",
                operator: "AND",

                leftPolicy: {
                    type: "Coupon Code",
                    code: "VIP50",
                    percentage: 50,
                    expirationDate: "2026-06-15",
                },

                rightPolicy: {
                    type: "Last Minute",
                    percentage: 25,
                    lastMinuteStartDate: "2026-06-20",
                },
            },
        },
          purchasePolicy: {
            type: "Composite",
            operator: "AND",
            leftPolicy: {
                type: "Composite",
                operator: "OR",
                leftPolicy: {
                    type: "Minimum Age",
                    minAge: 18,
                },
                rightPolicy: {
                    type: "Maximum Tickets Per Customer",
                    maxTickets: 4,
                },
            },

            rightPolicy: {
                type: "Lottery",
                lotteryName: "VIP Giveaway",
                lotteryWinnerCount: 10,
                lotteryRegistrationDueDate: "2026-06-01",
            },
},
          price: 1000.0,
          rating: -5.0,
        };
        setEventDTO(fakeEventDTO);*/
        console.log("Loaded event from API:", event);
      } catch (err) {
        setError(err instanceof Error ? err.message : "Failed to load event.");
      }
    }

    void loadEvent();
  }, [eventID]);

  if (!eventDTO) {
    return <div>Loading...</div>;
  }

  return (
    <div>
      {error && <p className="form-error">{error}</p>}

      <h1>{eventDTO.name}</h1>

      <p>Venue ID: {eventDTO.venueID}</p>

      <p>Start Time: {eventDTO.startTime}</p>

      <p>End Time: {eventDTO.endTime}</p>

      <p>Artist: {eventDTO.artist}</p>

      <p>Category: {eventDTO.category}</p>

      <p>Production Company ID: {eventDTO.productionCompanyID}</p>

      <p>Price: {eventDTO.price}</p>

      <p>Rating: {eventDTO.rating}</p>

      <h3>Discount Policy:</h3>
      <ViewDiscountPolicies discountPolicy={eventDTO.discountPolicy} />

      <h3>Purchase Policy:</h3>
      <ViewPurchasePolicies purchasePolicy={eventDTO.purchasePolicy} />
    </div>
  );
}
