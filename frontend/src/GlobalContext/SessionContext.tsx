import { createContext, useContext } from "react";

type SessionContextType = {
  sessionToken: string;
  setSessionToken: React.Dispatch<React.SetStateAction<string | null>>;
};

export const SessionContext = createContext<SessionContextType | null>(null);

export function useSession() {
  const context = useContext(SessionContext);

  if (!context) {
    throw new Error("useSession must be used inside SessionContext.Provider");
  }

  return context;
}
