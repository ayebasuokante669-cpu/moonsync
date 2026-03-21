export function StatusBadge({ status, size = "md" }) {
  const styles = {
    active: "bg-[var(--color-success-light)] text-[var(--color-success)]",
    inactive: "bg-[var(--color-muted)] text-[var(--color-muted-foreground)]",
    pending: "bg-[var(--color-warning-light)] text-[var(--color-warning)]",
    banned: "bg-[var(--color-error-light)] text-[var(--color-error)]",
    resolved: "bg-[var(--color-success-light)] text-[var(--color-success)]",
    flagged: "bg-[var(--color-error-light)] text-[var(--color-error)]",
    draft: "bg-[var(--color-muted)] text-[var(--color-muted-foreground)]",
    published: "bg-[var(--color-primary-light)] text-[var(--color-primary)]",
    approved: "bg-[var(--color-success-light)] text-[var(--color-success)]",
    rejected: "bg-[var(--color-error-light)] text-[var(--color-error)]",
    "under review": "bg-[var(--color-info-light)] text-[var(--color-info)]",
  };

  const sizeStyles = {
    sm: "px-2 py-0.5 text-xs",
    md: "px-2.5 py-1 text-sm",
  };

  const statusStyle = styles[status] || styles.inactive;

  return (
    <span
      className={`inline-flex items-center rounded-full font-medium ${statusStyle} ${sizeStyles[size]}`}
    >
      {status === "under review" ? "Under Review" : status.charAt(0).toUpperCase() + status.slice(1)}
    </span>
  );
}
