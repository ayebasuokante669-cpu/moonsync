export function LoadingSpinner({ size = "md" }) {
  const sizeClasses = {
    sm: "h-4 w-4 border-2",
    md: "h-8 w-8 border-3",
    lg: "h-12 w-12 border-4",
  };

  return (
    <div className="flex items-center justify-center">
      <div
        className={`animate-spin rounded-full border-[var(--color-primary)] border-t-transparent ${sizeClasses[size]}`}
      ></div>
    </div>
  );
}

export function LoadingPage() {
  return (
    <div className="flex h-full w-full items-center justify-center">
      <div className="text-center">
        <LoadingSpinner size="lg" />
        <p className="mt-4 text-sm text-[var(--color-muted-foreground)]">Loading...</p>
      </div>
    </div>
  );
}
