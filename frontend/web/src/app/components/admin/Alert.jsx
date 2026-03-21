import { AlertTriangle, CheckCircle, Info, XCircle, X } from "lucide-react";

export function Alert({ variant, title, message, onClose }) {
  const styles = {
    success: {
      container: "bg-[var(--color-success-light)] border-[var(--color-success)]",
      icon: CheckCircle,
      iconColor: "text-[var(--color-success)]",
    },
    error: {
      container: "bg-[var(--color-error-light)] border-[var(--color-error)]",
      icon: XCircle,
      iconColor: "text-[var(--color-error)]",
    },
    warning: {
      container: "bg-[var(--color-warning-light)] border-[var(--color-warning)]",
      icon: AlertTriangle,
      iconColor: "text-[var(--color-warning)]",
    },
    info: {
      container: "bg-[var(--color-primary-light)] border-[var(--color-primary)]",
      icon: Info,
      iconColor: "text-[var(--color-primary)]",
    },
  };

  const style = styles[variant];
  const Icon = style.icon;

  return (
    <div className={`rounded-lg border p-4 ${style.container}`}>
      <div className="flex items-start gap-3">
        <Icon className={`h-5 w-5 flex-shrink-0 ${style.iconColor}`} />
        <div className="flex-1">
          {title && <h4 className="font-semibold text-foreground mb-1">{title}</h4>}
          <p className="text-sm text-foreground">{message}</p>
        </div>
        {onClose && (
          <button
            onClick={onClose}
            className="rounded-lg p-1 hover:bg-[var(--color-card)]/50 transition-smooth"
          >
            <X className="h-4 w-4 text-[var(--color-muted-foreground)]" />
          </button>
        )}
      </div>
    </div>
  );
}
