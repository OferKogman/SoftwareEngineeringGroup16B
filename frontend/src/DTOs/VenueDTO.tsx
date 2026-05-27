export type VenueData = {
  name: string;
  location: string;
  grid: VenueGridData;
  fieldSeg: FieldSegData[];
  seatSeg: ChosenSeatingSegData[];
  stages: StageData[];
  entrances: EntranceData[];
};

export type VenueGridData = {
  rows: number;
  columns: number;
};

export type FieldSegData = {
  segmentID: string;
  area: GridRectangleData;
  size: number;
};

export type ChosenSeatingSegData = {
  segmentID: string;
  area: GridRectangleData;
  seats: SeatData[];
};

export type StageData = {
  stageID: string;
  area: GridRectangleData;
};

export type EntranceData = {
  entranceID: string;
  area: GridRectangleData;
};

export type GridRectangleData = {
  startRow: number;
  startColumn: number;
  rowCount: number;
  columnCount: number;
};

export type SeatData = {
  row: number;
  column: number;
};
