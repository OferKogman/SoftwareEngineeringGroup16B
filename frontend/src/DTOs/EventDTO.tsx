import type { LotteryDTO } from "../Components/Event/EventLottery";
import type { NullableDiscountPolicyDTO } from "./DiscountPolicyDTO";
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
  eventDiscountPolicy: NullableDiscountPolicyDTO;
  eventPurchasePolicy: NullablePurchasePolicyDTO;
  eventPrice: number;
  eventRating: number;

  lotteryDTO: LotteryDTO | null;
};
