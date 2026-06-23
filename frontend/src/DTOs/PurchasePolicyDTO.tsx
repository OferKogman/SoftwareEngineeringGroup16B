export type PurchasePolicyTypes =
  | "AND"
  | "OR"
  | "MIN_AGE"
  | "MAX_AGE"
  | "MIN_TICKETS"
  | "MAX_TICKETS";

export interface IPurchasePolicyDTO {
  type: PurchasePolicyTypes;
}

export type OrDTO = IPurchasePolicyDTO & {
  type: "OR";

  left: PurchasePolicyDTO;
  right: PurchasePolicyDTO;
};

export type AndDTO = IPurchasePolicyDTO & {
  type: "AND";

  left: PurchasePolicyDTO;
  right: PurchasePolicyDTO;
};

export type MinAgeDTO = IPurchasePolicyDTO & {
  type: "MIN_AGE";
  minAge: number;
};

export type MaxAgeDTO = IPurchasePolicyDTO & {
  type: "MAX_AGE";
  maxAge: number;
};

export type MinTicketsDTO = IPurchasePolicyDTO & {
  type: "MIN_TICKETS";
  minTickets: number;
};

export type MaxTicketsDTO = IPurchasePolicyDTO & {
  type: "MAX_TICKETS";
  maxTickets: number;
};

export type PurchasePolicyDTO =
  | OrDTO
  | AndDTO
  | MinAgeDTO
  | MaxAgeDTO
  | MinTicketsDTO
  | MaxTicketsDTO;

export type SimplePolicyTypes =
  | "MIN_AGE"
  | "MAX_AGE"
  | "MIN_TICKETS"
  | "MAX_TICKETS";

export type NullablePurchasePolicyDTO = PurchasePolicyDTO | null;
