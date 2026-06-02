export type RoleType = "OWNER" | "MANAGER" | "FOUNDER";

export type ManagerPermissions =
  | "EVENT_INVENTORY"
  | "VENUE_CONFIGURATION"
  | "PURCHASE_POLICY"
  | "CUSTOMER_SUPPORT"
  | "VIEW_PURCHASE_HISTORY"
  | "SALES_REPORT";

export type ProductionCompanyDTO = {
  id: number;
  name: string;
  rating: number;
  founderID: string;
  members: string[];
};

export type HierarchyNodeDTO = {
  userID: string;
  parentID: string;
  roleType: RoleType;
  permissions: ManagerPermissions[];
};
