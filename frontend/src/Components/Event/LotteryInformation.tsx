import "./CSS/ViewEvent.css";
import type { LotteryDTO } from "./EventLottery";

type Props = {
  lottery: LotteryDTO | null;
};

export default function LotteryInformation({ lottery }: Props) {
  if (!lottery) {
    return null;
  }

  return (
    <>
      <div className="event-view">
        <h3 className="event-title">Lottery</h3>

        <div className="event-details">
          <p>
            <strong>Name:</strong>
            {lottery.lotteryName}
          </p>

          <p>
            <strong>Winner Amount:</strong>
            {lottery.winnerAmount}
          </p>

          <p>
            <strong>Registration Due Date:</strong>
            {lottery.lotteryRegistrationDueDate}
          </p>
        </div>
      </div>
    </>
  );
}
