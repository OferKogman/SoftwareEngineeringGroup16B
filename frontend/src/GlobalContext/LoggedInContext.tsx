import { createContext, useContext } from "react";

type LoggedInContextType = {
  loggedIn: boolean;
  setLoggedIn: (val: boolean) => void;
};

export const LoggedInContext = createContext<LoggedInContextType | null>(null);

export function useLoggedIn() {
  const context = useContext(LoggedInContext);

  if (!context) {
    throw new Error("Is Logged in must be valid");
  }

  return context;
}
