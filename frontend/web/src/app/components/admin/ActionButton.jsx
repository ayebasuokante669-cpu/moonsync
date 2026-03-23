export function ActionButton({
  variant = "primary",
  size = "md",
  icon: Icon,
  children,
  onClick,
  disabled = false,
  className = "",
  ...props
}) {
  const variantStyles = {
    primary: "bg-[var(--color-primary)] text-white hover:bg-[var(--color-primary-hover)]",
    secondary: "bg-[var(--color-secondary)] text-[var(--color-secondary-foreground)] hover:bg-[var(--color-secondary-accent)]",
    success: "bg-[var(--color-success)] text-white hover:opacity-90",
    warning: "bg-[var(--color-warning)] text-white hover:opacity-90",
    error: "bg-[var(--color-error)] text-white hover:opacity-90",
    ghost: "bg-transparent text-[var(--color-muted-foreground)] hover:bg-[var(--color-muted)]",
  };

  const sizeStyles = {
    sm: "px-3 py-1.5 text-xs",
    md: "px-4 py-2 text-sm",
    lg: "px-6 py-3 text-base",
  };

  return (
    <button
      onClick={onClick}
      disabled={disabled}
      className={`inline-flex items-center justify-center gap-2 rounded-lg font-medium transition-smooth disabled:cursor-not-allowed disabled:opacity-50 ${variantStyles[variant]} ${sizeStyles[size]} ${className}`}
      {...props}
    >
      {Icon && <Icon className="h-4 w-4" />}
      {children}
    </button>
  );
}
