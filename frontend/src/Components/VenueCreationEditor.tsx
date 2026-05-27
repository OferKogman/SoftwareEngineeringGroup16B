export type VenueData = {
  name: string;
  location: string;
  fieldSeg: FieldSegData[];
  seatSeg: ChosenSeatingSegData[];
};

export type FieldSegData = {
  segmentID: string;
  size: number;
};

export type ChosenSeatingSegData = {
  segmentID: string;
  seats: SeatData[];
};

export type SeatData = {
  row: number;
  column: number;
};
