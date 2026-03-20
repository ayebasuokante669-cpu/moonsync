export function Modal({ isOpen, onClose, title, description, children, footer, size = "md" }) {
  if (!isOpen) return null;

  const sizeClasses = {
    sm: "max-w-md",
    md: "max-w-lg",
    lg: "max-w-2xl",
    xl: "max-w-4xl",
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 dark:bg-black/60 p-4" onClick={onClose}>
      <div
        className={`w-full ${sizeClasses[size]} rounded-xl bg-[var(--color-card)] border border-transparent dark:border-[var(--color-border)] shadow-xl overflow-hidden`}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header */}
        <div className="flex items-center justify-between border-b border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
          <div>
            <h3 className="text-lg font-semibold text-foreground">{title}</h3>
            {description && <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{description}</p>}
          </div>
          <button
            onClick={onClose}
            className="rounded-lg p-2 hover:bg-[var(--color-muted)] transition-smooth"
          >
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 text-[var(--color-muted-foreground)]" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
              <line x1="18" y1="6" x2="6" y2="18" />
              <line x1="6" y1="6" x2="18" y2="18" />
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="p-6 max-h-[60vh] overflow-y-auto">
          {children}
        </div>

        {/* Footer */}
        {footer && (
          <div className="flex items-center justify-end gap-3 border-t border-[var(--color-border)] bg-[var(--color-secondary-bg)] px-6 py-4">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
