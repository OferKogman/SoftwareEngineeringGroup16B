import type { LotteryDTO } from "../Components/Event/EventLottery";
import type { DiscountPolicyDTO } from "./DiscountPolicyDTO";
import type { NullablePurchasePolicyDTO } from "./PurchasePolicyDTO";

export type EventDTO = {
  eventID: number;
  eventStatus: boolean;
  eventVenueID: string;
  eventName: string;
  eventStartTime: string;
  eventEndTime: string;
  eventArtist: string;
  eventCategory: string;
  eventProductionCompanyID: number;
  eventDiscountPolicy: DiscountPolicyDTO;
  eventPurchasePolicy: NullablePurchasePolicyDTO;
  eventPrice: number;
  eventRating: number;

  lotteryDTO: LotteryDTO | null;
};
