import type {
  ChosenSeatingSegData,
  FieldSegData,
  SeatData,
} from "../DTOs/VenueDTO";

type VenueDisplayProps = {
  handleFieldSegmentClick: (segment: FieldSegData) => void | Promise<void>;
  handleSeatSegmentClick: (
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  handleSeatClick: (
    seat: SeatData,
    segment: ChosenSeatingSegData,
    gridRow: number,
    gridColumn: number,
  ) => void | Promise<void>;
  onCancel?: () => void;
};

export default function VenueEditor({}) {}
