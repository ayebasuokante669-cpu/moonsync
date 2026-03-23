import { useState } from "react";
import { Save, Copy, Eye, EyeOff } from "lucide-react";
import { ActionButton } from "../components/admin/ActionButton";
import { toast } from "sonner";

export function Settings() {
  const [settings, setSettings] = useState({
    appName: "MoonSync Admin Portal",
    appVersion: "2.1.0",
    maintenanceMode: false,
    allowNewUsers: true,
    communityEnabled: true,
    chatbotEnabled: true,
    autoModeration: true,
    emailNotifications: true,
    maxCommunityPosts: "10",
  });

  const handleToggle = (key) => {
    setSettings((prev) => ({
      ...prev,
      [key]: !prev[key],
    }));
  };

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Settings</h1>
        <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
          Configure application settings and system preferences
        </p>
      </div>

      {/* General Settings */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <h3 className="text-lg font-semibold text-foreground mb-6">General Settings</h3>
        
        <div className="space-y-5">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <div>
              <label className="block text-sm font-medium text-foreground mb-2">
                Application Name
              </label>
              <input
                type="text"
                value={settings.appName}
                onChange={(e) => setSettings({ ...settings, appName: e.target.value })}
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              />
            </div>
            
            <div>
              <label className="block text-sm font-medium text-foreground mb-2">
                Version
              </label>
              <input
                type="text"
                value={settings.appVersion}
                onChange={(e) => setSettings({ ...settings, appVersion: e.target.value })}
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              />
            </div>
          </div>
        </div>
      </div>

      {/* Feature Controls */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <h3 className="text-lg font-semibold text-foreground mb-6">Feature Controls</h3>
        
        <div className="space-y-4">
          <ToggleRow
            label="Maintenance Mode"
            description="Temporarily disable app access for all users"
            checked={settings.maintenanceMode}
            onChange={() => handleToggle("maintenanceMode")}
            variant="warning"
          />
          
          <ToggleRow
            label="Allow New User Registrations"
            description="Enable or disable new user sign-ups"
            checked={settings.allowNewUsers}
            onChange={() => handleToggle("allowNewUsers")}
          />
          
          <ToggleRow
            label="Community Feature"
            description="Enable the community posts and discussions"
            checked={settings.communityEnabled}
            onChange={() => handleToggle("communityEnabled")}
          />
          
          <ToggleRow
            label="AI Chatbot"
            description="Enable AI-powered chatbot assistance"
            checked={settings.chatbotEnabled}
            onChange={() => handleToggle("chatbotEnabled")}
          />
          
          <ToggleRow
            label="Auto-Moderation"
            description="Automatically flag inappropriate content"
            checked={settings.autoModeration}
            onChange={() => handleToggle("autoModeration")}
          />
          
          <ToggleRow
            label="Email Notifications"
            description="Send email notifications to users"
            checked={settings.emailNotifications}
            onChange={() => handleToggle("emailNotifications")}
          />
        </div>
      </div>

      {/* Community Rules */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <h3 className="text-lg font-semibold text-foreground mb-6">Community Rules</h3>
        
        <div className="space-y-5">
          <div>
            <label className="block text-sm font-medium text-foreground mb-2">
              Max Posts Per Day (per user)
            </label>
            <input
              type="number"
              value={settings.maxCommunityPosts}
              onChange={(e) => setSettings({ ...settings, maxCommunityPosts: e.target.value })}
              className="w-full max-w-xs rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
            />
          </div>
          
          <div>
            <label className="block text-sm font-medium text-foreground mb-2">
              Community Guidelines
            </label>
            <textarea
              placeholder="Enter community guidelines..."
              className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              rows={6}
              defaultValue="1. Be respectful and supportive to all community members&#10;2. No spam or promotional content&#10;3. Protect your privacy - don't share personal information&#10;4. Medical advice should come from healthcare professionals&#10;5. Report inappropriate content to moderators"
            />
          </div>
        </div>
      </div>

      {/* System Info */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <h3 className="text-lg font-semibold text-foreground mb-6">System Information</h3>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <InfoRow label="Server Status" value="Online" valueClass="text-[var(--color-success)]" />
          <InfoRow label="Database Status" value="Connected" valueClass="text-[var(--color-success)]" />
          <InfoRow label="Last Backup" value="Feb 12, 2026 03:00 AM" />
          <InfoRow label="Storage Used" value="42.3 GB / 100 GB" />
          <InfoRow label="API Version" value="v2.1" />
          <InfoRow label="Uptime" value="99.9%" />
        </div>
      </div>

      {/* Save Button */}
      <div className="flex justify-end">
        <ActionButton variant="primary" icon={Save} size="lg" onClick={() => toast.success("Settings saved successfully!")}>
          Save All Changes
        </ActionButton>
      </div>
    </div>
  );
}

// Helper Components
function ToggleRow({
  label,
  description,
  checked,
  onChange,
  variant = "default",
}) {
  return (
    <div className={`flex items-start justify-between p-4 rounded-lg border transition-smooth ${
      variant === "warning" && checked
        ? "bg-[var(--color-warning-light)] border-[var(--color-warning)]"
        : "bg-[var(--color-secondary-bg)] border-[var(--color-border)]"
    }`}>
      <div className="flex-1">
        <h4 className="font-medium text-foreground">{label}</h4>
        <p className="text-sm text-[var(--color-muted-foreground)] mt-1">{description}</p>
      </div>
      <button
        onClick={onChange}
        className={`relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)] focus:ring-offset-2 ${
          checked ? "bg-[var(--color-primary)]" : "bg-[var(--color-muted)]"
        }`}
      >
        <span
          className={`pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out ${
            checked ? "translate-x-5" : "translate-x-0"
          }`}
        />
      </button>
    </div>
  );
}

function InfoRow({ label, value, valueClass = "" }) {
  return (
    <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-secondary-bg)]">
      <span className="text-sm text-[var(--color-muted-foreground)]">{label}</span>
      <span className={`text-sm font-semibold ${valueClass || "text-foreground"}`}>{value}</span>
    </div>
  );
}

