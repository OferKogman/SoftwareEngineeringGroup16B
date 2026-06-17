export type RoleType = "OWNER" | "MANAGER" | "MEMBER";

export type ManagerPermissions =
  | "EVENT_INVENTORY"
  | "VENUE_CONFIGURATION"
  | "PURCHASE_POLICY"
  | "CUSTOMER_SUPPORT"
  | "VIEW_PURCHASE_HISTORY"
  | "SALES_REPORT";

export type HierarchyNodeDTO = {
  userID: string;
  parentID: string | null;
  roleType: RoleType;
  permissions: ManagerPermissions[];
};
