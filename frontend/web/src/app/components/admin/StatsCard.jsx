export function StatsCard({ title, value, icon: Icon, trend, description }) {
  return (
    <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6 transition-smooth hover:shadow-soft-lg hover:-translate-y-0.5">
      <div className="flex items-start justify-between">
        <div className="flex-1">
          <p className="text-sm font-medium text-[var(--color-muted-foreground)]">{title}</p>
          <h3 className="mt-2 text-3xl font-semibold text-foreground">{value}</h3>
          
          {trend && (
            <div className="mt-2 flex items-center gap-1">
              <span
                className={`text-sm font-medium ${
                  trend.isPositive ? "text-[var(--color-success)]" : "text-[var(--color-error)]"
                }`}
              >
                {trend.isPositive ? "+" : ""}{trend.value}
              </span>
              {description && (
                <span className="text-sm text-[var(--color-muted-foreground)]">{description}</span>
              )}
            </div>
          )}
        </div>
        
        <div className="flex h-12 w-12 items-center justify-center rounded-lg bg-[var(--color-primary-light)]">
          <Icon className="h-6 w-6 text-[var(--color-primary)]" />
        </div>
      </div>
    </div>
  );
}
