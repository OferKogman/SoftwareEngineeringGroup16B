import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import type { EventDTO } from "../../DTOs/EventDTO";
import "./CSS/EventSmallDisplay.css";

const FALLBACK_IMAGE_URL =
  "https://placehold.co/400x250/6d2aa7/f3e9ff?text=Event";

type PexelsSearchResponse = {
  photos?: {
    src?: {
      medium?: string;
      large?: string;
      landscape?: string;
    };
  }[];
};

type EventSmallDisplayProps = {
  event: EventDTO;
};

export default function EventSmallDisplay({ event }: EventSmallDisplayProps) {
  const navigate = useNavigate();
  const [imageUrl, setImageUrl] = useState<string>(FALLBACK_IMAGE_URL);

  useEffect(() => {
    async function loadEventImage() {
      const apiKey = import.meta.env.VITE_PEXELS_API_KEY;

      if (!apiKey) {
        setImageUrl(FALLBACK_IMAGE_URL);
        return;
      }

      try {
        const response = await fetch(
          `https://api.pexels.com/v1/search?query=${encodeURIComponent(
            event.eventName,
          )}&per_page=50`,
          {
            headers: {
              Authorization: apiKey,
            },
          },
        );

        if (!response.ok) {
          setImageUrl(FALLBACK_IMAGE_URL);
          return;
        }

        const data: PexelsSearchResponse = await response.json();

        const photos = data.photos ?? [];

        if (photos.length === 0) {
          setImageUrl(FALLBACK_IMAGE_URL);
          return;
        }

        const randomPhoto = photos[Math.floor(Math.random() * photos.length)];

        const randomImageUrl =
          randomPhoto?.src?.large ||
          randomPhoto?.src?.medium ||
          randomPhoto?.src?.landscape;

        setImageUrl(randomImageUrl || FALLBACK_IMAGE_URL);
      } catch {
        setImageUrl(FALLBACK_IMAGE_URL);
      }
    }

    void loadEventImage();
  }, [event.eventName]);

  return (
    <button
      type="button"
      className="event-small-display"
      onClick={() => navigate(`/events/${event.eventID}`)}
    >
      <img
        className="event-small-image"
        src={imageUrl}
        alt={event.eventName}
        onError={() => setImageUrl(FALLBACK_IMAGE_URL)}
        style={{
          width: "100%",
          height: "180px",
          objectFit: "fill",
        }}
      />

      <div className="event-small-name">{event.eventName}</div>
    </button>
  );
}
