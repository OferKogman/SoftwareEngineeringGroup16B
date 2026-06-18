import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";

type LotteryDTO = {
  lotteryID: number;
  eventID: number;
  lotteryName: string;
  winnerAmount: number;
  lotteryRegistrationDueDate: string;
};

type Props = {
  eventID: string;
};

export default function LotteryInformation({ eventID }: Props) {
  const [lottery, setLottery] = useState<LotteryDTO | null>(null);

  const apiFetch = useApiFetch();

  useEffect(() => {
    async function loadLottery() {
      try {
        const response = await apiFetch(
          `http://localhost:8080/events/${eventID}/lottery`,
          {
            method: "GET",
          },
        );

        if (!response.ok) {
          return;
        }

        const data: LotteryDTO = await response.json();
        setLottery(data);
      } catch {
        setLottery(null);
      }
    }

    void loadLottery();
  }, [eventID, apiFetch]);

  if (!lottery) {
    return null;
  }

  return (
    <>
      <h3>Lottery</h3>

      <div>
        <p>
          <strong>Name</strong> {lottery.lotteryName}
        </p>

        <p>
          <strong>Winner Amount</strong> {lottery.winnerAmount}
        </p>

        <p>
          <strong>Registration Due Date</strong>{" "}
          {lottery.lotteryRegistrationDueDate}
        </p>
      </div>
    </>
  );
}
