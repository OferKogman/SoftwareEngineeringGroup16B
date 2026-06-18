import { useEffect, useState } from "react";
import { useApiFetch } from "../../apiFetch";
import LotteryCreationForm from "./LotteryCreationForm";
import LotteryInformation from "./LotteryInformation";

export type LotteryDTO = {
  lotteryName: string;
  winnerAmount: number;
  lotteryRegistrationDueDate: string;
};

type Props = {
  eventID: string;
};

export default function EventLottery({ eventID }: Props) {
  const [lottery, setLottery] = useState<LotteryDTO | null>(null);
  const [loading, setLoading] = useState(true);

  const apiFetch = useApiFetch();

  async function getLottery() {
    try {
      const response = await apiFetch(
        `http://localhost:8080/events/${eventID}/lottery`,
        {
          method: "GET",
        },
      );

      if (!response.ok) {
        setLottery(null);
        return;
      }

      const data: LotteryDTO = await response.json();
      setLottery(data);
    } catch {
      setLottery(null);
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    getLottery();
  }, [eventID]);

  if (loading) {
    return <p>Loading lottery...</p>;
  }

  return (
    <main>
      <h2>Event Lottery</h2>

      {lottery ? (
        <LotteryInformation lottery={lottery} />
      ) : (
        <LotteryCreationForm />
      )}
    </main>
  );
}
