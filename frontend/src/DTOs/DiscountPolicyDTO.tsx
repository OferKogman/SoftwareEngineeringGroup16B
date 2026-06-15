export interface DefaultDiscountPolicyDTO {
  type: string;
}

export type CompositeDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Composite";
  leftPolicy?: DiscountPolicyDTO;
  rightPolicy?: DiscountPolicyDTO;
  operator: "AND" | "OR";
};

export type SimpleDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Regular";
  percentage: number;
};

export type EarlyBirdDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Early Bird";
  percentage: number;
  earlyBirdEndDate: string;
};

export type LastMinuteDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Last Minute";
  percentage: number;
  lastMinuteStartDate: string;
};

export type MinimumPurchaseDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Minimum Purchase";
  percentage: number;
  minimumAmount: number;
};

export type MaximumPurchaseDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Maximum Purchase";
  percentage: number;
  maximumAmount: number;
};

export type CouponCodeDiscountPolicyDTO = DefaultDiscountPolicyDTO & {
  type: "Coupon Code";
  percentage: number;
  code: string;
  expirationDate: string;
};

export type DiscountPolicyDTO =
  | CompositeDiscountPolicyDTO
  | SimpleDiscountPolicyDTO
  | EarlyBirdDiscountPolicyDTO
  | LastMinuteDiscountPolicyDTO
  | MinimumPurchaseDiscountPolicyDTO
  | MaximumPurchaseDiscountPolicyDTO
  | CouponCodeDiscountPolicyDTO
  | null;
