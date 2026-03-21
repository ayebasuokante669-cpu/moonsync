import { ActionButton } from "./ActionButton";

export function EmptyState({ icon: Icon, title, description, action }) {
  return (
    <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-12 text-center">
      <div className="mx-auto flex h-16 w-16 items-center justify-center rounded-full bg-[var(--color-primary-light)]">
        <Icon className="h-8 w-8 text-[var(--color-primary)]" />
      </div>
      <h3 className="mt-4 text-lg font-semibold text-foreground">{title}</h3>
      <p className="mt-2 text-sm text-[var(--color-muted-foreground)] max-w-md mx-auto">{description}</p>
      {action && (
        <div className="mt-6">
          <ActionButton variant="primary" onClick={action.onClick}>
            {action.label}
          </ActionButton>
        </div>
      )}
    </div>
  );
}
