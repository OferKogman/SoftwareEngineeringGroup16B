import { createContext, useContext } from "react";

export type SessionContextType = {
  sessionToken: string | null;
  setSessionToken: (token: string | null) => void;
  requestNewSessionToken: () => Promise<string>;
};

export const SessionContext = createContext<SessionContextType | null>(null);

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error("useSession must be used inside SessionContext.Provider");
  }

  return context;
}
