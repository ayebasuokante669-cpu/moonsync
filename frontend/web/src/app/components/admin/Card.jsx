export function Card({ children, className = "", padding = "md", hover = false }) {
  const paddingClasses = {
    none: "",
    sm: "p-4",
    md: "p-6",
    lg: "p-8",
  };

  return (
    <div
      className={`rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] ${paddingClasses[padding]} ${
        hover ? "transition-smooth hover:shadow-soft-lg" : ""
      } ${className}`}
    >
      {children}
    </div>
  );
}
