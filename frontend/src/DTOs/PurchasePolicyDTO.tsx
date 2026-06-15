export interface DefaultPurchasePolicyDTO {
  type: string;
}

export type CompositePurchasePolicyDTO = DefaultPurchasePolicyDTO & {
  type: "Composite";

  leftPolicy?: PurchasePolicyDTO;
  rightPolicy?: PurchasePolicyDTO;

  operator: "AND" | "OR";
};

export type MinAgePurchasePolicyDTO = DefaultPurchasePolicyDTO & {
  type: "Minimum Age";
  minAge: number;
};

export type MaxAgePurchasePolicyDTO = DefaultPurchasePolicyDTO & {
  type: "Maximum Age";
  maxAge: number;
};

export type MinTicketsPerCustomerPurchasePolicyDTO =
  DefaultPurchasePolicyDTO & {
    type: "Minimum Tickets Per Customer";
    minTickets: number;
  };

export type MaxTicketsPerCustomerPurchasePolicyDTO =
  DefaultPurchasePolicyDTO & {
    type: "Maximum Tickets Per Customer";
    maxTickets: number;
  };

export type LotteryPurchasePolicyDTO = DefaultPurchasePolicyDTO & {
  type: "Lottery";
  lotteryName: string;
  lotteryWinnerCount: number;
  lotteryRegistrationDueDate: string;
};

export type PurchasePolicyDTO =
  | CompositePurchasePolicyDTO
  | MinAgePurchasePolicyDTO
  | MaxAgePurchasePolicyDTO
  | MinTicketsPerCustomerPurchasePolicyDTO
  | MaxTicketsPerCustomerPurchasePolicyDTO
  | LotteryPurchasePolicyDTO
  | null;
