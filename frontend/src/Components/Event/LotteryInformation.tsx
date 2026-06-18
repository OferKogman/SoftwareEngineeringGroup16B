import type { LotteryDTO } from "./EventLottery";

type Props = {
  lottery: LotteryDTO;
};

export default function LotteryInformation({ lottery }: Props) {
  return (
    <div>
      <p>
        <strong>Name:</strong> {lottery.lotteryName}
      </p>

      <p>
        <strong>Winner Amount:</strong> {lottery.winnerAmount}
      </p>

      <p>
        <strong>Registration Due:</strong> {lottery.lotteryRegistrationDueDate}
      </p>
    </div>
  );
}
