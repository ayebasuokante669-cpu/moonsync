import { useState, useEffect } from "react";
import { Send, Users, Calendar, Smartphone } from "lucide-react";
import { ActionButton } from "../components/admin/ActionButton";

const initialRecentNotifications = [
  { id: 1, title: "New Article Available: Understanding Your Cycle", info: "Sent to All Users • 2 hours ago • 3,890 recipients", timeAgo: "2 hours ago", status: "Delivered" },
  { id: 2, title: "Don't forget to log your daily mood!", info: "Sent to Active Users • 1 day ago • 2,680 recipients", timeAgo: "1 day ago", status: "Delivered" },
  { id: 3, title: "System Maintenance Scheduled", info: "Sent to All Users • 2 days ago • 3,890 recipients", timeAgo: "2 days ago", status: "Delivered" },
];

export function Notifications() {
  const [notificationData, setNotificationData] = useState({
    title: "",
    message: "",
    segment: "all",
    scheduleDate: "",
    scheduleDate: "",
    scheduleTime: "",
  });

  const [recentNotifications, setRecentNotifications] = useState(initialRecentNotifications);

  useEffect(() => {
    // Auto-clear sent notifications older than 24 hours
    setRecentNotifications(current => 
      current.filter(n => !n.timeAgo.includes("day") && !n.timeAgo.includes("week"))
    );
  }, []);

  return (
    <div className="space-y-6">
      {/* Page Header */}
      <div>
        <h1 className="text-2xl font-semibold text-foreground">Notifications Manager</h1>
        <p className="mt-1 text-sm text-[var(--color-muted-foreground)]">
          Create and schedule push notifications for MoonSync users
        </p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Notification Form */}
        <div className="lg:col-span-2 rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
          <h3 className="text-lg font-semibold text-foreground mb-6">Create Notification</h3>
          
          <div className="space-y-5">
            {/* Title */}
            <div>
              <label className="block text-sm font-medium text-foreground mb-2">
                Notification Title
              </label>
              <input
                type="text"
                placeholder="Enter notification title..."
                value={notificationData.title}
                onChange={(e) => setNotificationData({ ...notificationData, title: e.target.value })}
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              />
            </div>

            {/* Message */}
            <div>
              <label className="block text-sm font-medium text-foreground mb-2">
                Message
              </label>
              <textarea
                placeholder="Write your notification message..."
                value={notificationData.message}
                onChange={(e) => setNotificationData({ ...notificationData, message: e.target.value })}
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground placeholder:text-[var(--color-muted-foreground)] focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                rows={4}
              />
              <p className="mt-2 text-xs text-[var(--color-muted-foreground)]">
                Maximum 200 characters recommended for mobile notifications
              </p>
            </div>

            {/* Target Segment */}
            <div>
              <label className="block text-sm font-medium text-foreground mb-2">
                Target Audience
              </label>
              <select
                value={notificationData.segment}
                onChange={(e) => setNotificationData({ ...notificationData, segment: e.target.value })}
                className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
              >
                <option value="all">All Users</option>
                <option value="active">Active Users Only</option>
                <option value="inactive">Inactive Users (7+ days)</option>
                <option value="new">New Users (Last 30 days)</option>
              </select>
            </div>

            {/* Schedule */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Schedule Date
                </label>
                <input
                  type="date"
                  value={notificationData.scheduleDate}
                  onChange={(e) => setNotificationData({ ...notificationData, scheduleDate: e.target.value })}
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-foreground mb-2">
                  Schedule Time
                </label>
                <input
                  type="time"
                  value={notificationData.scheduleTime}
                  onChange={(e) => setNotificationData({ ...notificationData, scheduleTime: e.target.value })}
                  className="w-full rounded-lg border border-[var(--color-input-border)] bg-[var(--color-input)] px-4 py-3 text-sm text-foreground focus:border-[var(--color-primary)] focus:outline-none focus:ring-2 focus:ring-[var(--color-primary)]/20 transition-smooth"
                />
              </div>
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3 pt-4">
              <ActionButton variant="primary" icon={Send} className="flex-1">
                Send Now
              </ActionButton>
              <ActionButton variant="secondary" icon={Calendar} className="flex-1">
                Schedule
              </ActionButton>
            </div>
          </div>
        </div>

        {/* Preview & Stats */}
        <div className="space-y-6">
          {/* Mobile Preview */}
          <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
            <h3 className="text-lg font-semibold text-foreground mb-4">Preview</h3>
            
            {/* Phone Frame */}
            <div className="mx-auto max-w-xs">
              <div className="rounded-2xl border-4 border-[var(--color-muted)] bg-gradient-to-b from-[var(--color-secondary-bg)] to-white p-4 shadow-lg">
                <div className="flex items-center gap-3 mb-2">
                  <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-gradient-to-br from-[var(--color-primary)] to-[var(--color-primary-hover)]">
                    <Smartphone className="h-5 w-5 text-white" />
                  </div>
                  <div className="flex-1">
                    <p className="text-xs font-semibold text-foreground">MoonSync</p>
                    <p className="text-xs text-[var(--color-muted-foreground)]">now</p>
                  </div>
                </div>
                <div className="rounded-lg bg-[var(--color-card)] border border-[var(--color-border)] p-3 shadow-sm">
                  <h4 className="text-sm font-semibold text-foreground mb-1">
                    {notificationData.title || "Notification Title"}
                  </h4>
                  <p className="text-xs text-[var(--color-muted-foreground)]">
                    {notificationData.message || "Your notification message will appear here..."}
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Audience Stats */}
          <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
            <h3 className="text-lg font-semibold text-foreground mb-4">Reach Statistics</h3>
            
            <div className="space-y-3">
              <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-secondary-bg)]">
                <div className="flex items-center gap-2">
                  <Users className="h-4 w-4 text-[var(--color-primary)]" />
                  <span className="text-sm text-foreground">Target Users</span>
                </div>
                <span className="text-sm font-semibold text-foreground">
                  {notificationData.segment === "all" && "3,890"}
                  {notificationData.segment === "active" && "2,680"}
                  {notificationData.segment === "inactive" && "1,210"}
                  {notificationData.segment === "new" && "456"}
                </span>
              </div>
              
              <div className="flex items-center justify-between p-3 rounded-lg bg-[var(--color-secondary-bg)]">
                <div className="flex items-center gap-2">
                  <Calendar className="h-4 w-4 text-[var(--color-primary)]" />
                  <span className="text-sm text-foreground">Send Time</span>
                </div>
                <span className="text-sm font-semibold text-foreground">
                  {notificationData.scheduleDate && notificationData.scheduleTime
                    ? "Scheduled"
                    : "Immediate"}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Recent Notifications */}
      <div className="rounded-xl border border-[var(--color-border)] bg-[var(--color-card)] p-6">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-foreground">Recent Notifications</h3>
          <p className="text-xs text-[var(--color-muted-foreground)]">Auto-clears after 24h</p>
        </div>
        
        <div className="space-y-3">
          {recentNotifications.length === 0 ? (
            <div className="py-6 text-center text-sm text-[var(--color-muted-foreground)]">
              No recent notifications in the past 24 hours.
            </div>
          ) : (
            recentNotifications.map((notif) => (
              <div key={notif.id} className="flex items-start justify-between p-4 rounded-lg border border-[var(--color-border)] bg-[var(--color-secondary-bg)]">
                <div>
                  <h4 className="font-semibold text-foreground">{notif.title}</h4>
                  <p className="text-sm text-[var(--color-muted-foreground)] mt-1">
                    {notif.info}
                  </p>
                </div>
                <span className="text-xs font-medium text-[var(--color-success)]">{notif.status}</span>
              </div>
            ))
          )}
        </div>
      </div>
    </div>
  );
}

