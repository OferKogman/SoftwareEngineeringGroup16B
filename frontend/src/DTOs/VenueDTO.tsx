import type { LocationDTO } from "./LocationDTO";

export type VenueDTO = {
  name: string;
  location: LocationDTO;
  segments: Record<string, SegmentDTO>;
  events: Record<number, EventScheduleDTO>;
  stages: Record<string, StageDTO>;
  entrances: Record<string, EntranceDTO>;
  grid: VenueGridDTO;
};

export type SegmentDTO = FieldSegDTO | ChosenSeatingSegDTO;

export type VenueGridDTO = {
  rows: number;
  columns: number;
};

export type FieldSegDTO = {
  segmentID: string;
  area: GridRectangleDTO;
  size: number;
  stocks: Record<number, number>;
};

export type ChosenSeatingSegDTO = {
  segmentID: string;
  seats: Record<string, SeatDTO>;
  area: GridRectangleDTO;
};

export type StageDTO = {
  stageID: string;
  area: GridRectangleDTO;
};

export type EntranceDTO = {
  entranceID: string;
  area: GridRectangleDTO;
};

export type GridRectangleDTO = {
  startRow: number;
  startColumn: number;
  rowCount: number;
  columnCount: number;
};

export type SeatDTO = {
  seatId: string;
  row: number;
  column: number;
  stock: Record<number, boolean>;
};

export type EventScheduleDTO = {
  startTime: string;
  endTime: string;
};
