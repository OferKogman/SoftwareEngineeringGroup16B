import { createContext, useContext } from "react";

type AdminLoggedInContextType = {
  adminLoggedIn: boolean;
  setAdminLoggedIn: (val: boolean) => void;
};

export const AdminLoggedInContext =
  createContext<AdminLoggedInContextType | null>(null);

export function useAdminLoggedIn() {
  const context = useContext(AdminLoggedInContext);

  if (!context) {
    throw new Error("Is Logged in must be valid");
  }

  return context;
}
