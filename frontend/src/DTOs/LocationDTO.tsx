export type LocationDTO = {
  name: string;
  houseNumber: string;
  street: string;
  city: string;
  state: string;
  country: string;
  latitude: number | null;
  longitude: number | null;
};

export function locationToString(location: LocationDTO): string {
  const parts = [
    location.name,
    [location.houseNumber, location.street].filter(Boolean).join(" "),
    location.city,
    location.state,
    location.country,
  ].filter((part) => part && part.trim() !== "");

  return parts.join(", ");
}
