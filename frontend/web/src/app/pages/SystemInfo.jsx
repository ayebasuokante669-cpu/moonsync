import { Users, Activity, TrendingUp, Shield, CheckCircle, AlertTriangle } from "lucide-react";
import { StatsCard } from "../components/admin/StatsCard";
import { StatusBadge } from "../components/admin/StatusBadge";
import { ActionButton } from "../components/admin/ActionButton";
import { Card } from "../components/admin/Card";
import { Alert } from "../components/admin/Alert";
import { PageHeader } from "../components/admin/PageHeader";

export function SystemInfo() {
  return (
    <div className="space-y-6">
      <PageHeader
        title="System Info & Design System"
        description="Overview of system health, design tokens, and component library"
      />

      {/* System Health */}
      <div className="grid grid-cols-1 gap-6 sm:grid-cols-2 lg:grid-cols-4">
        <StatsCard
          title="API Health"
          value="100%"
          icon={Activity}
          trend={{ value: "0.0%", isPositive: true }}
          description="uptime"
        />
        <StatsCard
          title="Database"
          value="Healthy"
          icon={Shield}
          description="All connections active"
        />
        <StatsCard
          title="Response Time"
          value="42ms"
          icon={TrendingUp}
          trend={{ value: "5ms", isPositive: true }}
          description="average"
        />
        <StatsCard
          title="Active Users"
          value="2,680"
          icon={Users}
          description="currently online"
        />
      </div>

      {/* Alerts */}
      <div className="space-y-3">
        <h3 className="text-lg font-semibold text-foreground">Alert Components</h3>
        <Alert
          variant="success"
          title="Success Alert"
          message="This is a success message. All operations completed successfully."
        />
        <Alert
          variant="info"
          message="This is an info message. Here's some helpful information for you."
        />
        <Alert
          variant="warning"
          title="Warning Alert"
          message="This is a warning message. Please review this action carefully."
        />
        <Alert
          variant="error"
          title="Error Alert"
          message="This is an error message. Something went wrong and needs attention."
        />
      </div>

      {/* Color Palette */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Color Palette</h3>
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
          <ColorSwatch name="Primary" color="var(--color-primary)" />
          <ColorSwatch name="Success" color="var(--color-success)" />
          <ColorSwatch name="Warning" color="var(--color-warning)" />
          <ColorSwatch name="Error" color="var(--color-error)" />
          <ColorSwatch name="Muted" color="var(--color-muted)" />
        </div>
      </Card>

      {/* Status Badges */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Status Badges</h3>
        <div className="flex flex-wrap gap-3">
          <StatusBadge status="active" />
          <StatusBadge status="inactive" />
          <StatusBadge status="pending" />
          <StatusBadge status="banned" />
          <StatusBadge status="resolved" />
          <StatusBadge status="flagged" />
          <StatusBadge status="draft" />
          <StatusBadge status="published" />
        </div>
      </Card>

      {/* Buttons */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Button Variants</h3>
        <div className="flex flex-wrap gap-3">
          <ActionButton variant="primary" icon={CheckCircle}>Primary Button</ActionButton>
          <ActionButton variant="secondary">Secondary Button</ActionButton>
          <ActionButton variant="success" icon={CheckCircle}>Success Button</ActionButton>
          <ActionButton variant="warning" icon={AlertTriangle}>Warning Button</ActionButton>
          <ActionButton variant="error">Error Button</ActionButton>
          <ActionButton variant="ghost">Ghost Button</ActionButton>
        </div>
        
        <h4 className="text-md font-semibold text-foreground mt-6 mb-4">Button Sizes</h4>
        <div className="flex flex-wrap items-center gap-3">
          <ActionButton variant="primary" size="sm">Small</ActionButton>
          <ActionButton variant="primary" size="md">Medium</ActionButton>
          <ActionButton variant="primary" size="lg">Large</ActionButton>
        </div>
      </Card>

      {/* Typography */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Typography Scale</h3>
        <div className="space-y-3">
          <div>
            <h1>H1 Heading - Page Title</h1>
            <p className="text-sm text-[var(--color-muted-foreground)]">Font: Inter, Size: 1.5rem, Weight: 600</p>
          </div>
          <div>
            <h2>H2 Heading - Section Header</h2>
            <p className="text-sm text-[var(--color-muted-foreground)]">Font: Inter, Size: 1.25rem, Weight: 600</p>
          </div>
          <div>
            <h3>H3 Heading - Card Header</h3>
            <p className="text-sm text-[var(--color-muted-foreground)]">Font: Inter, Size: 1.125rem, Weight: 600</p>
          </div>
          <div>
            <p>Body text - Regular paragraph content with optimal readability</p>
            <p className="text-sm text-[var(--color-muted-foreground)]">Font: Inter, Size: 1rem, Weight: 400</p>
          </div>
        </div>
      </Card>

      {/* System Information */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Technical Information</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InfoRow label="Application Name" value="MoonSync Admin Portal" />
          <InfoRow label="Version" value="2.1.0" />
          <InfoRow label="Build Date" value="February 12, 2026" />
          <InfoRow label="Environment" value="Production" />
          <InfoRow label="API Version" value="v2.1" />
          <InfoRow label="Database" value="PostgreSQL 15.2" />
          <InfoRow label="Framework" value="React 18.3.1" />
          <InfoRow label="UI Library" value="Tailwind CSS v4" />
        </div>
      </Card>

      {/* Design Principles */}
      <Card>
        <h3 className="text-lg font-semibold text-foreground mb-4">Design Principles</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          <PrincipleCard
            title="Minimal & Calm"
            description="Clean interfaces with intentional white space. Every element serves a purpose."
          />
          <PrincipleCard
            title="Healthcare-Grade Clarity"
            description="High readability and accessibility. Clear hierarchy and intuitive navigation."
          />
          <PrincipleCard
            title="Professional Feminine"
            description="Soft lavender palette that's elegant, not playful. Sophisticated and trustworthy."
          />
          <PrincipleCard
            title="Responsive & Adaptive"
            description="Seamless experience across desktop, tablet, and mobile devices."
          />
        </div>
      </Card>
    </div>
  );
}

function ColorSwatch({ name, color }) {
  return (
    <div className="space-y-2">
      <div
        className="h-20 w-full rounded-lg border border-[var(--color-border)]"
        style={{ backgroundColor: color }}
      />
      <p className="text-sm font-medium text-foreground">{name}</p>
      <p className="text-xs text-[var(--color-muted-foreground)] font-mono">{color}</p>
    </div>
  );
}

function InfoRow({ label, value }) {
  return (
    <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-secondary-bg)]">
      <span className="text-sm text-[var(--color-muted-foreground)]">{label}</span>
      <span className="text-sm font-semibold text-foreground">{value}</span>
    </div>
  );
}

function PrincipleCard({ title, description }) {
  return (
    <div className="rounded-lg border border-[var(--color-border)] bg-[var(--color-secondary-bg)] p-4">
      <h4 className="font-semibold text-foreground mb-2">{title}</h4>
      <p className="text-sm text-[var(--color-muted-foreground)]">{description}</p>
    </div>
  );
}

