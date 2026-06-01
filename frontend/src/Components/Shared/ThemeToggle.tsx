import "./CSS/ThemeToggle.css";

type ThemeToggleProps = {
  theme: string;
  setTheme: React.Dispatch<React.SetStateAction<string>>;
};

function ThemeToggle({ theme, setTheme }: ThemeToggleProps) {
  const isLight = theme === "light";

  return (
    <button
      type="button"
      className={`theme-slider ${isLight ? "day" : "night"}`}
      onClick={() =>
        setTheme((current) => (current === "light" ? "dark" : "light"))
      }
    >
      <span className="theme-slider-text">
        {isLight ? "DAY MODE" : "NIGHT MODE"}
      </span>

      <span className="theme-slider-circle">{isLight ? "☀️" : "🌙"}</span>
    </button>
  );
}

export default ThemeToggle;
