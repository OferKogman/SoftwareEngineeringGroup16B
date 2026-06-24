export type DiscountPolicyTypes =
  | "SIMPLE"
  | "AND"
  | "OR"
  | "SUM"
  | "MAX"
  | "MIN_DATE"
  | "MAX_DATE"
  | "MIN_TICKETS"
  | "MAX_TICKETS";

export interface IDiscountPolicyDTO {
  type: DiscountPolicyTypes;
}

export type SimpleDTO = IDiscountPolicyDTO & {
  type: "SIMPLE";
  percentage: number;
};

export type OrDTO = IDiscountPolicyDTO & {
  type: "OR";

  left: DiscountPolicyDTO;
  right: DiscountPolicyDTO;
  percentage: number;
};

export type AndDTO = IDiscountPolicyDTO & {
  type: "AND";

  left: DiscountPolicyDTO;
  right: DiscountPolicyDTO;
  percentage: number;
};

export type StartDateDTO = IDiscountPolicyDTO & {
  type: "MIN_DATE";
  startDate: string;
  percentage: number;
};

export type EndDateDTO = IDiscountPolicyDTO & {
  type: "MAX_DATE";
  endDate: string;
  percentage: number;
};

export type MinAmountDTO = IDiscountPolicyDTO & {
  type: "MIN_TICKETS";
  minAmount: number;
  percentage: number;
};

export type MaxAmountDTO = IDiscountPolicyDTO & {
  type: "MAX_TICKETS";
  maxAmount: number;
  percentage: number;
};

export type SumDTO = IDiscountPolicyDTO & {
  type: "SUM";
  left: DiscountPolicyDTO;
  right: DiscountPolicyDTO;
};

export type MaxDTO = IDiscountPolicyDTO & {
  type: "MAX";
  left: DiscountPolicyDTO;
  right: DiscountPolicyDTO;
};

export type DiscountPolicyDTO =
  | SimpleDTO
  | OrDTO
  | AndDTO
  | StartDateDTO
  | EndDateDTO
  | MinAmountDTO
  | MaxAmountDTO
  | SumDTO
  | MaxDTO;

export type SimpleDiscountPolicyTypes =
  | "MIN_DATE"
  | "MAX_DATE"
  | "MIN_TICKETS"
  | "MAX_TICKETS"
  | "SIMPLE";

export type CompositeDiscountPolicyTypes = "OR" | "AND" | "SUM" | "MAX";

export type NullableDiscountPolicyDTO = DiscountPolicyDTO | null;
