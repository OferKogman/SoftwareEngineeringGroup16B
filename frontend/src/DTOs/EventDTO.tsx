import type { DiscountPolicyDTO } from "./DiscountPolicyDTO";
import type { PurchasePolicyDTO } from "./PurchasePolicyDTO";

export type EventDTO = {
  eventID: number;
  active: boolean;
  venueID: string;
  name: string;
  startTime: string;
  endTime: string;
  artist: string;
  category: string;
  productionCompanyID: number;
  discountPolicy: DiscountPolicyDTO | null;
  purchasePolicy: PurchasePolicyDTO | null;
  price: number;
  rating: number;
};
